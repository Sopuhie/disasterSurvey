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
import com.santech.core.framework.entity.model.JcActionModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ActionAddDto;
import com.santech.module.biz.entity.dto.ActionQueryDto;
import com.santech.module.biz.entity.dto.ActionUpdateDto;
import com.santech.module.biz.service.ActionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/web-disaster-basic/web/disaster/basic/action")
@Api(tags = "System-功能菜单运维管理相关功能接口")
public class ActionController {
	private static final  Logger logger = LoggerFactory.getLogger(ActionController.class);
    
	@Resource
    private ActionService actionService;
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ActionController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
   @ExceptionHandler(Exception.class)
   public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("ActionController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
   }
    
    
   	/**
	  * @Title queryActionInfos 
	  * @Description (根据筛选条件批量分页查询功能列表信息)
	  * @param query (ActionQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/queryWithPage")
	 @ApiOperation(value = "功能列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<JcActionModel>> queryActionInfos(HttpServletRequest request, @RequestBody ActionQueryDto query){
		 if(StringUtil.isEmpty(query.getAdminFlag())) {
			 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
			 query.setAdminFlag(adminFlag);
		 }
		 if(StringUtil.isEmpty(query.getLoginUserId())) {
			 String loginUserId = (String) request.getAttribute(Constants.HEADER_USERID);//request.getHeader(Constants.HEADER_USERID);
			 query.setLoginUserId(loginUserId);
		 }
		 return actionService.queryActionInfos(query);
	 }
	 /**
	  * @Title queryActionInfoByName 
	  * @Description (根据功能名称查询功能信息)
	  * @param actionName (功能名称)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getByName")
	 @ApiOperation(value = "根据功能名称查询功能记录接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<JcActionModel> queryActionInfoByName(@RequestParam("actionName") String actionName){
		 return actionService.queryActionInfoByName(actionName);
	 }
	 
	 /**
	  * @Title saveActionInfo 
	  * @Description (功能保存或新增接口)
	  * @param dto (ActionAddDto 新增功能项所要保存的信息)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "功能项新增", type = Constants.LOG_TYPE_ADD)
	  @PostMapping("/add")
	  @ApiOperation(value = "功能保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	  public ExecuteResult<String> saveActionInfo(HttpServletRequest request, @RequestBody ActionAddDto dto) {
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		   if(!"1".equals(adminFlag)) {
			   return ExecuteResult.error("非管理员不能添加功能菜单", null);
		   }
	       return actionService.saveActionInfo(dto);
	  }
	  
	  
	 /**
	  * @Title updateActionInfo 
	  * @Description (功能项更新接口)
	  * @param dto (ActionUpdateDto 功能项所要更新的信息)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "功能项更新", type = Constants.LOG_TYPE_UPDATE)
	  @PostMapping("/update")
	  @ApiOperation(value = "功能项更新接口", notes = "{字段说明【\n" +
	           "】")
	  public ExecuteResult<String> updateActionInfo(HttpServletRequest request, @RequestBody ActionUpdateDto dto) {
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		   if(!"1".equals(adminFlag)) {
			   return ExecuteResult.error("非管理员不能修改功能菜单", null);
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
	       return actionService.updateActionInfo(dto);
	  }
	  
    /**
	 * @Title deleteActionInfo
	 * @Description (此方法用于对功能项进行删除)
	 * @param actionId(功能项ID)
	 * @return (删除的执行结果)
	 */
	@LogSystem(target = "功能项删除", type = Constants.LOG_TYPE_DELETE)
    @GetMapping("/delete")
    @ApiOperation(value = "功能项删除接口", notes = "{字段说明【\n" +
    		"actionId（功能项id）\n" +
    		"】")
    public ExecuteResult<String> deleteActionInfo(HttpServletRequest request,@RequestParam("actionId") String actionId){
		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return actionService.deleteActionInfo(actionId, userName);
    }
    
    
    /**
	  * @Title queryRouterTreeInfos 
	  * @Description (根据权限查询系统功能信息并组装成树结构返回的接口)
	  * @return (接口的执行情况及系统功能树信息)
	  */
	 @GetMapping("/tree")
	 @ApiOperation(value = "根据权限查询系统功能信息并组装成树结构返回的接口", notes = "{字段说明【\n" +
	   		"】")
	 public ExecuteResult<List<JcActionModel>> queryActionTreeInfos(HttpServletRequest request) {
		String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		String loginUserId = (String) request.getAttribute(Constants.HEADER_USERID);//request.getHeader(Constants.HEADER_USERID);
	   return actionService.queryActionTreeInfos(loginUserId,adminFlag);
	 }

}
