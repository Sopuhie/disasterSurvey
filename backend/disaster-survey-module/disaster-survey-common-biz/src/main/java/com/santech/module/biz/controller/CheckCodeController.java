package com.santech.module.biz.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.ImageVerificationUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: CheckCodeController.java 
 * @Description：验证码相关接口 
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-common/web/disaster/common")
@Api(tags = "Common-验证码相关接口")
public class CheckCodeController {
	private static final  Logger logger = LoggerFactory.getLogger(CheckCodeController.class);
	
	@Autowired
    private RedisUtil redisUtil;
	
    /**
	 * @Title exceptionHandler
	 * @Description (用来捕获CheckCodeController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
	 * @param e (Exception,所有异常的父类异常)
	 * @return (接口执行情况)
	 */
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
    	logger.error("CheckCodeController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
	 /**
	  * @Title getVerifiCode 
	  * @Description (生成校验码，前端会传递key为requestCode的一个随机时间戳字段，
	  * 用来记录实时生成的校验码唯一标识，同时用来保证请求的不通，生成的校验码会存储到redis中，
	  * 用于后续验证，其在redis的存储有效期为6分钟)
	  * @return (接口的执行情况及校验码信息)
	  */
    @PostMapping("/checkcode/generate")
    @ApiOperation(value = "获取校验码接口", notes = "{字段说明【\n" +
            "】")
	public void getVerifiCode(HttpServletRequest request,HttpServletResponse response){
    	try {
			logger.info("生成验证码");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			// 生成随机字串
			String verifyCode = ImageVerificationUtil.generateVerifyCode(4);
			boolean result = redisUtil.set(request.getParameter("requestCode"),verifyCode,360);
			if(result) {
				// 生成图片
				int w = 100, h = 60;
				OutputStream out = response.getOutputStream();
				ImageVerificationUtil.outputImage(w, h, out, verifyCode);
			}
		}catch (Exception e) {
			logger.error("生成验证码出现异常，原因：【{}】,详情：", e.getMessage(), e);
		}
	}
}
