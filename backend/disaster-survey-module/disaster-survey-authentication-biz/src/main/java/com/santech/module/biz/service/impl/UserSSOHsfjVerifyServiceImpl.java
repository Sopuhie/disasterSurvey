package com.santech.module.biz.service.impl;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.linewell.hsfjsso.entity.AccessTokenModel;
import com.linewell.hsfjsso.entity.PeopleDetailInfo;
import com.linewell.hsfjsso.entity.ResultEntity;
import com.linewell.hsfjsso.util.HsfjSsoUtil;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.vo.SSOHsfjResultVo;
import com.santech.module.biz.service.UserSSOHsfjVerifyService;

import cn.hutool.json.JSONUtil;

/**
 * @Title: UserSSOHsfjVerifyServiceImpl.java 
 * @Description：海上福建用户单点登录业务层处理类 ，实现UserSSOHsfjVerifyService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class UserSSOHsfjVerifyServiceImpl implements UserSSOHsfjVerifyService {
	
	public static final Logger logger = LoggerFactory.getLogger(UserSSOHsfjVerifyServiceImpl.class);
	
	@Resource
	private HsfjSsoUtil hsfjSsoUtil;

    /**
	  * @Title singleSignon 
	  * @Description (此为单点登录认证完成后，更具解析的用户信息，查询用户详情的接口的业务层处理逻辑，查询成功会将认证的token及对应详情信息予以返回。)
	  * @param model (UserLoginModel,用户登录条件参数)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
	@Override
	public ExecuteResult<SSOHsfjResultVo> singleSignOn(String code) {
		
		logger.info("根据第三方授权码，获取token信息以及用户信息，入参授权码【{}】",code);
		try {
	   		if(StringUtils.isEmpty(code)) {
	   			return ExecuteResult.error("授权码不能为空！", null);
	   		}
	   		//获取token信息
	   		AccessTokenModel accessTokenModel = hsfjSsoUtil.getToken(code);
	   		if(accessTokenModel==null) {
	   			return ExecuteResult.error("根据授权码获取token失败！", null);
	   		}
	   		
	   		//根据token获取用户信息
	   		PeopleDetailInfo peopleDetailInfo = hsfjSsoUtil.getUserInfo(accessTokenModel.getAccess_token());
	   		if(peopleDetailInfo==null) {
	   			return ExecuteResult.error("根据accessToken获取用户信息失败！", null);
	   		}
	   		SSOHsfjResultVo vo = new SSOHsfjResultVo();
	   		vo.setTokenInfo(accessTokenModel);
	   		vo.setUserInfo(peopleDetailInfo);
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
	public ExecuteResult<AccessTokenModel> refreshToken(String refreshToken) {
		logger.info("获取新token，刷新token为【{}】",refreshToken);
		try {
	   		if(StringUtils.isEmpty(refreshToken)) {
	   			return ExecuteResult.error("刷新token不能为空！", null);
	   		}
	   		
	   		AccessTokenModel accessTokenModel = hsfjSsoUtil.refreshToken(refreshToken);
	   		if(accessTokenModel==null) {
	   			return ExecuteResult.error("根据刷新token获取新token失败！", null);
	   		}
	   		
	   		return ExecuteResult.success("刷新token成功", accessTokenModel);
		} catch (Exception e) {
			logger.error("根据刷新token【{}】获取新token出现异常，详情：",refreshToken,e);
			return ExecuteResult.error("用户获取新token出现异常，请稍后再试！", null);
		}
	}
    
	@Override
	public ExecuteResult<String> userLogout(String accessToken) {
		try {
			logger.info("退出登录，注销token【{}】",accessToken);
			if(StringUtils.isEmpty(accessToken)) {
	   			return ExecuteResult.error("刷新token不能为空！", null);
	   		}
			String result = hsfjSsoUtil.logout(accessToken);
			boolean resultFlag = false;//默认登出失败
			try {
	          ResultEntity<JSONObject> resultEntity = (ResultEntity<JSONObject>)JSONUtil.toBean(result, ResultEntity.class);
	          if (resultEntity != null && "200".equals(resultEntity.getCode())) {
	        	  resultFlag = true;
	          }
	        } catch (Exception e) {
	        	logger.error("用户登出结果【{}】转换失败，异常详情：",result,e);
	        } 
			if(resultFlag) {
				return ExecuteResult.success("退出登录成功", null);
			}else {
				return ExecuteResult.error(result, null);
			}
		} catch (Exception e) {
			logger.error("用户登出，将用户accessToken【{}】注销出现异常，异常详情：",accessToken,e);
			return ExecuteResult.error("退出登录失败", null);
		}
		
	}
   
	
}
