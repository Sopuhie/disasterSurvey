package com.santech.module.biz.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.santech.core.framework.entity.model.MappingLogModel;
import com.santech.core.framework.entity.model.MappingTemplateModel;
import com.santech.core.framework.entity.view.UploadFileVo;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.MappingLogAddDto;
import com.santech.module.biz.entity.dto.MappingLogQueryDto;
import com.santech.module.biz.entity.dto.MappingTemplateAddDto;
import com.santech.module.biz.entity.dto.MappingTemplateResourceQueryDto;
import com.santech.module.biz.entity.dto.MappingTemplateUpdateDto;

public interface MappingTemplateService {
	
	/**
	  * @Title queryMappingTemplateResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询专题出图模板资源记录信息)
	  * @param query (MappingTemplateResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<MappingTemplateModel>> queryMappingTemplateResourceInfosWithPage(MappingTemplateResourceQueryDto query);
	
	/**
	  * @Title saveMappingTemplateResourceInfo 
	  * @Description (此为保存专题出图模板资源信息接口的业务层接口方法)
	  * @param query (MappingTemplateAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveMappingTemplateResourceInfo(MappingTemplateAddDto model);
	
	/**
	  * @Title updateMappingTemplateResourceInfo
	  * @Description (此为更新专题出图模板资源信息接口的业务层接口方法)
	  * @param model (MappingTemplateUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateMappingTemplateResourceInfo(MappingTemplateUpdateDto model);
	
	/**
	  * @Title deleteMappingTemplateResourceInfo
	  * @Description (此为删除专题出图模板资源信息接口的业务层接口方法)
	  * @param apiId (删除的目标专题出图模板ID)
	  * @param loginUser (当前登录人)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteMappingTemplateResourceInfo(String loginUser,String templateId);
	
	
	/**
     * @Title symbolFilesUpload
	 * @Description (此为符号图片上传接口对应的业务层方法)
     * @param file(MultipartFile 上传的符号图片)
     * @param typeName(标注分类名称，如symbol_dot(点符号),symbol_compass（指南针符号）)
     * @return (接口的执行情况及文件信息)
     */
	ExecuteResult<UploadFileVo> symbolFilesUpload(MultipartFile file,String typeName);
	
	/**
     * @Title uploadFile
	 * @Description (此为根据文件夹名称获取其内文件路径的业务层方法)
     * @param folderName(目标文件夹名称，如symbol_dot(点符号),symbol_compass（指南针符号）)
     * @return (接口的执行情况及文件路径信息)
     */
	ExecuteResult<List<String>> getFilePathInfoByFolder(String folderName);
	
	/**
     * @Title deleteSymbolFileByFilePath
	 * @Description (此为根据符号文件路径删除目标文件的业务层方法)
     * @param symbolFilePath(目标文件路径)
     * @return (接口的执行情况)
     */
	ExecuteResult<String> deleteSymbolFileByFilePath(String symbolFilePath);
	
	 /**
	  * @Title queryLogInfos 
	  * @Description (根据筛选条件批量查询日志信息，可分页)
	  * @param query (MappingLogQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<MappingLogModel>> queryLogInfos(MappingLogQueryDto query);
  
	/**
	  * @Title saveLogInfo 
	  * @Description (日志保存或新增接口的业务逻辑)
	  * @param model (MappingLogAddDto 所要保存的日志信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveLogInfo(MappingLogAddDto model);

}
