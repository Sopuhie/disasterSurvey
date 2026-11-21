package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.JcActionModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ActionAddDto;
import com.santech.module.biz.entity.dto.ActionQueryDto;
import com.santech.module.biz.entity.dto.ActionUpdateDto;
/**
 * @Title: ActionService.java 
 * @Description：系统功能管理接口
 * @Version： V1.0
 * @Modify：   
 */
public interface ActionService {
	
	/**
	  * @Title queryActionInfos 
	  * @Description (根据筛选条件批量分页查询功能记录信息)
	  * @param query (ActionQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<JcActionModel>> queryActionInfos(ActionQueryDto query);
	/**
	  * @Title queryActionInfoByName 
	  * @Description (根据功能名称查询功能信息接口的业务层逻辑)
	  * @param actionName (功能名称)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<JcActionModel> queryActionInfoByName(String actionName);
	
	/**
	  * @Title saveActionInfo 
	  * @Description (此为保存功能项信息接口的业务层接口方法)
	  * @param dto (ActionAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveActionInfo(ActionAddDto dto);
	
	/**
	  * @Title updateActionInfo 
	  * @Description (此为更新功能项信息接口的业务层接口方法)
	  * @param dto (ActionUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateActionInfo(ActionUpdateDto dto);
	
	/**
	  * @Title deleteActionInfo 
	  * @Description (此为删除功能项信息接口的业务层接口方法)
	  * @param actionId (功能项id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteActionInfo(String actionId,String loginUser);
	
	/**
	  * @Title queryActionTreeInfos 
	  * @Description (根据权限查询系统功能信息并组装成树结构返回的接口业务层接口)
	  * @param adminFlag (管理员标识)
	  * @param loginUserId (当前登录用户用户ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<JcActionModel>> queryActionTreeInfos(String loginUserId,String adminFlag);
}
