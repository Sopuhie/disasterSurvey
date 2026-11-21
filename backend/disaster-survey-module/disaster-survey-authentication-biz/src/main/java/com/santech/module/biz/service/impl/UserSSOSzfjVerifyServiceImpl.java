package com.santech.module.biz.service.impl;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.linewell.yjsso.YjSsoUtil;
import com.santech.core.framework.dao.UserDao;
import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.UserSSOSzfjVerifyService;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * @Title: UserSSOZwztVerifyServiceImpl.java 
 * @Description：福建省数字身份管理服务平台(IAM)单点登录业务层处理类 ，实现UserSSOSzfjVerifyService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class UserSSOSzfjVerifyServiceImpl implements UserSSOSzfjVerifyService {
	
	public static final Logger logger = LoggerFactory.getLogger(UserSSOSzfjVerifyServiceImpl.class);
	
	@Autowired
    private YjSsoUtil yjSsoUtil;
    
    @Autowired
    private RedisUtil redisUtil;

    @Value("${yjsso.frontUrl}")
    protected String frontUrl;
    
    @Value("${yjsso.globalLogoutUrl}")
    protected String globalLogoutUrl;
    
    @Resource
    private UserDao userDao;
    
	@Override
	public ExecuteResult<String> ssoCode(String code) {
		logger.info("根据第三方授权码，获取token信息以及用户信息，入参授权码【{}】",code);
		try {
	   		if(StringUtils.isEmpty(code)) {
	   			return ExecuteResult.error("授权码不能为空！", null);
	   		}
	   		String accessToken = yjSsoUtil.getAccessToken(code);
	        if (StringUtil.isEmpty(accessToken)) {
	        	throw new Exception("获取accessToken失败!");
	        }
	        String ticket = accessToken + System.currentTimeMillis();
	        redisUtil.set("yjsso_"+ticket, accessToken,3600*4);//设置四个小时的有效期
			return ExecuteResult.success("获取accessToken成功", "redirect:" + frontUrl + ticket);
		} catch (Exception e) {
			logger.error("授权码【{}】登录认证获取token出现异常，详情：",code,e);
			return ExecuteResult.error("用户登录认证获取ticket出现异常，请稍后再试！", "redirect:" + frontUrl);
		}
	}



	@Override
	public ExecuteResult<QxUserSimpleModel> ssoLogin(String ticket) {
		try {
	   		if(StringUtils.isEmpty(ticket)) {
	   			return ExecuteResult.error("票不能为空！", null);
	   		}
	   	    // 1. 取 accessToken
	        String accessToken = String.valueOf(redisUtil.get("yjsso_"+ticket));
	        //redisUtil.del("yjsso_"+ticket);
	        if (StringUtil.isEmpty(accessToken) || "null".equals(accessToken)) {
	            throw new Exception("ticket 无效或已过期!");
	        }

	        // 2. 获取并解析用户信息
	        String userJsonStr = yjSsoUtil.getUserInfo(accessToken);
	        if(StringUtil.isEmpty(userJsonStr)) {
	        	throw new Exception("根据accessToken获取用户信息失败!");
	        }
	        JSONObject json = JSONUtil.parseObj(userJsonStr);
	        String mobileStr = json.getStr("mobile", "");
	        if(StringUtil.isEmpty(mobileStr)) {
	        	logger.error("IAM用户信息：【{}】",userJsonStr);
	        	throw new Exception("根据accessToken获取的用户信息中手机号为空!");
	        }
	        // 3. 解密手机号
	        String mobile = yjSsoUtil.decryptMobile(mobileStr);
	        if (StringUtil.isEmpty(mobile)) {
	            throw new Exception("用户手机号解密后为空!");
	        }
	        // 4. 根据手机号查询本地用户并登录
	        QxUserSimpleModel user = userDao.getUserByUserTel(mobile);
	        if (user == null||StringUtil.isEmpty(user.getUserId())) {
	            throw new Exception("本地未查到对应用户，登录失败");
	        }
			return ExecuteResult.success("登录成功", user);
		} catch (Exception e) {
			logger.error("票【{}】登录认证获取用户信息出现异常，详情：",ticket,e);
			return ExecuteResult.error("用户登录认证获取用户信息出现异常，请稍后再试！", null);
		}
	}
	
	/**
	 * @Title ssoLogout
	 * @Description (此方法用于登出系统)
	 * @param ticket(票信息)
	 * @return (新的可用token)
	 */
	@Override
	public ExecuteResult<String> ssoLogout(String ticket) {
		logger.info("根据票信息进行系统登出，入参【{}】",ticket);
		try {
	   		if(StringUtils.isEmpty(ticket)) {
	   			return ExecuteResult.error("票不能为空！", null);
	   		}
	   		// 1. 取 accessToken并注销
	        String accessToken = String.valueOf(redisUtil.get("yjsso_"+ticket));
	   		boolean result = yjSsoUtil.revokeToken(accessToken);
	   		//2. 删除缓存的token
	   		redisUtil.del("yjsso_"+ticket);
			return ExecuteResult.success("清理缓存成功，并重定向到登出链接", "redirect:" + globalLogoutUrl);
		} catch (Exception e) {
			logger.error("根据票信息【{}】进行系统登出出现异常，详情：",ticket,e);
			return ExecuteResult.error("根据票信息【{}】进行系统登出出现异常，请稍后再试！", "redirect:" + frontUrl + ticket);
		}
	}




   
	
}
