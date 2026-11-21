package com.santech.module.biz.service;

import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.global.ExecuteResult;

/**
 * @Title: UserSSOSzfjVerifyService.java 
 * @Description：福建省数字身份管理服务平台(IAM)用户单点登录业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface UserSSOSzfjVerifyService {

	/**
	 * 单点登录，获取token信息
	 * @param String code 第三方授权码，通过前端请求省平台后，省平台回调时传入
	 * @return String
	 */
	ExecuteResult<String> ssoCode(String code);
	
	/**
	 * 前台根据票进行登录验证并获取用户信息
	 * @param ticket
	 * @return  QxUserSimpleModel
	 */
	ExecuteResult<QxUserSimpleModel> ssoLogin(String ticket);
	/**
	 * @Title ssoLogout
	 * @Description (此方法用于登出系统)
	 * @param ticket(票信息)
	 * @return (新的可用token)
	 */
	ExecuteResult<String> ssoLogout(String ticket);
}
