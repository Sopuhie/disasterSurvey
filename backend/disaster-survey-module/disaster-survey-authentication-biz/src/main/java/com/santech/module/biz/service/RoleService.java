package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.RoleAddDto;
import com.santech.module.biz.entity.dto.RoleQueryDto;
import com.santech.module.biz.entity.dto.RoleUpdateDto;

/**
 * @Title: RoleService.java 
 * @Description：角色业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface RoleService {
	
	/**
	  * @Title queryRolesWithPage 
	  * @Description (此为根据筛选条件进行批量查询角色信息接口的业务层方法，该方法会针对入参生成一条对应的查询sql，
	  * 以此sql进行记录的查询并返回。查询的记录默认按优先级字段升序返回，可分页查询。)
	  * @param query (RoleQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的角色记录信息)
	  */
	ExecuteResult<List<QxRoleModel>> queryRolesWithPage(RoleQueryDto query);
	
	/**
	  * @Title queryRolesByCondition 
	  * @Description (此为根据筛选条件进行批量查询角色信息接口的业务层方法，该方法会针对入参生成一条对应的查询sql，
	  * 以此sql进行记录的查询并返回。查询的记录默认按优先级字段升序返回。)
	  * @param query (RoleQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的角色记录信息)
	  */
	ExecuteResult<List<QxRoleModel>> queryRolesByCondition(RoleQueryDto query);
	
	/**
	  * @Title saveRoleInfo 
	  * @Description (此为保存角色信息接口的业务层接口方法)
	  * @param dto (RoleAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveRoleInfo(RoleAddDto dto);
	
	
	/**
	  * @Title updateRoleInfo 
	  * @Description (此为更新角色信息接口的业务层接口方法)
	  * @param dto (RoleUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateRoleInfo(RoleUpdateDto dto);
	
	/**
	  * @Title deleteRoleInfo 
	  * @Description (此为删除角色信息接口的业务层接口方法)
	  * @param roleId (角色id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteRoleInfo(String roleId,String loginUser);
	

}
