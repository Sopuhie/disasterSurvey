package com.santech.module.biz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

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
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ThemeResourceTreeQueryDto;
import com.santech.module.biz.entity.dto.ThemeServiceBindDto;
import com.santech.module.biz.entity.vo.ThemeResourceTreeVo;
import com.santech.module.biz.entity.vo.ThemeWithServiceLayersVo;
import com.santech.module.biz.service.ThemeServiceRelateService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/theme")
@Api(tags = "Resource-专题服务资源绑定运维管理相关功能接口")
public class ThemeServiceRelateController {
	
	public static final Logger logger = LoggerFactory.getLogger(ThemeServiceRelateController.class);
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ThemeServiceRelateController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("ThemeServiceRelateController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}
	
	
	@Resource
    ThemeServiceRelateService themeServiceRelateService;
	
	 /**
	  * @Title queryThemeResourceTreeInfos 
	  * @Description (数据专题资源树查询接口-不分页)
	  * @param query (ThemeResourceTreeQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/tree")
	 @ApiOperation(value = "数据专题资源树查询接口-不分页", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<ThemeResourceTreeVo>> queryThemeResourceTreeInfos(@RequestBody ThemeResourceTreeQueryDto query){
		 return themeServiceRelateService.queryThemeResourceTreeInfos(query);
	 }
	
	/**
	  * @Title serviceThemeBind 
	  * @Description (服务专题资源绑定接口)
	  * @param list (ThemeServiceBindDto 服务专题资源绑定所需的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "服务专题资源绑定", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/bind/service")
	 @ApiOperation(value = "服务专题资源绑定接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> serviceThemeBind(HttpServletRequest request, @RequestBody ThemeServiceBindDto model) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  String userName =model.getLoginUser();
		  if(StringUtil.isEmpty(userName)) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setLoginUser(userName);
			  } catch (UnsupportedEncodingException e) {
				  e.printStackTrace();
			  }
		  }
		  if(StringUtil.isEmpty(userName)) {
			  return ExecuteResult.error("用户未登录，不能进行数据专题的绑定", null);
		  }
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("【"+userName+"】非管理员不能进行数据专题资源的绑定", null);
		  }
	      return themeServiceRelateService.serviceThemeBind(model);
	 }
	 /**
	  * @Title serviceThemeUnBind 
	  * @Description (服务专题资源解绑接口，可以进行专题的批量解绑)
	  * @param list (List<ThemeServiceBindDto> 服务专题资源解绑所需的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "服务专题资源解绑", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/unbind/service")
	 @ApiOperation(value = "服务专题资源解绑接口-可进行批量专题服务的解绑", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> serviceThemeUnBind(HttpServletRequest request, @RequestBody List<ThemeServiceBindDto> models) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  String userName =null;
		  try {
			  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
			  userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		  } catch (UnsupportedEncodingException e) {
			  e.printStackTrace();
		  }
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("【"+userName+"】非管理员不能进行数据专题资源的解绑", null);
		  }
	      return themeServiceRelateService.serviceThemeUnBind(models);
	 }
	 
	 
	 /**
	  * @Title queryThemeResourceInfoById 
	  * @Description (根据专题id获取专题下的服务和图层接口)
	  * @param themeId (专题ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getLayersById")
	 @ApiOperation(value = "根据专题id获取专题下的服务和图层接口", notes = "{字段说明【\n" +
			 "themeId（专题资源id）\n" +
	         "】")
	 public ExecuteResult<List<ThemeWithServiceLayersVo>> queryThemeBindLayersInfoById(@RequestParam("themeId") String themeId){
		
		 return themeServiceRelateService.queryThemeBindLayersInfoById(themeId);
	 }
	 
}
