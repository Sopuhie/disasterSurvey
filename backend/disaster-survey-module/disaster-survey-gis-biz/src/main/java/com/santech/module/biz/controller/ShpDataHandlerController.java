package com.santech.module.biz.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ShpFileContentQueryDto;
import com.santech.module.biz.entity.dto.ShpFileExportImageDto;
import com.santech.module.biz.service.ShpDataHandlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/web-santech-shp/web/santech/shp/data")
@Api(tags = "Gis-Shp文件数据处理相关接口")
public class ShpDataHandlerController {
	private static final Logger logger = LoggerFactory.getLogger(ShpDataHandlerController.class);

	@Resource
    private ShpDataHandlerService shpDataHandlerService;

    /**
     * @param e(Exception,所有异常的父类异常)
     * @return (接口执行情况)
     * @Title exceptionHandler
     * @Description (用来捕获ShpDataHandlerController接口程序中未能预见的异常信息 ， 保证前端响应结果的一致性 ， 避免无响应)
     */
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e) {
        logger.error("ShpDataHandlerController中捕获的其他异常，异常详情：", e);
        return ExecuteResult.error(e.getMessage(), null);
    }
    
    
    /**
     * @Title queryShpTableTargetRecordGemoetryByFid
     * @Description (此为根据ID查询shp文件目标属性记录的几何图形信息接口。)
     * @param dto (ShpFileContentQueryDto 查询参数信息)
     * @return (接口的执行情况及符合条件的数据源字段接口信息)
     */
    @PostMapping("/getGemoetry")
    @ApiOperation(value = "根据ID查询shp文件目标属性记录的几何图形信息接口", notes = "{字段说明【  \n" +
            "】}")
    public ExecuteResult<List<Map<String, Object>>> queryShpTableTargetRecordGemoetryByFid(@RequestBody ShpFileContentQueryDto dto){
        return shpDataHandlerService.queryShpTableTargetRecordGemoetryByFid(dto);
    }
    
    /**
     * @Title wms
     * @Description (此为根据Shp文件路径预览shp图层的wms服务接口。)
     * @return
     */
    @GetMapping("/wms")
    @ApiOperation(value = "预览shp图层的wms服务接口")
    public void wms(HttpServletRequest request, HttpServletResponse response) throws Exception {

    	shpDataHandlerService.getShpWms(request, response);
    }
    
    /**
     * @Title exportShpToImage
     * @Description (此为将shp图层以图片格式导出接口。)
     * @return
     */
    @PostMapping("/exportImage")
    @ApiOperation(value = "将shp图层以图片格式导出接口")
    public void exportShpToImage(HttpServletResponse response,@RequestBody ShpFileExportImageDto dto) {

    	shpDataHandlerService.exportShpToImage(response, dto);
    }
    
    
    /**
     * @Title queryShpTableTotalPropertiesRecord
     * @Description (此为根据Shp文件路径查询shp文件全量记录信息接口（含几何图形）。)
     * @param dto (ShpFileContentQueryDto 查询参数信息)
     * @return (接口的执行情况及符合条件的数据源属性记录信息)
     */
    @PostMapping("/getTotalProperties")
    @ApiOperation(value = "查询shp文件全量属性记录信息接口（含几何图形）", notes = "{响应字段说明【  \n" +
    		"fields（shp包含的字段信息）\n" +
    		"features（要素记录信息）\n" +
    		"wkid（空间参考信息）\n" +
    		"fileName（图层名称）\n" +
    		"】}")
    public ExecuteResult<Map<Object, Object>> queryShpTableTotalPropertiesRecord(@RequestBody ShpFileContentQueryDto dto){
        return shpDataHandlerService.queryShpTableTotalPropertiesRecord(dto);
    }
	  

}
