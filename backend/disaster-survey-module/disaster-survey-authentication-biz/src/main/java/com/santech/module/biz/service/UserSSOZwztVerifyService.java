package com.santech.module.biz.service;

import com.linewell.sso.entity.SsoToken;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.SSOSyncOrgDto;
import com.santech.module.biz.entity.dto.SSOSyncUserDto;
import com.santech.module.biz.entity.vo.SSOZwztResultVo;


/**
 * @Title: UserSSOZwztVerifyService.java 
 * @Description：政务中台用户单点登录业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface UserSSOZwztVerifyService {

	/**
	 * 单点登录，获取token并查询用户信息
	 * @param String code 第三方授权码，通过前端请求接口获取的
	 * @return SSOZwztResultVo
	 */
	ExecuteResult<SSOZwztResultVo> singleSignOn(String code);
	
	/**
	 * @Title refreshToken
	 * @Description (此方法用于刷新或获取新签名access_token)
	 * @param String refreshToken(刷新token用于获取access_token)
	 * @return (新的可用token等信息)
	 */
	ExecuteResult<SsoToken> refreshToken(String refreshToken);
	/**
	 * 同步机构信息
	 * @param dto
	 * @return
	 */
	ExecuteResult<String> ssoSyncOrg(SSOSyncOrgDto dto);
	
	/**
	 * 同步用户信息
	 * @param dto
	 * @return
	 */
	ExecuteResult<String> ssoSyncUser(SSOSyncUserDto dto);
}
