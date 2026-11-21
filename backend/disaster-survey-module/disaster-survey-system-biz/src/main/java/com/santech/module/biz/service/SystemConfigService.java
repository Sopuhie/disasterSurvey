package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.JcSystemConfigModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.SystemConfigAddDto;
import com.santech.module.biz.entity.dto.SystemConfigQueryDto;
import com.santech.module.biz.entity.dto.SystemConfigUpdateDto;
/**
 * @Title: SystemConfigService.java 
 * @Description：系统配置管理接口
 * @Version： V1.0
 * @Modify：   
 */
public interface SystemConfigService {

	/**
	  * @Title saveSystemConfigInfo 
	  * @Description (此为保存系统基础配置信息接口的业务层接口方法)
	  * @param dto (SystemConfigAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveSystemConfigInfo(SystemConfigAddDto dto);
	
	
	/**
	  * @Title updateSystemConfigInfo 
	  * @Description (此为更新系统基础配置信息接口的业务层接口方法)
	  * @param dto (SystemConfigUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateSystemConfigInfo(SystemConfigUpdateDto dto);
	
	/**
	  * @Title deleteSystemConfigInfo 
	  * @Description (此为删除系统基础配置信息接口的业务层接口方法)
	  * @param systemConfigId (系统基础配置id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteSystemConfigInfo(String systemConfigId,String loginUser);
	
	/**
	  * @Title querySystemConfigInfosWithPage 
	  * @Description (根据筛选条件批量分页查询系统基础配置记录信息,不传分页信息会获取全部)
	  * @param query (SystemConfigQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<JcSystemConfigModel>> querySystemConfigInfosWithPage(SystemConfigQueryDto query);
	
	/**
	  * @Title querySystemConfigInfoByName 
	  * @Description (根据系统配置名称查询系统基础配置记录信息)
	  * @param configName (系统配置名称)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<JcSystemConfigModel> querySystemConfigInfoByName(String configName);
}
