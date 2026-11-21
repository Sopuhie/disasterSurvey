package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.ZyFileModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.FileAddDto;
import com.santech.module.biz.entity.dto.FileDeleteDto;
import com.santech.module.biz.entity.dto.FileDetailsUpdateDto;
import com.santech.module.biz.entity.dto.FileResourceQueryDto;
import com.santech.module.biz.entity.dto.FileUpdateDto;
import com.santech.module.biz.entity.vo.ResourceStatisticsResultVo;

public interface FileResourceService {
	
	/**
	  * @Title queryFileResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询文件资源记录信息)
	  * @param query (FileResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<ZyFileModel>> queryFileResourceInfosWithPage(FileResourceQueryDto query);
	
	
	/**
	  * @Title queryFileDetailInfosById 
	  * @Description (根据服务ID查询文件资源记录以及包含的文件附件明细信息信息)
	  * @param fileResourceId (文件资源ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ZyFileModel> queryFileDetailInfosById(String fileResourceId);
	
	
	/**
	  * @Title saveFileResourceInfo 
	  * @Description (此为保存文件资源信息接口的业务层接口方法)
	  * @param query (ZyFileModel,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveFileResourceInfo(FileAddDto model);
	
	/**
	  * @Title updateFileResourceInfo
	  * @Description (此为更新文件资源信息接口的业务层接口方法)
	  * @param model (ZyFileModel,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateFileResourceInfo(FileUpdateDto model);
	
	/**
	  * @Title deleteFileResourceInfo 
	  * @Description (此为删除文件资源信息接口的业务层接口方法)
	  * @param model (FileDeleteModel 删除的目标文件参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteFileResourceInfo(FileDeleteDto model);
	/**
	  * @Title updateFileResourceDetailsInfo 
	  * @Description (此为为文件资源继续添加文件的接口的业务层接口方法)
	  * @param model (FileDetailsUpdateDto 保存新添加的附件信息类)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateFileResourceDetailsInfo(FileDetailsUpdateDto dto);
	/**
	  * @Title deleteFileDetailInfo 
	  * @Description (此为删除文件资源具体文件记录信息接口的业务层接口方法)
	  * @param model (FileDeleteModel 删除的目标文件参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteFileDetailInfo(FileDeleteDto model);
	
	/**
	  * @Title statisticsFileResourceForLeftTab
	  * @Description (此为文件资源按指定字段进行统计的接口的业务层接口方法)
	  * @param adminFlag (管理员标识)
	  * @param groupColumn (分组或统计字段名称)
	  * @param dicType (字典类型名称)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<List<ResourceStatisticsResultVo>> statisticsFileResourceForLeftTab(String adminFlag,String groupColumn,String dicType);
	
}
