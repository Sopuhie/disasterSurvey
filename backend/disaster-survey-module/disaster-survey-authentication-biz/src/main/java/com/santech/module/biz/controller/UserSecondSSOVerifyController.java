package com.santech.module.biz.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linewell.hsfjsso.entity.AccessTokenModel;
import com.linewell.sso.entity.SsoToken;
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.SSOSyncOrgDto;
import com.santech.module.biz.entity.dto.SSOSyncUserDto;
import com.santech.module.biz.entity.vo.SSOHsfjResultVo;
import com.santech.module.biz.entity.vo.SSOZwztResultVo;
import com.santech.module.biz.service.UserSSOHsfjVerifyService;
import com.santech.module.biz.service.UserSSOZwztVerifyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: UserSSOVerifyController.java
 * @Description：用户单点登录相关接口，单点登录分两种，分为海上福建版和政务中台版，根据客户要求，两者都要存在，
 * 根据不同的场景进行对应单点的切换，为了应付等保测评要求使用政务中台版，后续上线需要再切回海上福建版
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/user/verify/sso")
@Api(tags = "Auth-南威单点登录认证信息相关功能接口")
public class UserSecondSSOVerifyController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserSecondSSOVerifyController.class);

    @Resource
    private UserSSOZwztVerifyService userSSOZwztVerifyService;
    
    @Resource
    private UserSSOHsfjVerifyService userSSOHsfjVerifyService;
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("UserSecondSSOVerifyController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }

    /**
	  * @Title login 
	  * @Description (登录认证(含本地认证及单点登录逻辑)，并获取用户信息、用户角色信息、用户部门信息)
	  * @param model (UserLoginDto,用户检索条件参数)
	  * @return (接口的执行情况及对应用户信息)
	  */
   @LogSystem(target = "用户登录-政务中台", type = Constants.LOG_TYPE_LOGIN)
   @GetMapping("/zwzt/login")
   @ApiOperation(value = "用户登录认证及用户信息和token生成接口", notes = "{字段说明【\n" +
   		"】")
   public ExecuteResult<SSOZwztResultVo> loginByZwzt(@RequestParam(value = "code",required = true) String code) {
   	
   	return userSSOZwztVerifyService.singleSignOn(code);
   }
   
   /**
	  * @Title refreshToken 
	  * @Description (获取新token)
	  * @param username (用户名)
	  * @return (接口的执行情况及新token)
	  */
	 @GetMapping("/zwzt/refreshToken")
	 @ApiOperation(value = "获取新token-政务中台", notes = "{字段说明【\n" +
	 		"refreshToken（刷新token用于获取access_token）\n" +
	 		"】")
	 public ExecuteResult<SsoToken> refreshTokenByZwzt(@RequestParam(value = "refreshToken",required = true) String refreshToken) {
	 	
	 	return userSSOZwztVerifyService.refreshToken(refreshToken);
	 }
	 /**
	  * @Title ssoSyncOrg 
	  * @Description (机构同步接口V2.0-新版)
	  * @return (接口的执行情况及新token)
	  */
	 @PostMapping("/zwzt/ssoSyncOrg")
	 @ApiOperation(value = "机构同步接口V2.0-新版-政务中台", notes = "{字段说明【\n" +
	 		"】")
	 public Map<String,Object> ssoSyncOrg(SSOSyncOrgDto dto) {
		 Map<String,Object> res = new HashMap<String,Object>();
		 try {
			 ExecuteResult<String> result = userSSOZwztVerifyService.ssoSyncOrg(dto);
			 if("S".equalsIgnoreCase(result.getResultCode())) {
				 res.put("success", true);
				 res.put("message", "数据推送成功");
			 }else {
				 res.put("success", false);
				 res.put("message", "数据推送失败，详情："+result.getResultMsg());
			 }
		} catch (Exception e) {
			res.put("success", false);
			res.put("message", "数据推送异常，详情："+e.getMessage());
		}
		 return res;
	 }
	 /**
	  * @Title ssoSyncUser 
	  * @Description (用户信息同步接口V2.0-新版)
	  * @return (接口的执行情况及新token)
	  */
	 @PostMapping("/zwzt/ssoSyncUser")
	 @ApiOperation(value = "用户信息同步接口V2.0-新版-政务中台", notes = "{字段说明【\n" +
	 		"】")
	 public Map<String,Object> ssoSyncUser(SSOSyncUserDto dto) {
		 Map<String,Object> res = new HashMap<String,Object>();
		 try {
			 ExecuteResult<String> result = userSSOZwztVerifyService.ssoSyncUser(dto);
			 if("S".equalsIgnoreCase(result.getResultCode())) {
				 res.put("success", true);
				 res.put("message", "数据推送成功");
			 }else {
				 res.put("success", false);
				 res.put("message", "数据推送失败，详情："+result.getResultMsg());
			 }
		} catch (Exception e) {
			res.put("success", false);
			res.put("message", "数据推送异常，详情："+e.getMessage());
		}
		 return res;
	 }
	 
	 
	 /**
	  * @Title login 
	  * @Description (登录认证(含本地认证及单点登录逻辑)，并获取用户信息、用户角色信息、用户部门信息)
	  * @param model (UserLoginDto,用户检索条件参数)
	  * @return (接口的执行情况及对应用户信息)
	  */
    @LogSystem(target = "用户登录-海上福建", type = Constants.LOG_TYPE_LOGIN)
    @GetMapping("/hsfj/login")
    @ApiOperation(value = "用户登录认证及用户信息和token生成接口", notes = "{字段说明【\n" +
    		"】")
    public ExecuteResult<SSOHsfjResultVo> loginByHsfj(@RequestParam(value = "code",required = true) String code) {
    	
    	return userSSOHsfjVerifyService.singleSignOn(code);
    }
    
    /**
	  * @Title refreshToken 
	  * @Description (获取新token)
	  * @param username (用户名)
	  * @return (接口的执行情况及新token)
	  */
	 @GetMapping("/hsfj/refreshToken")
	 @ApiOperation(value = "获取新token-海上福建", notes = "{字段说明【\n" +
	 		"refreshToken（刷新token用于获取access_token）\n" +
	 		"】")
	 public ExecuteResult<AccessTokenModel> refreshTokenByHsfj(@RequestParam(value = "refreshToken",required = true) String refreshToken) {
	 	
	 	return userSSOHsfjVerifyService.refreshToken(refreshToken);
	 }
	 
	 /**
	  * @Title userLogout 
	  * @Description (登出系统)
	  * @param accessToken (用户token)
	  * @return (接口的执行情况)
	  */
	 @GetMapping("/hsfj/logout")
	 @ApiOperation(value = "登出系统-海上福建", notes = "{字段说明【\n" +
	 		"accessToken（用户token）\n" +
	 		"】")
	 public ExecuteResult<String> userLogoutByHsfj(@RequestParam(value = "accessToken",required = true) String accessToken) {
	 	
	 	return userSSOHsfjVerifyService.userLogout(accessToken);
	 }
   
}
