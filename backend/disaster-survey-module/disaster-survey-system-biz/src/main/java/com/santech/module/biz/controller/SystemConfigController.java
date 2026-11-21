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
import com.santech.core.framework.entity.model.JcSystemConfigModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.SystemConfigAddDto;
import com.santech.module.biz.entity.dto.SystemConfigQueryDto;
import com.santech.module.biz.entity.dto.SystemConfigUpdateDto;
import com.santech.module.biz.service.SystemConfigService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/web-disaster-basic/web/disaster/basic/config")
@Api(tags = "System-系统基础配置管理相关功能接口")
public class SystemConfigController {

    public static final Logger logger = LoggerFactory.getLogger(SystemConfigController.class);

    @Resource
    private SystemConfigService systemConfigService;

    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获SystemConfigController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("SystemConfigController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
    }
    /**
	  * @Title saveSystemConfigInfo 
	  * @Description (系统基础配置保存或新增接口)
	  * @param dto (SystemConfigAddDto 新增系统基础配置所要保存的信息)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "系统配置项新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "系统基础配置保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveSystemConfigInfo(HttpServletRequest request, @RequestBody SystemConfigAddDto dto) {
    	 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加系统基础配置", null);
		  }
	      return systemConfigService.saveSystemConfigInfo(dto);
	 }
	 
	 /**
	  * @Title updateSystemConfigInfo 
	  * @Description (系统基础配置保存或新增接口)
	  * @param dto (SystemConfigUpdateDto 新增系统基础配置所要更新的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "系统配置项更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "系统基础配置更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateSystemConfigInfo(HttpServletRequest request, @RequestBody SystemConfigUpdateDto dto) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改系统基础配置", null);
		  }
		  if(StringUtil.isEmpty(dto.getZhgxr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  dto.setZhgxr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	      return systemConfigService.updateSystemConfigInfo(dto);
	 }
	/**
	 * @Title deleteSystemConfigInfo
	 * @Description (此方法用于对系统基础配置项进行删除)
	 * @param actionId(功能项ID)
	 * @return (删除的执行结果)
	 */
   @LogSystem(target = "系统配置项删除", type = Constants.LOG_TYPE_DELETE)
   @GetMapping("/delete")
   @ApiOperation(value = "系统基础配置项删除接口", notes = "{字段说明【\n" +
   		"systemConfigId（系统基础配置项id）\n" +
   		"】")
   public ExecuteResult<String> deleteSystemConfigInfo(HttpServletRequest request,@RequestParam("systemConfigId") String systemConfigId){
	    String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return systemConfigService.deleteSystemConfigInfo(systemConfigId, userName);
   }
	 
	 /**
	  * @Title querySystemConfigInfosWithPage 
	  * @Description (根据筛选条件批量分页查询系统基础配置列表信息)
	  * @param query (SystemConfigQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/queryWithPage")
	 @ApiOperation(value = "系统基础配置列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<JcSystemConfigModel>> querySystemConfigInfosWithPage(@RequestBody SystemConfigQueryDto query){
		 return systemConfigService.querySystemConfigInfosWithPage(query);
	 }
	 
	 /**
	  * @Title querySystemConfigInfoByName 
	  * @Description (根据系统配置名称或编码获取对应系统配置信息接口)
	  * @param configName (系统配置名称或编码)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getByName")
	 @ApiOperation(value = "根据系统配置名称或编码获取对应系统配置信息接口", notes = "{字段说明【\n" +
			 "configName（系统配置名称或编码）\n" +
	         "】")
	 public ExecuteResult<JcSystemConfigModel> querySystemConfigInfoByName(@RequestParam("configName") String configName){
		 return systemConfigService.querySystemConfigInfoByName(configName);
	 }
}
