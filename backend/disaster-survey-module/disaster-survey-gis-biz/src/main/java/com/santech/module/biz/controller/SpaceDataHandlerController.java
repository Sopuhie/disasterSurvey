package com.santech.module.biz.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.JsonToShpDto;
import com.santech.module.biz.entity.vo.OverLayDataVo;
import com.santech.module.biz.service.SpaceDataHandlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: SpaceDataHandlerController.java
 * @Description：空间数据加工相关功能接口
 * @Version： V1.0
 * @Modify：
 */
@RestController
@RequestMapping("/web-disaster-map/web/disaster/map")
@Api(tags = "Gis-空间数据加工相关功能接口")
public class SpaceDataHandlerController {
    private static final Logger logger = LoggerFactory.getLogger(SpaceDataHandlerController.class);

    @Resource
    private SpaceDataHandlerService spaceDataHandlerService;

    /**
     * @param e(Exception,所有异常的父类异常)
     * @return (接口执行情况)
     * @Title exceptionHandler
     * @Description (用来捕获SpaceDataHandlerController接口程序中未能预见的异常信息 ， 保证前端响应结果的一致性 ， 避免无响应)
     */
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e) {
        logger.error("SpaceDataHandlerController中捕获的其他异常，异常详情：", e);
        return ExecuteResult.error(e.getMessage(), null);
    }


    /**
     * @Title getGeoJsonBySpaceDataFile
     * @Description (txt或shp或dwg转geojson接口)
     * @param spaceDataFiles (MultipartFile[],txt或shp或dwg或mdb或xls(xlsx)文件数据，可传多个)
     * @return (接口的执行情况)
     */
    @PostMapping("/spacedata/togeojson")
    @ApiOperation(value = "txt或shp或dwg转geojson接口")
    public ExecuteResult<List<OverLayDataVo>> getGeoJsonBySpaceDataFile(@RequestPart(value = "spaceDataFile") MultipartFile[] spaceDataFiles) {
    	
    	return spaceDataHandlerService.getGeoJsonBySpaceDataFile(spaceDataFiles);
    }
    
    /**
	 * @Title exportShpFile
	 * @Description (此方法用于将请求中的空间数据信息以shp导出)
	 * @param dto(JsonToShpDto 参数信息)
	 * @return (执行结果)
	 */
	  @PostMapping("/jsontoshp")
	  @ApiOperation(value = "将数据以shp导出接口", notes = "{字段说明【\n" +
	  		"】")
	  public void exportShpFile(HttpServletResponse response,@RequestBody JsonToShpDto dto){
		  
 		   spaceDataHandlerService.exportShpFile(response,dto);
	  }
	  
    
}
