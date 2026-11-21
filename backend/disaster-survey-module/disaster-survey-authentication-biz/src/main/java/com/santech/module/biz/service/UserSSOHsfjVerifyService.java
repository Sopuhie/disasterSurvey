package com.santech.module.biz.service;

import com.linewell.hsfjsso.entity.AccessTokenModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.vo.SSOHsfjResultVo;

/**
 * @Title: UserSSOHsfjVerifyService.java 
 * @Description：海上福建用户单点登录业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface UserSSOHsfjVerifyService {

	/**
	 * 单点登录，获取token并查询用户信息
	 * @param String code 第三方授权码，通过前端请求接口获取的
	 * @return SSOHsfjResultVo
	 */
	ExecuteResult<SSOHsfjResultVo> singleSignOn(String code);
	
	/**
	 * @Title refreshToken
	 * @Description (此方法用于刷新或获取新签名access_token)
	 * @param String refreshToken(刷新token用于获取access_token)
	 * @return (新的可用token等信息)
	 */
	ExecuteResult<AccessTokenModel> refreshToken(String refreshToken);
	
	/**
	 * @Title userLoginOut
	 * @Description (此方法用于用户登出系统，进行token注销)
	 * @param accessToken
	 * @return (登出结果信息)
	 */
	ExecuteResult<String> userLogout(String accessToken);
	
}
