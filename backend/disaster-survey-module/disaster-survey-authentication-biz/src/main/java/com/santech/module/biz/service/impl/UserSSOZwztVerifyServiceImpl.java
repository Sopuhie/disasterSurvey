package com.santech.module.biz.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.linewell.sso.SsoUtil;
import com.linewell.sso.entity.SsoToken;
import com.linewell.sso.entity.SsoUser;
import com.linewell.yjsso.YjSsoUtil;
import com.santech.core.framework.dao.DeptDao;
import com.santech.core.framework.dao.UserDao;
import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.core.framework.entity.model.QxUserDeptModel;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.EncryptUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.config.AuthenticationConfig;
import com.santech.module.biz.entity.dto.SSOSyncOrgDataDto;
import com.santech.module.biz.entity.dto.SSOSyncOrgDto;
import com.santech.module.biz.entity.dto.SSOSyncUserDataDto;
import com.santech.module.biz.entity.dto.SSOSyncUserDto;
import com.santech.module.biz.entity.vo.SSOZwztResultVo;
import com.santech.module.biz.service.UserSSOZwztVerifyService;

/**
 * @Title: UserSSOZwztVerifyServiceImpl.java 
 * @Description：政务中台用户单点登录业务层处理类 ，实现UserSSOZwztVerifyService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class UserSSOZwztVerifyServiceImpl implements UserSSOZwztVerifyService {
	
	public static final Logger logger = LoggerFactory.getLogger(UserSSOZwztVerifyServiceImpl.class);
	
	@Resource
	private AuthenticationConfig authenticationConfig;
	@Resource
	private YjSsoUtil yjSsoUtil;
	@Resource
	private SsoUtil ssoUtil;
	@Resource
	private DeptDao deptDao;
	@Resource
	private UserDao userDao;

    /**
	  * @Title singleSignon 
	  * @Description (此为单点登录认证完成后，更具解析的用户信息，查询用户详情的接口的业务层处理逻辑，查询成功会将认证的token及对应详情信息予以返回。)
	  * @param model (UserLoginModel,用户登录条件参数)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
	@Override
	public ExecuteResult<SSOZwztResultVo> singleSignOn(String code) {
		
		logger.info("根据第三方授权码，获取token信息以及用户信息，入参授权码【{}】",code);
		try {
	   		if(StringUtils.isEmpty(code)) {
	   			return ExecuteResult.error("授权码不能为空！", null);
	   		}
	   		//获取token信息
	   		SsoToken ssoToken = ssoUtil.getToken(code);
	   		if(ssoToken==null) {
	   			return ExecuteResult.error("根据授权码获取token失败！", null);
	   		}
	   		
	   		//根据token获取用户信息
	   		SsoUser  ssoUser  = ssoUtil.getUser(ssoToken.getAccess_token());
	   		if(ssoUser==null) {
	   			return ExecuteResult.error("根据accessToken获取用户信息失败！", null);
	   		}
	   		SSOZwztResultVo vo = new SSOZwztResultVo();
	   		vo.setSsoToken(ssoToken);
	   		vo.setSsoUser(ssoUser);
			return ExecuteResult.success("认证完成", vo);
		} catch (Exception e) {
			logger.error("授权码【{}】登录认证获取用户信息出现异常，详情：",code,e);
			return ExecuteResult.error("用户登录认证出现异常，请稍后再试！", null);
		}
	}
	
	
	
	/**
	 * @Title refreshToken
	 * @Description (此方法用于刷新或获取新签名token)
	 * @param username(用户名)
	 * @return (新的可用token)
	 */
	@Override
	public ExecuteResult<SsoToken> refreshToken(String refreshToken) {
		logger.info("获取新token，刷新token为【{}】",refreshToken);
		try {
	   		if(StringUtils.isEmpty(refreshToken)) {
	   			return ExecuteResult.error("刷新token不能为空！", null);
	   		}
	   		
	   		SsoToken ssoToken = ssoUtil.refreshToken(refreshToken);
	   		if(ssoToken==null) {
	   			return ExecuteResult.error("根据刷新token获取新token失败！", null);
	   		}
	   		
	   		return ExecuteResult.success("刷新token成功", ssoToken);
		} catch (Exception e) {
			logger.error("根据刷新token【{}】获取新token出现异常，详情：",refreshToken,e);
			return ExecuteResult.error("用户获取新token出现异常，请稍后再试！", null);
		}
	}
   
	@Override
	@Transactional
	public ExecuteResult<String> ssoSyncOrg(SSOSyncOrgDto dto) {

		logger.info("政务中台推送机构信息，入参【{}】", dto);
		try {
			String type = dto.getDataType();
			if ("ORG".equals(type)) {
				String operType = dto.getOperationType();
				SSOSyncOrgDataDto orgModel = dto.getData();
				String orgId = orgModel.getId();
				if (orgModel != null && StringUtil.isNotEmpty(orgId) && StringUtil.isNotEmpty(operType)) {
					if ("INSERT".equals(operType)) {
						Object porg = orgModel.getParentOrg();
						JSONObject orgjson = JSONObject.parseObject(JSON.toJSONString(porg));
						QxDepartmentModel dept = new QxDepartmentModel();
						dept.setId(orgId);
						dept.setPid(orgjson.getString("id"));
						dept.setOrgName(orgModel.getName());
						dept.setOrgCode(orgModel.getDeptCode());
						dept.setOrgType(orgModel.getNature());
						dept.setOrgDesc(orgModel.getOrgPath());
						dept.setStatus(orgModel.getStatus() == 0 ? "running" : "cancel");
						dept.setSortIndex(orgModel.getSortNum());
						dept.setXzqdm(orgModel.getRegionCode());
						dept.setCjr("linewell");
						dept.setZhgxr("linewell");
						deptDao.insertDeptInfo(dept);
					} else if ("UPDATE".equals(operType) || "INSERT_UPDATE".equals(operType)) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("orgId", orgId);
						QxDepartmentModel dept = deptDao.queryObject(QxDepartmentModel.class, "ID=:orgId", params);
						if (dept == null || dept.getId() == null) {
							// 新增
							Object porg = orgModel.getParentOrg();
							JSONObject orgjson = JSONObject.parseObject(JSON.toJSONString(porg));
							dept = new QxDepartmentModel();
							dept.setId(orgId);
							dept.setPid(orgjson != null ? orgjson.getString("id") : "");
							dept.setOrgName(orgModel.getName());
							dept.setOrgCode(orgModel.getDeptCode());
							dept.setOrgType(orgModel.getNature());
							dept.setOrgDesc(orgModel.getOrgPath());
							dept.setStatus(orgModel.getStatus() == 0 ? "running" : "cancel");
							dept.setSortIndex(orgModel.getSortNum());
							dept.setXzqdm(orgModel.getRegionCode());
							dept.setCjr("linewell");
							dept.setZhgxr("linewell");
							deptDao.insertDeptInfo(dept);
						} else {
							// 更新
							dept = getUpdateOrgModel(orgModel, dept);
							deptDao.updateDeptInfo(dept);
						}
					} else if ("DELETE".equals(operType)) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("orgId", orgId);
						QxDepartmentModel dept = deptDao.queryObject(QxDepartmentModel.class, "ID=:orgId", params);
						if (dept != null && dept.getId() != null) {
							deptDao.deleteDeptInfo(dept);
							List<QxUserDeptModel> uds = deptDao.queryObjects(QxUserDeptModel.class,
									"DEPARTMENTID=:orgId", params);
							if (uds != null && uds.size() > 0) {
								deptDao.deleteUserDeptInfoByDeptId(orgId);
							}
						}
					}
				}
			}
			return ExecuteResult.success("同步机构成功");
		} catch (Exception e) {
			logger.error("政务中台推送机构信息【{}】添加、更新或删除出现异常，详情：", dto, e);
			throw new RuntimeException("政务中台推送机构信息添加、更新或删除异常，原因：【" + e.getMessage() + "】");
			// return ExecuteResult.error("政务中台推送机构信息出现异常，请稍后再试！", null);
		}
	}

	private QxDepartmentModel getUpdateOrgModel(SSOSyncOrgDataDto orgModel, QxDepartmentModel dept) {
		Object porg = orgModel.getParentOrg();
		JSONObject orgjson = JSONObject.parseObject(JSON.toJSONString(porg));
		if (orgjson != null && StringUtil.isNotEmpty(orgjson.getString("id"))
				&& !orgjson.getString("id").equals(dept.getPid())) {
			dept.setPid(orgjson.getString("id"));
		}
		if (StringUtil.isNotEmpty(orgModel.getName()) && !orgModel.getName().equals(dept.getOrgName())) {
			dept.setOrgName(orgModel.getName());
		}
		if (StringUtil.isNotEmpty(orgModel.getDeptCode()) && !orgModel.getDeptCode().equals(dept.getOrgCode())) {
			dept.setOrgCode(orgModel.getDeptCode());
		}
		if (StringUtil.isNotEmpty(orgModel.getOrgPath()) && !orgModel.getOrgPath().equals(dept.getOrgDesc())) {
			dept.setOrgDesc(orgModel.getOrgPath());
		}
		if (StringUtil.isNotEmpty(orgModel.getStatus())) {
			dept.setStatus(orgModel.getStatus() == 0 ? "running" : "cancel");
		}
		if (StringUtil.isNotEmpty(orgModel.getSortNum()) && !orgModel.getSortNum().equals(dept.getSortIndex())) {
			dept.setSortIndex(orgModel.getSortNum());
		}
		if (StringUtil.isNotEmpty(orgModel.getRegionCode()) && !orgModel.getRegionCode().equals(dept.getXzqdm())) {
			dept.setXzqdm(orgModel.getRegionCode());
		}
		dept.setZhgxr("linewell");
		return dept;
	}

	@Override
	@Transactional
	public ExecuteResult<String> ssoSyncUser(SSOSyncUserDto dto) {
		logger.info("政务中台推送机构信息，入参【{}】", dto);
		try {
			String type = dto.getDataType();
			if ("ORG".equals(type)) {
				String operType = dto.getOperationType();
				SSOSyncUserDataDto userModel = dto.getData();
				String userId = userModel.getId();
				if (userModel != null && StringUtil.isNotEmpty(userId) && StringUtil.isNotEmpty(operType)) {
					if ("INSERT".equals(operType)) {
						QxUserModel userInfo = new QxUserModel();
						userInfo.setUserId(userId);
						userInfo.setUsername(
								EncryptUtil.decryptSm2(userModel.getUserName() == null ? "" : userModel.getUserName()));
						userInfo.setPassword(EncryptUtil.encrypt(authenticationConfig.getDefaultPassword()));// 同步过来的数据没密码，我这给设置默认密码，方便我们自己登录界面进行登录
						userInfo.setName(
								EncryptUtil.decryptSm2(userModel.getRealName() == null ? "" : userModel.getRealName()));
						userInfo.setGender(
								Integer.valueOf(userModel.getGender() != null ? userModel.getGender() : "1"));
						userInfo.setTel(yjSsoUtil.decryptMobile(userModel.getPhone()));
						userInfo.setStatus(userModel.getStatus());
						userInfo.setFirstLoginFlag(1);
						userInfo.setCjr("linewell");
						userInfo.setZhgxr("linewell");
						// 获取机构信息
						JSONArray belongOrg = userModel.getBelongOrg();
						if (belongOrg != null && belongOrg.size() > 0) {
							for (int i = 0; i < belongOrg.size(); i++) {
								JSONObject dept = belongOrg.getJSONObject(i);
								QxUserDeptModel ud = new QxUserDeptModel();
								ud.setDepartmentId(dept.getString("orgId"));
								ud.setUserId(userId);
								ud.setId(UuidUtils.buildID("UR") + i);
								deptDao.insertUserDeptInfo(ud);
							}
						}
						userDao.insertUserInfo(userInfo);
					} else if ("UPDATE".equals(operType) || "INSERT_UPDATE".equals(operType)) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("userId", userId);
						QxUserModel userInfo = userDao.queryObject(QxUserModel.class, "USERID=:userId", params);
						if (userInfo == null || userInfo.getUserId() == null) {
							// 新增
							userInfo = new QxUserModel();
							userInfo.setUserId(userId);
							userInfo.setUsername(EncryptUtil
									.decryptSm2(userModel.getUserName() == null ? "" : userModel.getUserName()));
							userInfo.setPassword(EncryptUtil.encrypt(authenticationConfig.getDefaultPassword()));// 同步过来的数据没密码，我这给设置默认密码，方便我们自己登录界面进行登录
							userInfo.setName(EncryptUtil
									.decryptSm2(userModel.getRealName() == null ? "" : userModel.getRealName()));
							userInfo.setGender(
									Integer.valueOf(userModel.getGender() != null ? userModel.getGender() : "1"));
							userInfo.setTel(yjSsoUtil.decryptMobile(userModel.getPhone()));
							userInfo.setStatus(userModel.getStatus());
							userInfo.setFirstLoginFlag(1);
							userInfo.setCjr("linewell");
							userInfo.setZhgxr("linewell");
							// 获取机构信息
							JSONArray belongOrg = userModel.getBelongOrg();
							if (belongOrg != null && belongOrg.size() > 0) {
								for (int i = 0; i < belongOrg.size(); i++) {
									JSONObject dept = belongOrg.getJSONObject(i);
									QxUserDeptModel ud = new QxUserDeptModel();
									ud.setDepartmentId(dept.getString("orgId"));
									ud.setUserId(userId);
									ud.setId(UuidUtils.buildID("UR") + i);
									deptDao.insertUserDeptInfo(ud);
								}
							}
							userDao.insertUserInfo(userInfo);
						} else {
							// 更新
							userInfo =  getUpdateUserModel(userModel,userInfo);
							userDao.updateUserInfo(userInfo);
							//更新用户部门关联信息
							JSONArray belongOrg = userModel.getBelongOrg();
							if (belongOrg != null && belongOrg.size() > 0) {
								List<QxUserDeptModel> uds = deptDao.queryObjects(QxUserDeptModel.class, "USERID=:userId",
										params);
								//判断新的关联关系与原关联关系情况，原没有则新增，原有新没有则删除
								for (int i = 0; i < belongOrg.size(); i++) {
									JSONObject dept = belongOrg.getJSONObject(i);
									if (uds != null && uds.size() > 0) {
										boolean flag = false;//默认无
										for(QxUserDeptModel ud : uds) {
											if(ud.getDepartmentId().equals(dept.getString("orgId"))) {
												flag = true;
												break;
											}
										}
										if(!flag) {
											//增加用户部门关联关系
											QxUserDeptModel ud = new QxUserDeptModel();
											ud.setDepartmentId(dept.getString("orgId"));
											ud.setUserId(userId);
											ud.setId(UuidUtils.buildID("UR") + i);
											deptDao.insertUserDeptInfo(ud);
										}
										
									}else {
										//增加用户部门关联关系
										QxUserDeptModel ud = new QxUserDeptModel();
										ud.setDepartmentId(dept.getString("orgId"));
										ud.setUserId(userId);
										ud.setId(UuidUtils.buildID("UR") + i);
										deptDao.insertUserDeptInfo(ud);
									}
								}
								if(uds!=null&&uds.size()>0) {
									for(QxUserDeptModel ud:uds){
										boolean flag = false;//默认无
										for (int i = 0; i < belongOrg.size(); i++) {
											JSONObject dept = belongOrg.getJSONObject(i);
											if(ud.getDepartmentId().equals(dept.getString("orgId"))) {
												flag = true;
												break;
											}
										}
										if(!flag) {
											deptDao.deleteUserDeptInfo(ud);
										}
									}
								}
							}
						}
					} else if ("DELETE".equals(operType)) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("userId", userId);
						QxUserModel user = userDao.queryObject(QxUserModel.class, "USERID=:userId", params);
						if (user != null && user.getUserId() != null) {
							userDao.deleteUserInfo(user);
							List<QxUserDeptModel> uds = deptDao.queryObjects(QxUserDeptModel.class, "USERID=:userId",
									params);
							if (uds != null && uds.size() > 0) {
								deptDao.deleteUserDeptInfoByUserId(userId);
							}
						}

					}
				}
			}
			return ExecuteResult.success("同步用户成功");
		} catch (Exception e) {
			logger.error("政务中台推送用户信息【{}】添加、更新或删除异常，详情：", dto, e);
			throw new RuntimeException("政务中台推送用户信息添加、更新或删除异常，原因：【" + e.getMessage() + "】");
		}
	}

	private QxUserModel getUpdateUserModel(SSOSyncUserDataDto userModel, QxUserModel userInfo) {

		if (StringUtil.isNotEmpty(userModel.getRealName()) && !userModel.getRealName().equals(userInfo.getName())) {
			userInfo.setName(
					EncryptUtil.decryptSm2(userModel.getRealName()));
		}
		if (StringUtil.isNotEmpty(userModel.getGender()) && !userModel.getGender().equals(userInfo.getGender())) {
			userInfo.setGender(
					Integer.valueOf(userModel.getGender()));
		}
		String tel = yjSsoUtil.decryptMobile(userModel.getPhone()==null?"":userModel.getPhone());
		if (StringUtil.isNotEmpty(tel) && !tel.equals(userInfo.getTel())) {
			userInfo.setTel(tel);
		}
		if (StringUtil.isNotEmpty(userModel.getStatus())&& !userModel.getStatus().equals(userInfo.getStatus())) {
			userInfo.setStatus(userModel.getStatus());
		}
		userInfo.setZhgxr("linewell");
		return userInfo;
	}
}
