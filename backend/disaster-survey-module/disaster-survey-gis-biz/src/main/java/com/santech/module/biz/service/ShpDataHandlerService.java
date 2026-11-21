package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ShpFileContentQueryDto;
import com.santech.module.biz.entity.dto.ShpFileExportImageDto;

public interface ShpDataHandlerService {
	
	/**
	  * @Title queryShpTableTargetRecordGemoetryByFid 
	  * @Description (此为根据条件查询shp文件属性记录信息接口的业务层方法，该方法仅返回属性记录的图形几何信息)
	  * @param dto (ShpFileContentQueryDto 查询参数信息)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<Map<String, Object>>> queryShpTableTargetRecordGemoetryByFid(ShpFileContentQueryDto dto);
	
    
	/**
	  * @Title getShpWms 
	  * @Description (shp图形数据快显接口)
	  */
    void getShpWms(HttpServletRequest request, HttpServletResponse response) throws Exception;
    
    /**
	  * @Title exportShpToImage 
	  * @Description (shp图形数据导出为图片接口)
	  * @param dto (ShpFileExportImageDto 参数信息)
	  * @return void
	  */
   void exportShpToImage(HttpServletResponse response,ShpFileExportImageDto dto);
    
    
	/**
	  * @Title queryShpTableTotalPropertiesRecord 
	  * @Description (此为根据条件查询shp文件属性记录信息接口的业务层方法，该方法返回全部属性字段信息，包含几何图形信息)
	  * @param dto (ShpFileContentQueryDto 查询参数信息)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<Map<Object, Object>> queryShpTableTotalPropertiesRecord(ShpFileContentQueryDto dto);
	

}
