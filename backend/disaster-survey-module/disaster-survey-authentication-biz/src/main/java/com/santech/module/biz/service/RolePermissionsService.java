package com.santech.module.biz.service;


import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.RolePermissionsQueryDto;
import com.santech.module.biz.entity.dto.RolePermissionsSaveDto;
import com.santech.module.biz.entity.vo.RolePermissionsManagerVo;
import com.santech.module.biz.entity.vo.RolePermissionsVo;

/**
 * @Title: RolePermissionsService.java 
 * @Description：角色权限业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface RolePermissionsService {
	
	/**
	  * @Title queryPermissionsByRoles 
	  * @Description (此为根据用户角色查询功能权限的接口。)
	  * @param query (RolePermissionsQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的角色记录信息)
	  */
	ExecuteResult<RolePermissionsVo> queryPermissionsByRoles(RolePermissionsQueryDto query);
	
	/**
	  * @Title queryPermissionsByRoleIdForManager 
	  * @Description (根据角色ID获取用户功能等权限信息的接口的业务层方法)
	  * @param roleId (角色ID)
	  * @return (接口的执行情况及符合条件的功能权限记录信息)
	  */
	ExecuteResult<RolePermissionsManagerVo> queryPermissionsByRoleIdForManager(String roleId);
	
	
	/**
	  * @Title saveRoleUserPermission 
	  * @Description (根据角色ID保存绑定的用户信息的接口的业务层方法)
	  * @param model (RolePermissionsSaveModel 对应参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveRoleUserPermission(RolePermissionsSaveDto model);
	
	
	/**
	  * @Title saveRoleActionPermission 
	  * @Description (根据角色ID保存绑定的系统功能信息的接口的业务层方法)
	  * @param model (RolePermissionsSaveModel 对应参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveRoleActionPermission(RolePermissionsSaveDto model);
	
	
	/**
	  * @Title saveRoleRouterPermission 
	  * @Description (根据角色ID保存绑定的运维路由信息的接口的业务层方法)
	  * @param model (RolePermissionsSaveModel 对应参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveRoleRouterPermission(RolePermissionsSaveDto model);
	
	
	/**
	  * @Title saveRoleCatalogPermission 
	  * @Description (根据角色ID保存绑定的资源目录信息的接口的业务层方法)
	  * @param model (RolePermissionsSaveModel 对应参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveRoleCatalogPermission(RolePermissionsSaveDto model);
	

}
