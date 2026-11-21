package com.santech.module.biz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.core.framework.service.LogService;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.UserLoginDto;
import com.santech.module.biz.service.UserVerifyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: UserController.java
 * @Description：用户相关接口
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/user/verify")
@Api(tags = "Auth-灾后调查评估系统用户认证信息相关功能接口")
public class UserVerifyController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserVerifyController.class);

    @Resource
    private UserVerifyService userVerifyService;
    
  
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("UserVerifyController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }

    /**
	  * @Title login 
	  * @Description (登录认证(含本地认证及单点登录逻辑)，并获取用户信息、用户角色信息、用户部门信息)
	  * @param model (UserLoginDto,用户检索条件参数)
	  * @return (接口的执行情况及对应用户信息)
	  */
    @LogSystem(target = "用户登录", type = Constants.LOG_TYPE_LOGIN)
    @PostMapping("/login")
    @ApiOperation(value = "用户登录认证及用户信息和token生成接口", notes = "{字段说明【\n" +
    		"】")
    public ExecuteResult<Map<String,Object>> login(HttpServletRequest request,@RequestBody UserLoginDto model) {
    	//如下单点登录时会有值
    	String token =(String) request.getAttribute("token");//request.getHeader("token");
    	String username = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
    	String yzm = request.getHeader("yzm");
		String requestCode = request.getHeader("requestCode");
    	if(StringUtil.isNotEmpty(token)) {
    		model.setToken(token);
    	}
    	if(StringUtil.isNotEmpty(username)) {
    		try {
    			model.setUsername(URLDecoder.decode(username, Constants.UTF8));
    		} catch (UnsupportedEncodingException e) {
    			logger.error("从请求头中获取用户信息失败");
    		}
    	}
    	ExecuteResult<Map<String,Object>> result = null;
    	if(StringUtil.isEmpty(model.getPassword())&&StringUtil.isNotEmpty(token)) {
    		//单点登录，获取用户信息
    		result = userVerifyService.singleSignon(model);
    	}else{
			ExecuteResult<Map<String, Object>> checkResult = userVerifyService.checkVerificationCode(yzm, requestCode);
			if ("F".equals(checkResult.getResultCode())) {
				return checkResult;
			}
    		//本地登录，获取用户信息及token
    		result = userVerifyService.login(model);
    	}
    	return result;
    }
    
    /**
	  * @Title refreshToken 
	  * @Description (获取新token)
	  * @param username (用户名)
	  * @return (接口的执行情况及新token)
	  */
	 @GetMapping("/refreshToken")
	 @ApiOperation(value = "获取新token", notes = "{字段说明【\n" +
	 		"username（用户名）\n" +
	 		"】")
	 public ExecuteResult<String> refreshToken(@RequestParam(value = "username",required = true) String username) {
	 	
	 	return userVerifyService.refreshToken(username);
	 }
    /**
	  * @Title getUserInfosByRoleId 
	  * @Description (根据角色id获取对应授权用户)
	  * @param roleId (用户角色id)
	  * @return (接口的执行情况及角色对应的用户信息)
	  */
    @ApiOperation(value = "根据角色ID获取对应用户信息接口", notes = "{字段说明【\n" +
    		"roleId（角色ID）\n" +
    		"】")
    @GetMapping("/querybyroleid")
    public ExecuteResult<List<QxUserModel>> getUserInfosByRoleId(@RequestParam(value = "roleId",required = true) String roleId) {
        return userVerifyService.getUserInfosByRoleId(roleId);
    }
    
    /**
	  * @Title checkUserLogin 
	  * @Description (验证用户是否已登录系统)
	  * @param username (用户名)
	  * @return (接口的执行情况及检查结果)
	  */
	 @GetMapping("/check/login")
	 @ApiOperation(value = "验证用户是否已登录系统", notes = "{字段说明【\n" +
	 		"username（用户名）\n" +
	 		"】")
	 public ExecuteResult<JSONObject> checkUserLogin(@RequestParam(value = "username",required = true) String username) {
	 	
	 	return userVerifyService.checkUserLogin(username);
	 }
	 
	 /**
	  * @Title userLoginOut 
	  * @Description (登出系统)
	  * @param username (用户名)
	  * @return (接口的执行情况)
	  */
	 @GetMapping("/loginOut")
	 @ApiOperation(value = "登出系统", notes = "{字段说明【\n" +
	 		"username（用户名）\n" +
	 		"】")
	 public ExecuteResult<String> userLoginOut(@RequestParam(value = "username",required = true) String username) {
	 	
	 	return userVerifyService.userLoginOut(username);
	 }
    
   
   
}
