package com.santech.module.biz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.ZyThemeModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ThemeAddDto;
import com.santech.module.biz.entity.dto.ThemeUpdateDto;
import com.santech.module.biz.service.ThemeResourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/theme")
@Api(tags = "Resource-专题资源目录运维管理相关功能接口")
public class ThemeResourceController {
	
	public static final Logger logger = LoggerFactory.getLogger(ThemeResourceController.class);
	
	@Resource
    ThemeResourceService themeResourceService;
    
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ThemeResourceController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("ThemeResourceController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}
	 
	 
	 /**
	  * @Title queryThemeResourceInfoById 
	  * @Description (根据ID获取数据专题信息接口)
	  * @param themeId (专题ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getById")
	 @ApiOperation(value = "根据ID获取数据专题信息接口", notes = "{字段说明【\n" +
			 "themeId（专题资源id）\n" +
	         "】")
	 public ExecuteResult<ZyThemeModel> queryThemeResourceInfoById(@RequestParam("themeId") String themeId){
		 return themeResourceService.queryThemeResourceInfoById(themeId);
	 }
	 
	 /**
	  * @Title queryThemeResourceInfoByName 
	  * @Description (根据名称获取数据专题信息接口)
	  * @param themeName (专题名称)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getByName")
	 @ApiOperation(value = "根据专题名称获取数据专题信息接口", notes = "{字段说明【\n" +
			 "themeName（专题资源名称）\n" +
	         "】")
	 public ExecuteResult<ZyThemeModel> queryThemeResourceInfoByName(@RequestParam("themeName") String themeName){
		 return themeResourceService.queryThemeResourceInfoByName(themeName);
	 }
   
	 /**
	  * @Title saveThemeResourceInfo 
	  * @Description (数据专题资源保存或新增接口)
	  * @param model (ThemeAddDto 新增数据专题资源所要保存的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "数据专题资源新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "数据专题资源保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveThemeResourceInfo(HttpServletRequest request, @RequestBody ThemeAddDto model) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(StringUtil.isEmpty(model.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("【"+model.getCjr()+"】非管理员不能添加数据专题资源", null);
		  }
	      return themeResourceService.saveThemeResourceInfo(model);
	 }
   
	 /**
	  * @Title updateThemeResourceInfo 
	  * @Description (数据专题资源保存或新增接口)
	  * @param model (ThemeUpdateDto 新增数据专题资源所要更新的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "数据专题资源更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "数据专题资源更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateThemeResourceInfo(HttpServletRequest request, @RequestBody ThemeUpdateDto model) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改数据专题资源", null);
		  }
		  if(StringUtil.isEmpty(model.getZhgxr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setZhgxr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	      return themeResourceService.updateThemeResourceInfo(model);
	 }
	/**
	 * @Title deleteThemeResourceInfo
	 * @Description (此方法用于对数据专题资源进行删除)
	 * @param serviceId(数据专题资源ID)
	 * @return (删除的执行结果)
	 */
   @LogSystem(target = "数据专题资源删除", type = Constants.LOG_TYPE_DELETE)
   @GetMapping("/delete")
   @ApiOperation(value = "数据专题资源删除接口", notes = "{字段说明【\n" +
   		"themeId（数据专题资源id）\n" +
   		"】")
   public ExecuteResult<String> deleteThemeResourceInfo(HttpServletRequest request,@RequestParam("themeId") String themeId){
	   String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能删除数据专题资源", null);
		}
		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return themeResourceService.deleteThemeResourceInfo(themeId, userName);
   }

}
