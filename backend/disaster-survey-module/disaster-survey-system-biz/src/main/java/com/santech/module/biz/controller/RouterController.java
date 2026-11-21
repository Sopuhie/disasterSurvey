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
import com.santech.core.framework.entity.model.JcRouterModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.RouterAddDto;
import com.santech.module.biz.entity.dto.RouterQueryDto;
import com.santech.module.biz.entity.dto.RouterUpdateDto;
import com.santech.module.biz.service.RouterService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-basic/web/disaster/basic/router")
@Api(tags = "System-运维路由菜单管理相关功能接口")
public class RouterController {
	private static final  Logger logger = LoggerFactory.getLogger(RouterController.class);
    @Resource
    private RouterService routerService;
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获RouterController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("RouterController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
    }
    /**
	  * @Title saveRouterInfo 
	  * @Description (运维路由菜单保存或新增接口)
	  * @param dto (RouterAddDto 新增运维路由菜单所要保存的信息)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "运维路由项新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "运维路由菜单保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveRouterInfo(HttpServletRequest request, @RequestBody RouterAddDto dto) {
    	 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(StringUtil.isEmpty(dto.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  dto.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		 
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("【"+dto.getCjr()+"】非管理员不能添加运维路由菜单", null);
		  }
	      return routerService.saveRouterInfo(dto);
	 }
	 
	 /**
	  * @Title updateRouterInfo 
	  * @Description (运维路由菜单保存或新增接口)
	  * @param dto (RouterUpdateDto 新增运维路由菜单所要更新的信息)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "运维路由项更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "运维路由菜单更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateRouterInfo(HttpServletRequest request, @RequestBody RouterUpdateDto dto) {
    	 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改运维路由菜单", null);
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
	      return routerService.updateRouterInfo(dto);
	 }
	/**
	 * @Title deleteRouterInfo
	 * @Description (此方法用于对运维路由菜单项进行删除)
	 * @param routerId(功能项ID)
	 * @return (删除的执行结果)
	 */
    @LogSystem(target = "运维路由项删除", type = Constants.LOG_TYPE_DELETE)
    @GetMapping("/delete")
    @ApiOperation(value = "运维路由菜单项删除接口", notes = "{字段说明【\n" +
    		"routerId（运维路由菜单项id）\n" +
    		"】")
    public ExecuteResult<String> deleteRouterInfo(HttpServletRequest request,@RequestParam("routerId") String routerId){
    	String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return routerService.deleteRouterInfo(routerId, userName);
    }
	 
	 /**
	  * @Title queryRouterInfosWithPage 
	  * @Description (根据筛选条件批量分页查询运维路由菜单列表信息)
	  * @param query (RouterQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/queryWithPage")
	 @ApiOperation(value = "运维路由菜单列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<JcRouterModel>> queryRouterInfosWithPage(HttpServletRequest request, @RequestBody RouterQueryDto query){
		 if(StringUtil.isEmpty(query.getAdminFlag())) {
			 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
			 query.setAdminFlag(adminFlag);
		 }
		 if(StringUtil.isEmpty(query.getLoginUserId())) {
			 String loginUserId = (String) request.getAttribute(Constants.HEADER_USERID);request.getHeader(Constants.HEADER_USERID);
			 query.setLoginUserId(loginUserId);
		 }
		 return routerService.queryRouterInfosWithPage(query);
	 }

	 
	 /**
	  * @Title queryRouterTreeInfos 
	  * @Description (根据权限查询运维路由菜单信息并组装成树结构返回的接口)
	  * @return (接口的执行情况及运维路由菜单树信息)
	  */
	 @GetMapping("/tree")
	 @ApiOperation(value = "根据权限查询运维路由菜单信息并组装成树结构返回的接口", notes = "{字段说明【\n" +
	   		"】")
	 public ExecuteResult<List<JcRouterModel>> queryRouterTreeInfos(HttpServletRequest request) {
		String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		String loginUserId = (String) request.getAttribute(Constants.HEADER_USERID);request.getHeader(Constants.HEADER_USERID);
	   return routerService.queryRouterTreeInfos(loginUserId,adminFlag);
	 }


}
