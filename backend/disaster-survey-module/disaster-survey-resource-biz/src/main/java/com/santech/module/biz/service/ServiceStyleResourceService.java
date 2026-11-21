package com.santech.module.biz.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.entity.model.ZyServiceStyleModel;
import com.santech.core.framework.entity.model.ZyServiceStyleSimpleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ServiceStyleAddDto;
import com.santech.module.biz.entity.dto.ServiceStyleResourceQueryDto;
import com.santech.module.biz.entity.dto.ServiceStyleUpdateDto;

public interface ServiceStyleResourceService {
	
	/**
	  * @Title queryServiceStyleResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询服务样式资源记录信息)
	  * @param query (ServiceStyleResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<ZyServiceStyleSimpleModel>> queryServiceStyleResourceInfosWithPage(ServiceStyleResourceQueryDto query);
	
	/**
	  * @Title queryServiceStyleResourceInfoById 
	  * @Description (根据服务样式ID查询对应服务样式信息)
	  * @param serviceStyleId (服务样式ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ZyServiceStyleModel> queryServiceStyleResourceInfoById(String serviceStyleId);
	
	/**
	  * @Title queryServiceStyleXmlInfoById 
	  * @Description (根据服务样式ID查询对应服务样式xml信息)
	  * @param serviceStyleId (服务样式ID)
	  * @return (接口的执行情况及符合条件记录的XML信息)
	  */
	ExecuteResult<String> queryServiceStyleXmlInfoById(String serviceStyleId);
	/**
	  * @Title queryServiceStyleXmlInfoByName 
	  * @Description (根据数据样式名称查询对应数据样式xml信息)
	  * @param styleName (数据样式名称)
	  * @return (接口的执行情况及符合条件记录的XML信息)
	  */
	ExecuteResult<String> queryServiceStyleXmlInfoByName(String styleName);
	/**
     * @Title uploadStyleXmlAndFormatCheck
	 * @Description (样式XML文件上传解析检查接口的业务层方法)
     * @param file (MultipartFile，上传的文件流信息)
     * @return (接口的执行情况及上传后的文件解析后的xml信息)
     */
	ExecuteResult<JSONObject> uploadStyleXmlAndFormatCheck(MultipartFile xmlfile);
	
	/**
	  * @Title saveServiceStyleResourceInfo 
	  * @Description (此为保存服务样式资源信息接口的业务层接口方法)
	  * @param query (ServiceStyleAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveServiceStyleResourceInfo(ServiceStyleAddDto model);
	
	/**
	  * @Title updateServiceStyleResourceInfo
	  * @Description (此为更新服务样式资源信息接口的业务层接口方法)
	  * @param model (ServiceStyleUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateServiceStyleResourceInfo(ServiceStyleUpdateDto model);
	
	/**
	  * @Title deleteServiceStyleResourceInfo
	  * @Description (此为删除服务样式资源信息接口的业务层接口方法)
	  * @param serviceStyleId (删除的目标样式ID)
	  * @param loginUser (当前登录人)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteServiceStyleResourceInfo(String loginUser,String serviceStyleId);
	
	

}
