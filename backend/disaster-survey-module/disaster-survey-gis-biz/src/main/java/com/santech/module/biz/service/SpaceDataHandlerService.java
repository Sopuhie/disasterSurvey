package com.santech.module.biz.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.JsonToShpDto;
import com.santech.module.biz.entity.vo.OverLayDataVo;
/**
 * @Title: SpaceDataHandlerService.java 
 * @Description：空间数据加工相关功能接口的业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface SpaceDataHandlerService {
	
    /**
     * @Title publishShpMapServiceByGeoServer
     * @Description (txt或shp或dwg或mdb或xls(xlsx)转geojson接口的业务层定义)
     * @param spaceDataFile (MultipartFile,txt或shp或dwg或mdb或xls(xlsx)的空间文件数据)
     * @return (接口的执行情况)
     */
	ExecuteResult<List<OverLayDataVo>> getGeoJsonBySpaceDataFile(MultipartFile[] spaceDataFiles);
	
	

	/**
     * @Title exportShpFile
	 * @Description (此为将请求中的空间数据信息以shp导出接口对应的业务层方法)
     * @param dto(JsonToShpDto 参数信息)
     * @return (接口的执行情况及文件信息)
     */
	void exportShpFile(HttpServletResponse response, JsonToShpDto dto);
}
