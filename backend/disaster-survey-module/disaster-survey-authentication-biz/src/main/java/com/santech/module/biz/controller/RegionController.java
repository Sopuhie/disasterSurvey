package com.santech.module.biz.controller;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.aop.AsyncTask;
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.QxRegionModel;
import com.santech.core.framework.entity.model.QxRegionSimpleModel;
import com.santech.core.framework.entity.view.RegionQueryDto;
import com.santech.core.framework.service.RegionService;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: RegionController.java
 * @Description：行政区划相关接口
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@Api(tags = "Auth-灾后调查评估系统行政区划相关功能接口")
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/region")
public class RegionController {
	private static final Logger logger = LoggerFactory.getLogger(RegionController.class);
	@Resource
    private RedisUtil redisUtil;
	@Resource
    private RegionService regionService;
	@Resource
    private AsyncTask asyncTask;
	/**
	 * @Title exceptionHandler
	 * @Description (用来捕获RegionController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
	 * @param e(Exception,所有异常的父类异常)
	 * @return (接口执行情况)
	 */
	@ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
    	logger.error("RegionController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
    
	 /**
	  * @Title queryRegionTree
	  * @Description (行政区查询)
	  * @param request
	  * @param regionLevel 行政区级别，多个以英文逗号分割，如市级,区级,镇级,村级,其中只可以从尾递减不可去掉中间层级
	  * @return (接口执行情况)
	  */
	@GetMapping("/tree")
    @ApiOperation(value = "批量查询行政区并以树状机构返回的接口", notes = " regionLevel：行政区级别，多个以英文逗号分割，如市级,区级,镇级,村级,其中只可以从尾递减不可去掉中间层级")
    public ExecuteResult<List<QxRegionSimpleModel>> queryRegionTree(@RequestParam(value = "regionLevel",required = false)String regionLevel) {
		
		return regionService.showRegionTree(regionLevel);
    }
	
	/**
	  * @Title queryRegionWithPage
	  * @Description (分页查询行政区信息，不传分页查询全部)
	  * @param RegionQueryDto 过滤参数
	  * @return (接口执行情况)
	  */
	@PostMapping("/queryWithPage")
	@ApiOperation(value = "分页查询行政区信息", notes = "{字段说明【\n"
			+ "\n】")
	public ExecuteResult<List<QxRegionModel>> queryRegionWithPage(@RequestBody RegionQueryDto regionQueryModel){

		return regionService.queryRegionWithPage(regionQueryModel);
	}
	

	/**
	  * @Title saveRegionData
	  * @Description (新增或保存单条行政区信息)
	  * @param QxRegionModel 要保存的数据
	  * @return (接口执行情况)
	  */
	@LogSystem(target = "行政区划保存", type = Constants.LOG_TYPE_ADD)
	@PostMapping("/save")
	@ApiOperation(value = "保存行政区信息", notes = "{字段说明【\n"
			+ "\n】")
    public ExecuteResult<Integer> saveRegionData(@RequestBody QxRegionModel model){

		return  regionService.saveRegionData(model);
	}


	/**
	  * @Title deleteRegionData
	  * @Description (删除单条行政区信息)
	  * @param xzqdm 行政区划代码
	  * @return (接口执行情况)
	  */
	@LogSystem(target = "行政区划删除", type = Constants.LOG_TYPE_DELETE)
	@GetMapping("/delete")
	@ApiOperation(value = "删除行政区信息", notes = "{字段说明【\n"
			+ "xzqdm(行政区代码,必填)\n】")
	public ExecuteResult<String> deleteRegionData(@RequestParam(value = "xzqdm",required = true)String xzqdm){

		return regionService.deleteRegionData(xzqdm);
	}


}
