package com.santech.module.biz.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.service.UserSSOSzfjVerifyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: UserSSOVerifyController.java
 * @Description：用户单点登录相关接口，单点登录分两种，分为海上福建版和政务中台版，根据客户要求，两者都要存在，
 * 根据不同的场景进行对应单点的切换，为了应付等保测评要求使用政务中台版，后续上线需要再切回海上福建版
 * @Version： V1.0
 * @Modify：
 */
@Controller
@RequestMapping("/user/verify")
@Api(tags = "Auth-福建省数字身份管理服务平台(IAM)单点登录认证信息相关功能接口")
public class UserFirstSSOVerifyController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserFirstSSOVerifyController.class);

    @Resource
    private UserSSOSzfjVerifyService userSSOSzfjVerifyService;
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("UserFirstSSOVerifyController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }


    /**
     * 接收授权码回调
     */
    @GetMapping(value = "/yjsso/ssoCode")
    @ApiOperation(value = "一级登录-接收授权码回调接口", notes = "{字段说明【\n" +
	 		"code授权码\n" +
	 		"】")
    public String ssoCode(@RequestParam String code,
                          @RequestParam(required = false) String state) throws Exception {
    	ExecuteResult<String> result = userSSOSzfjVerifyService.ssoCode(code);
        return result.getData();
    }

    /**
     * 根据 ticket 登录
     */
    @ResponseBody
    @LogSystem(target = "用户登录-yjsso", type = Constants.LOG_TYPE_LOGIN)
    @RequestMapping("/yjsso/ssoLogin")
    @ApiOperation(value = "一级登录-认证并获取登录用户信息", notes = "{字段说明【\n" +
	 		"ticket（票信息）\n" +
	 		"】")
    public ExecuteResult<QxUserSimpleModel> ssoLogin(@RequestParam(name = "ticket") String ticket,
                                   HttpServletRequest request) throws Exception {
       
        return userSSOSzfjVerifyService.ssoLogin(ticket);
    }
    
    /**
     * 系统登出
     */
    @GetMapping(value = "/yjsso/ssoLogout")
    @ApiOperation(value = "一级登录-系统登出接口", notes = "{字段说明【\n" +
	 		"ticket票信息\n" +
	 		"】")
    public String ssoLogout(@RequestParam String ticket) throws Exception {
    	ExecuteResult<String> result = userSSOSzfjVerifyService.ssoLogout(ticket);
        return result.getData();
    }
    
   
}
