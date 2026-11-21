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
import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.RoleAddDto;
import com.santech.module.biz.entity.dto.RoleQueryDto;
import com.santech.module.biz.entity.dto.RoleUpdateDto;
import com.santech.module.biz.service.RoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: RoleController.java
 * @Description：角色相关接口
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/role")
@Api(tags = "Auth-灾后调查评估系统项角色信息相关功能接口")
public class RoleController {
	
	private static final  Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Resource
    private RoleService roleService;
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("RoleController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
    
    
    /**
	  * @Title queryRolesWithPage 
	  * @Description (根据指定条件分页获取对应角色信息)
	  * @param query (RoleQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的角色记录信息)
	  */
   @ApiOperation(value = "根据指定条件分页获取对应角色信息接口", notes = "{字段说明【\n" +
   		"】")
   @PostMapping("/queryWithPage")
   public ExecuteResult<List<QxRoleModel>> queryRolesWithPage(HttpServletRequest request,@RequestBody RoleQueryDto query) {
	   
	   if(StringUtil.isEmpty(query.getAdminFlag())) {
		   String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		   query.setAdminFlag(adminFlag);
		}
       return roleService.queryRolesWithPage(query);
   }
   
   
   /**
	  * @Title saveRoleInfo 
	  * @Description (角色保存或新增接口)
	  * @param dto (RoleAddDto 新增角色所要保存的信息)
	  * @return (接口的执行情况)
	  */
   	 @LogSystem(target = "角色新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "角色保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveRoleInfo(HttpServletRequest request, @RequestBody RoleAddDto dto) {
   		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加角色", null);
		  }
		  if(StringUtil.isEmpty(dto.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  dto.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	      return roleService.saveRoleInfo(dto);
	 }
	 
	 /**
	  * @Title updateRoleInfo 
	  * @Description (角色保存或新增接口)
	  * @param dto (RoleUpdateDto 新增角色所要更新的信息)
	  * @return (接口的执行情况)
	  */
   	 @LogSystem(target = "角色更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "角色更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateRoleInfo(HttpServletRequest request, @RequestBody RoleUpdateDto dto) {
   		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改角色", null);
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
	      return roleService.updateRoleInfo(dto);
	 }
		/**
		 * @Title deleteRoleInfo
		 * @Description (此方法用于对角色进行删除)
		 * @param RoleId(功能项ID)
		 * @return (删除的执行结果)
		 */
   	  @LogSystem(target = "角色删除", type = Constants.LOG_TYPE_DELETE)
	  @GetMapping("/delete")
	  @ApiOperation(value = "角色删除接口", notes = "{字段说明【\n" +
	  		"roleId（角色id）\n" +
	  		"】")
	  public ExecuteResult<String> deleteRoleInfo(HttpServletRequest request,@RequestParam("roleId") String roleId){
   		  	String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
			String userName = "";
			try {
				userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return roleService.deleteRoleInfo(roleId, userName);
	  }
   
   
   
}
