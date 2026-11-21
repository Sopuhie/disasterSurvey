package com.santech.module.biz.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.dao.DeptDao;
import com.santech.core.framework.dao.RoleDao;
import com.santech.core.framework.dao.UserDao;
import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.core.framework.entity.model.QxRoleUserModel;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.rsa.RSAUtils;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.JWTUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.config.AuthenticationConfig;
import com.santech.module.biz.entity.dto.UserLoginDto;
import com.santech.module.biz.service.UserVerifyService;

/**
 * @Title: UserVerifyServiceImpl.java 
 * @Description：用户业务层处理类 ，实现UserVerifyService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class UserVerifyServiceImpl implements UserVerifyService {
	
	public static final Logger logger = LoggerFactory.getLogger(UserVerifyServiceImpl.class);
	@Resource
	private AuthenticationConfig authenticationConfig;
    @Resource
    private UserDao userDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private DeptDao deptDao;
    
    @Resource
    private RedisUtil redisUtil;

    /**
	  * @Title login 
	  * @Description (此为非单点登录的登录认证接口的业务层处理逻辑，会更具登录时提供的用户名及密码进行用户认证
	  * 认证成功会将生成的认证token及对应用户详情信息予以返回。)
	  * @param model (UserLoginModel,用户登录条件参数)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
    @Override
    public ExecuteResult<Map<String,Object>> login(UserLoginDto model) {
    	String username = model.getUsername();
    	String password = model.getPassword();
    	logger.info("用户登录认证，入参用户名【{}】",username);
    	try {
    		int count = redisUtil.get("verifyfail_"+username)==null?0:(int) redisUtil.get("verifyfail_"+username);
    		if(count>5) {
    			return ExecuteResult.error("验签错误超过5次，请10分钟后再试或联系管理员！", null);
    		}
    		if(StringUtils.isEmpty(username)) {
    			return ExecuteResult.error("用户名不能为空！", null);
    		}
    		if(StringUtils.isEmpty(password)) {
    			return ExecuteResult.error("用户密码不能为空！", null);
    		}
    		String decryptPassword = RSAUtils.decryptByPrivateKey(authenticationConfig.getRsaPrivateKey(), password);
			Map<String,Object>	result = userDao.login(username,decryptPassword);
			QxUserModel userModel = (QxUserModel) result.get("userInfo");
			
			if(!"1".equals(result.get("checkResult")+"")) {
				//记录错误次数
				count++;
				redisUtil.set("verifyfail_"+username, count, 10*60);//10分钟有效期
				return ExecuteResult.error("用户名或密码错误！", null);
			}
			redisUtil.del("verifyfail_"+username);//认证成功删除拦截
			String adminFlag = "0";//默认非管理员
			if(userModel!=null) {
				//给认证通过的用户,返回信息中增加角色信息
				logger.info("用户【{}】，开始查询其具有的角色",username);
				List<QxRoleModel> roles = roleDao.getRolesByUserId(userModel.getUserId());
				userModel.setRoles(roles);
				if(roles!=null&&roles.size()>0) {
					for(QxRoleModel role:roles) {
						//if("rl0".equalsIgnoreCase(role.getRoleId())) {
						if(role.getRoleName().contains("管理员")) {
							adminFlag = "1";
							break;
						}
					}
				}
				userModel.setAdminFlag(adminFlag);
				//给数据增加登录标识
				result.put("loginFlag", "0");//0本地登录，1单点登录
				//增加部门信息
				List<QxDepartmentModel> depts = deptDao.getDeptByUserId(userModel.getUserId());
				userModel.setDepts(depts);
			}
			//生成token
			Map<String,String> userInfo = new HashMap<String,String>();
			userInfo.put("username",userModel.getUsername());
			userInfo.put("userId",userModel.getUserId());
			userInfo.put("name",userModel.getName());
			userInfo.put("districtcode",userModel.getDistrictcode());
			userInfo.put("adminFlag",userModel.getAdminFlag());
 			String token = JWTUtil.generatorToken(userInfo);
			if(StringUtils.isEmpty(token)) {
				return ExecuteResult.error("生成token异常！", null);
			}else {
				result.put("token",token);
				logger.info("用户【{}】登录认证，返回结果token【{}】",username,token);
			}
			
			//获取登录人之前缓存的token进行置为失效，并进行删除
			String lastToken = redisUtil.get("verifyToken_"+username)==null?null:(String) redisUtil.get("verifyToken_"+username);
			if(StringUtil.isNotEmpty(lastToken)) {
				Map<String,Object> dataMap = JWTUtil.getTokenMeta(lastToken);
				if(StringUtil.isNotEmpty(dataMap.get("tokenId"))) {
					String tokenId = (String) dataMap.get("tokenId");
					long expire = (long) dataMap.get("expire")>0&&(long) dataMap.get("expire")<7200? (long) dataMap.get("expire"):7200L;
					//将该token放入黑名单,并删除之前缓存的token值
					redisUtil.set("expiretoken_"+tokenId, "", expire);
				}
				redisUtil.del("verifyToken_"+username);
			}
			//将新token根据用户缓存到redis中
			redisUtil.set("verifyToken_"+username, token,JWTUtil.EXPIRE_TIME);//设置和token一个失效期
			
			return ExecuteResult.success("认证完成", result);
		} catch (Exception e) {
			logger.error("用户【{}】登录认证出现异常，详情：",username,e);
			return ExecuteResult.error("用户登录认证出现异常，请稍后再试！", null);
		}
    } 
    /**
	  * @Title singleSignon 
	  * @Description (此为单点登录认证完成后，更具解析的用户信息，查询用户详情的接口的业务层处理逻辑，查询成功会将认证的token及对应详情信息予以返回。)
	  * @param model (UserLoginModel,用户登录条件参数)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
	@Override
	public ExecuteResult<Map<String, Object>> singleSignon(UserLoginDto model) {
		Map<String,Object> result = new HashMap<String,Object>();
		String username = model.getUsername();
		String token = model.getToken();
		logger.info("用户登录认证，入参用户名【{}】",username);
		try {
	   		if(StringUtils.isEmpty(username)) {
	   			return ExecuteResult.error("用户名不能为空！", null);
	   		}
	   		if(StringUtils.isEmpty(token)) {
	   			return ExecuteResult.error("单点登录认证名牌不能为空！", null);
	   		}
	   		Map<String,Object> paramsMap = new HashMap<String,Object>();
            paramsMap.put("username",username);//用户名
            paramsMap.put("status",0);//查询有效的用户，0在职，1离职或逻辑删除
            QxUserModel userModel = userDao.queryObject(QxUserModel.class, "username=:username and status=:status", paramsMap);
			if(userModel==null||StringUtil.isEmpty(userModel.getUsername())) {//认证通过，生成token
				return ExecuteResult.error("未获取到用户信息！", null);
			}
			
			//给认证通过的用户,返回信息中增加角色信息
			logger.info("用户【{}】，开始查询其具有的角色",username);
			List<QxRoleModel> roles = roleDao.getRolesByUserId(userModel.getUserId());
			userModel.setRoles(roles);
			
			//增加部门信息
			List<QxDepartmentModel> depts = deptDao.getDeptByUserId(userModel.getUserId());
			userModel.setDepts(depts);
			
			//生成我们自己的token
			String adminFlag = "0";//默认非管理员
			if(roles!=null&&roles.size()>0) {
				for(QxRoleModel role:roles) {
					//if("rl0".equalsIgnoreCase(role.getRoleId())) {
					if(role.getRoleName().contains("管理员")) {
						adminFlag = "1";
						break;
					}
				}
			}
			userModel.setAdminFlag(adminFlag);
			//Map<String,String> userInfo = new HashMap<String,String>();
			//userInfo.put("username",userModel.getUsername());
			//userInfo.put("userId",userModel.getUserId());
			//userInfo.put("name",userModel.getName());
			//userInfo.put("districtcode",userModel.getDistrictcode());
			//userInfo.put("adminFlag",userModel.getAdminFlag());
			//String ourtoken = JWTUtil.generatorToken(userInfo);
			//if(StringUtils.isEmpty(ourtoken)) {
			//	return ExecuteResult.error("生成自有token异常！", null);
			//}else {
			//	result.put("token",ourtoken);
			//	logger.info("用户【{}】登录认证，返回结果token【{}】",username,token);
			//}
			result.put("token",token);
			result.put("userInfo", userModel);
			result.put("loginFlag", "1");//0本地登录，1单点登录
			
			return ExecuteResult.success("认证完成", result);
		} catch (Exception e) {
			logger.error("用户【{}】登录认证获取用户信息出现异常，详情：",username,e);
			return ExecuteResult.error("用户登录认证出现异常，请稍后再试！", null);
		}
	}
	
	@Override
	public ExecuteResult<Map<String, Object>> checkVerificationCode(String yzm, String requestCode) {
		logger.info("用户登录时验证码校验，入参yzm【{}】，requestCode【{}】", yzm, requestCode);
		try {
			if (StringUtil.isEmpty(yzm)) {
				return ExecuteResult.error("未输入验证码！", null);
			} else {
				if (requestCode != null) {
					String redisYzm = String.valueOf(redisUtil.get(requestCode));
					if (redisYzm.equalsIgnoreCase(yzm)) {
						redisUtil.del(requestCode);
						return ExecuteResult.success("用户登录时验证码校验成功", null);
					} else {
						return ExecuteResult.error("验证码错误！", null);
					}
				} else {
					return ExecuteResult.error("验证码已失效！", null);
				}
			}
		} catch (Exception e) {
			logger.error("用户登录时验证码校验出现异常，详情：", e);
			return ExecuteResult.error("用户登录时验证码校验出现异常，请稍后再试！", null);
		}
	} 
	
	/**
	 * @Title refreshToken
	 * @Description (此方法用于刷新或获取新签名token)
	 * @param username(用户名)
	 * @return (新的可用token)
	 */
	@Override
	public ExecuteResult<String> refreshToken(String username) {
		logger.info("获取新token，入参用户名【{}】",username);
		try {
	   		if(StringUtils.isEmpty(username)) {
	   			return ExecuteResult.error("用户名不能为空！", null);
	   		}
	   		Map<String,Object> paramsMap = new HashMap<String,Object>();
            paramsMap.put("username",username);
            paramsMap.put("status",0);//查询有效的用户，0在职，1离职或逻辑删除
            QxUserModel userModel = userDao.queryObject(QxUserModel.class, "username=:username and status=:status", paramsMap);
			if(userModel==null||StringUtil.isEmpty(userModel.getUsername())) {
				return ExecuteResult.error("未获取到用户信息，无法生成token！", null);
			}else{
				//给认证通过的用户,返回信息中增加角色信息
				logger.info("用户【{}】，开始查询其具有的角色",username);
				List<QxRoleModel> roles = roleDao.getRolesByUserId(userModel.getUserId());
				//生成我们自己的token
				String adminFlag = "0";//默认非管理员
				if(roles!=null&&roles.size()>0) {
					for(QxRoleModel role:roles) {
						//if("rl0".equalsIgnoreCase(role.getRoleId())) {
						if(role.getRoleName().contains("管理员")) {
							adminFlag = "1";
							break;
						}
					}
				}
				userModel.setAdminFlag(adminFlag);
				//生成token
				Map<String,String> userInfo = new HashMap<String,String>();
				userInfo.put("username",userModel.getUsername());
				userInfo.put("userId",userModel.getUserId());
				userInfo.put("name",userModel.getName());
				userInfo.put("districtcode",userModel.getDistrictcode());
				userInfo.put("adminFlag",userModel.getAdminFlag());
				String ourtoken = JWTUtil.generatorToken(userInfo);
				if(StringUtils.isEmpty(ourtoken)) {
					return ExecuteResult.error("生成token异常!", null);
				}else {
					return ExecuteResult.success("获取token成功!", ourtoken);
				}
			}
		} catch (Exception e) {
			logger.error("用户【{}】获取新token出现异常，详情：",username,e);
			return ExecuteResult.error("用户获取新token出现异常，请稍后再试！", null);
		}
	}
    /**
	  * @Title getUserInfosByRoleId 
	  * @Description (此为根据角色ID查询对应用户信息的接口的业务层处理逻辑，查询前会对入参进行非空校验，校验通过会将查询到的用户信息进行返回。)
	  * @param roleId (角色ID)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
    @Override
    public ExecuteResult<List<QxUserModel>> getUserInfosByRoleId(String roleId) {
    	logger.info("根据角色ID【{}】，获取对应的所有用户信息",roleId);
    	try {
    		if(StringUtils.isEmpty(roleId)) {
    			return ExecuteResult.error("角色ID不能为空！", null);
    		}
    		//获取角色用户信息
    		List<QxRoleUserModel>  models = userDao.getRoleUsersByRoleId(roleId);
    		List<QxUserModel> users =null;
			if(models!=null&&models.size()>0) {
				String userIds = "";
				StringBuilder userbuf = new StringBuilder("");
				for(QxRoleUserModel model:models) {
					userbuf.append(model.getUserId()).append(",");
				}
				userIds = userbuf.toString();
				if(userIds.lastIndexOf(",")>-1) {
					userIds = userIds.substring(0,userIds.lastIndexOf(","));
				}
				users = userDao.getUserInfosByIds(userIds);
			}else {
				return ExecuteResult.error("根据角色ID未查到对应用户信息", null);
			}
			return ExecuteResult.success("根据角色ID查询用户成功", users);
		} catch (Exception e) {
			logger.error("根据角色ID【{}】，获取对应的所有用户信息出现异常，详情：",roleId,e);
			return ExecuteResult.error("角色对应用户查询出现异常，请稍后再试！", null);
		}
    }
	@Override
	public ExecuteResult<JSONObject> checkUserLogin(String username) {
		JSONObject result = new JSONObject();
		result.put("checkCode", "NO");//默认未登录
		try {
			if(redisUtil.hasKey("verifyToken_"+username)) {
				result.put("checkCode", "YES");
				result.put("tip", "该用户已在其他设备登录，确认是否要继续进行登录操作（继续操作，已登录用户将自动退出系统）");
			}
		} catch (Exception e) {
			logger.error("检查用户【{}】登录状态出现异常，异常详情：",username,e);
		}
		return ExecuteResult.success("检查完成", result);
	}

	@Override
	public ExecuteResult<String> userLoginOut(String username) {
		try {
			if(redisUtil.hasKey("verifyToken_"+username)) {
				//获取登录人之前缓存的token进行置为失效（放入黑名单），并进行删除
				String lastToken = redisUtil.get("verifyToken_"+username)==null?null:(String) redisUtil.get("verifyToken_"+username);
				if(StringUtil.isNotEmpty(lastToken)) {
					Map<String,Object> dataMap = JWTUtil.getTokenMeta(lastToken);
					if(StringUtil.isNotEmpty(dataMap.get("tokenId"))) {
						String tokenId = (String) dataMap.get("tokenId");
						long expire = (long) dataMap.get("expire")>0&&(long) dataMap.get("expire")<7200? (long) dataMap.get("expire"):7200L;
						//将该token放入黑名单,并删除之前缓存的token值
						redisUtil.set("expiretoken_"+tokenId, "", expire);
					}
					redisUtil.del("verifyToken_"+username);
				}
			}
			return ExecuteResult.success("退出成功", null);
		} catch (Exception e) {
			logger.error("用户登出，将用户【{}】登录token放入黑名单出现异常，异常详情：",username,e);
			return ExecuteResult.error("退出失败", null);
		}
		
	}
	
}
