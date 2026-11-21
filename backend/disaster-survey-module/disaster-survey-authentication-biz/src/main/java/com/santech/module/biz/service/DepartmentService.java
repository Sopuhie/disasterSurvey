package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.DepartmentAddDto;
import com.santech.module.biz.entity.dto.DepartmentUpdateDto;
import com.santech.module.biz.entity.dto.DeptQueryDto;

/**
 * @Title: DepartmentService.java 
 * @Description：部门业务层接口类
 * @Version： V1.0
 * @Modify：   
 */
public interface DepartmentService {

	/**
	 * @Title queryDepartmentInfos
	 * @Description (此为部门信息查询接口的具体业务逻辑处理方法，该方法可根据检索条件对符合条件的部门信息进行查询，
	 * 如果不传检索条件，默认查询全部，如果传分页信息，可对部门信息进行分页查询)
	 * @param model(DeptQueryDto 检索条件信息)
	 * @return (符合检索条件的部门信息)
	 */
	ExecuteResult<List<QxDepartmentModel>> queryDepartmentInfos(DeptQueryDto model);
	
	/**
	 * @Title queryDepartmentTreeInfos
	 * @Description (此为部门树查询接口的具体业务逻辑处理方法，该方法通过全量查询有效的部门信息，以树状结构进行部门信息返回)
	 * @return (部门树信息)
	 */
	ExecuteResult<List<QxDepartmentModel>> queryDepartmentTreeInfos();
	
	/**
	 * @Title addDepartment
	 * @Description (此方法用于添加部门信息)
	 * @param dto(DepartmentAddDto,部门信息)
	 * @return (添加执行结果)
	 */
	ExecuteResult<String> addDepartment(DepartmentAddDto dto);
	
	
	/**
	 * @Title deleteDepartment
	 * @Description (此方法用于对部门进行删除)
	 * @param departmentId(部门ID)
	 * @param loginUser(当前登录人)
	 * @return (删除的执行结果)
	 */
	ExecuteResult<String> deleteDepartment(String departmentId,String loginUser);
	
	
	/**
	 * @Title updateDepartment
	 * @Description (此方法用于对部门信息进行修改)
	 * @param dto(DepartmentUpdateDto,部门信息)
	 * @return (更新的执行结果)
	 */
	ExecuteResult<String> updateDepartment(DepartmentUpdateDto dto);
	

}
