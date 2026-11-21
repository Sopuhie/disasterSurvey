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
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.RolePermissionsQueryDto;
import com.santech.module.biz.entity.dto.RolePermissionsSaveDto;
import com.santech.module.biz.entity.vo.RolePermissionsManagerVo;
import com.santech.module.biz.entity.vo.RolePermissionsVo;
import com.santech.module.biz.service.RolePermissionsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: RolePermissionsController.java
 * @Description：角色权限相关接口
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/permission")
@Api(tags = "Auth-灾后调查评估系统角色权限相关功能接口")
public class RolePermissionsController {
	
	private static final  Logger logger = LoggerFactory.getLogger(RolePermissionsController.class);

    @Resource
    private RolePermissionsService rolePermissionsService;
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("RolePermissionsController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
    
    
    /**
	  * @Title queryPermissionsByRoles 
	  * @Description (根据指定条件获取用户功能权限信息的接口)
	  * @param query (RolePermissionsQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的功能权限记录信息)
	  */
    @ApiOperation(value = "根据指定条件获取用户功能权限信息的接口-用于客户端的权限", notes = "")
    @PostMapping("/client/query")
    public ExecuteResult<RolePermissionsVo> queryPermissionsByRoles(HttpServletRequest request,@RequestBody RolePermissionsQueryDto query) {
    	  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(StringUtil.isEmpty(query.getAdminFlag())) {
			  query.setAdminFlag(adminFlag);
		  }
    	return rolePermissionsService.queryPermissionsByRoles(query);
    }
    
    /**
	  * @Title queryPermissionsByRoleForManager 
	  * @Description (根据角色ID获取用户功能等权限信息的接口)
	  * @param roleId (角色ID)
	  * @return (接口的执行情况及符合条件的功能权限记录信息)
	  */
   @ApiOperation(value = "根据角色ID获取用户功能权限信息的接口-用于运维的角色权限绑定", notes = "")
   @GetMapping("/manager/query")
   public ExecuteResult<RolePermissionsManagerVo> queryPermissionsByRoleIdForManager(@RequestParam("roleId") String roleId) {
       return rolePermissionsService.queryPermissionsByRoleIdForManager(roleId);
   }
   
   /**
	  * @Title saveRoleUserPermission 
	  * @Description (保存角色用户关联关系接口)
	  * @param query (RolePermissionsSaveModel,入参)
	  * @return (接口的执行情况)
	  */
   @LogSystem(target = "角色用户绑定", type = Constants.LOG_TYPE_ADD)
   @ApiOperation(value = "保存角色用户关联关系接口", notes = "")
   @PostMapping("/roleuser/save")
   public ExecuteResult<String> saveRoleUserPermission(HttpServletRequest request,@RequestBody RolePermissionsSaveDto model) {
	    String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			return ExecuteResult.error("非管理员不能调整授权关系", null);
		}
	   if(StringUtil.isEmpty(model.getLoginUser())) {
		  try {
			  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
			  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
			  model.setLoginUser(userName);
		  } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		  }
	  }
      return rolePermissionsService.saveRoleUserPermission(model);
   }
  /**
	  * @Title saveRoleActionPermission 
	  * @Description (保存角色功能关联关系接口)
	  * @param query (RolePermissionsSaveModel,入参)
	  * @return (接口的执行情况)
	  */
    @LogSystem(target = "角色功能权限绑定", type = Constants.LOG_TYPE_ADD)
	@ApiOperation(value = "保存角色功能关联关系接口", notes = "")
	@PostMapping("/roleaction/save")
	public ExecuteResult<String> saveRoleActionPermission(HttpServletRequest request,@RequestBody RolePermissionsSaveDto model) {
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		 if(!"1".equals(adminFlag)) {
			 return ExecuteResult.error("非管理员不能调整前端功能权限", null);
		 }
    	if(StringUtil.isEmpty(model.getLoginUser())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setLoginUser(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	   return rolePermissionsService.saveRoleActionPermission(model);
	}
	
	/**
	  * @Title saveRoleRouterPermission 
	  * @Description (保存角色用户关联关系接口)
	  * @param query (RolePermissionsSaveModel,入参)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "角色运维路由权限绑定", type = Constants.LOG_TYPE_ADD)
	 @ApiOperation(value = "保存角色运维路由关联关系接口", notes = "")
	 @PostMapping("/rolerouter/save")
	 public ExecuteResult<String> saveRoleRouterPermission(HttpServletRequest request,@RequestBody RolePermissionsSaveDto model) {
    	 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能调整运维路由权限", null);
		  }
    	 if(StringUtil.isEmpty(model.getLoginUser())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setLoginUser(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	     return rolePermissionsService.saveRoleRouterPermission(model);
	 }
	 
	 /**
	  * @Title saveRoleCatalogPermission 
	  * @Description (保存角色资源目录关联关系接口)
	  * @param query (RolePermissionsSaveModel,入参)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "角色数据目录权限绑定", type = Constants.LOG_TYPE_ADD)
	  @ApiOperation(value = "保存角色资源目录关联关系接口", notes = "")
	  @PostMapping("/rolecatalog/save")
	  public ExecuteResult<String> saveRoleCatalogPermission(HttpServletRequest request,@RequestBody RolePermissionsSaveDto model) {
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能调整目录权限", null);
		  }
		  if(StringUtil.isEmpty(model.getLoginUser())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setLoginUser(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	      return rolePermissionsService.saveRoleCatalogPermission(model);
	  }
   
   
}
