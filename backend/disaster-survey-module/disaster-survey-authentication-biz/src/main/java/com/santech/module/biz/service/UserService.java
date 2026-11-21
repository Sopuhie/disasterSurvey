package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.UserAddDto;
import com.santech.module.biz.entity.dto.UserPasswordChangeDto;
import com.santech.module.biz.entity.dto.UserQueryDto;
import com.santech.module.biz.entity.dto.UserUpdateDto;
import com.santech.module.biz.entity.vo.UserGroupByDeptVo;

/**
 * @Title: UserService.java 
 * @Description：用户业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface UserService {

	/**
	 * @Title addUser
	 * @Description (此方法用于添加用户信息)
	 * @param dto(UserAddDto,用户信息)
	 * @return (添加执行结果)
	 */
	ExecuteResult<String> addUser(UserAddDto dto);
	/**
	 * @Title changePasswordForFirstLogin
	 * @Description (此方法用于用户初次登录时的密码修改)
	 * @param model(UserPasswordChangeModel,用户密码更改信息)
	 * @return (执行结果)
	 */
	ExecuteResult<String> changePasswordForFirstLogin(UserPasswordChangeDto model);
	/**
	 * @Title deleteUser
	 * @Description (此方法用于对用户进行删除，为假删除)
	 * @param userid(删除的用户ID)
	 * @param loginUser(操作人的用户ID)
	 * @return (删除执行结果)
	 */
	ExecuteResult<String> deleteUser(String userid,String loginUser);
	
	
	/**
	 * @Title updateUser
	 * @Description (此方法用于对用户进行修改)
	 * @param dto(UserUpdateDto,用户信息)
	 * @return (更新执行结果)
	 */
	ExecuteResult<String> updateUser(UserUpdateDto dto);
	
	
	/**
	 * @Title queryUserInfosWithPage
	 * @Description (此为用户信息查询接口的具体业务逻辑处理方法，该方法可根据检索条件对符合条件的用户信息进行查询，
	 * 如果不传检索条件，默认查询全部，如果传分页信息，可对用户信息进行分页查询)
	 * @param model(QxUserSimpleModel 用户查询检索条件信息)
	 * @return (符合检索条件的用户信息)
	 */
	ExecuteResult<List<QxUserSimpleModel>> queryUserInfosWithPage(UserQueryDto model);
	
	/**
	 * @Title queryUserInfosGroupByDept
	 * @Description (此为用户信息查询接口的具体业务逻辑处理方法，该方法可根据检索条件对符合条件的用户信息进行查询，
	 * 如果不传检索条件，默认查询全部，不能进行分页查询，且查询的结果不含角色信息)
	 * @return (符合检索条件的用户信息)
	 */
	ExecuteResult<List<UserGroupByDeptVo>> queryUserInfosGroupByDept();
	
	/**
	  * @Title queryUserInfoById 
	  * @Description (此为根据ID获取用户信息接口的具体业务逻辑处理方法)
	  * @param userId (用户ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<QxUserModel> queryUserInfoById(String userId);
	
	
}
