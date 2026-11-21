package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.ZyServiceModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ServiceAddDto;
import com.santech.module.biz.entity.dto.ServiceResourceQueryDto;
import com.santech.module.biz.entity.dto.ServiceUpdateDto;
import com.santech.module.biz.entity.vo.ResourceStatisticsResultVo;
import com.santech.module.biz.entity.vo.ServiceResourceTreeVo;

public interface ServiceResourceService {
	
	/**
	  * @Title queryServiceResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询数据服务资源记录信息)
	  * @param query (ServiceResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<ZyServiceModel>> queryServiceResourceInfosWithPage(ServiceResourceQueryDto query);
	
	/**
	  * @Title queryServiceResourceTreeInfos 
	  * @Description (根据筛选条件获取资源树信息信息，根据发布平台以及数据类型进行组织成树结构返回)
	  * @param query (ServiceResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ServiceResourceTreeVo> queryServiceResourceTreeInfos(ServiceResourceQueryDto query);
	
	/**
	  * @Title queryServiceResourceInfoById 
	  * @Description (根据服务ID查询数据服务资源记录信息)
	  * @param serviceId (服务ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ZyServiceModel> queryServiceResourceInfoById(String serviceId);
	
	/**
	  * @Title saveServiceResourceInfo 
	  * @Description (此为保存数据资源服务信息接口的业务层接口方法)
	  * @param dto (ServiceAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveServiceResourceInfo(ServiceAddDto dto);
	
	/**
	  * @Title updateServiceResourceInfo 
	  * @Description (此为更新数据服务资源信息接口的业务层接口方法)
	  * @param dto (ServiceUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateServiceResourceInfo(ServiceUpdateDto dto);
	
	/**
	  * @Title deleteServiceResourceInfo 
	  * @Description (此为删除数据服务资源信息接口的业务层接口方法)
	  * @param serviceId (数据服务资源id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteServiceResourceInfo(String serviceId,String loginUser);
	
	
	/**
	  * @Title updateMapServiceStatusInfo 
	  * @Description (此为数据服务状态巡检的业务层接口方法)
	  */
	void updateMapServiceStatusInfo();
	
	/**
	  * @Title statisticsServiceResourceForLeftTab
	  * @Description (此为数据服务资源按指定字段进行统计的接口的业务层接口方法，由于该统计的资源类型是配置在功能项中的，所以需要使用功能名称进行查询)
	  * @param actionName (功能名称)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<List<ResourceStatisticsResultVo>> statisticsServiceResourceForLeftTab(String actionName);


}
