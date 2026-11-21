package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.UserLoginDto;

/**
 * @Title: UserService.java 
 * @Description：用户业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface UserVerifyService {

	/**
	 * 登录认证，并返回token及用户信息
	 * @param model
	 * @return
	 */
	ExecuteResult<Map<String,Object>> login(UserLoginDto model);
	
	/**
	 * 单点登录，查询用户信息
	 * @param model
	 * @return
	 */
	ExecuteResult<Map<String,Object>> singleSignon(UserLoginDto model);
	
	/**
	 * 根据角色ID，获取对应的所有用户信息
	 * @param roleId
	 * @return
	 */
	ExecuteResult<List<QxUserModel>> getUserInfosByRoleId(String roleId);
	
	/**
	 * @Title refreshToken
	 * @Description (此方法用于刷新或获取新签名token)
	 * @param username(用户名)
	 * @return (新的可用token)
	 */
	ExecuteResult<String> refreshToken(String username);
	
	/**
	 * 验证码校验
	 * @param
	 * @return
	 */
	ExecuteResult<Map<String,Object>> checkVerificationCode(String yzm, String requestCode);
	
	/**
	 * @Title checkUserLogin
	 * @Description (此方法用于检查用户是否已经登录系统，该接口需要配合登录以及登出接口使用)
	 * @param username(用户名)
	 * @return (检查结果信息)
	 */
	ExecuteResult<JSONObject> checkUserLogin(String username);
	/**
	 * @Title userLoginOut
	 * @Description (此方法用于用户登出系统，进行token注销)
	 * @param username(用户名)
	 * @return (注销结果信息)
	 */
	ExecuteResult<String> userLoginOut(String username);
}
