package com.santech.module.biz.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.vo.ExposureTreeVo;
import com.santech.module.biz.service.ExposureDataService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Evaluate-承灾体灾情调查相关功能接口")
@RestController
@RequestMapping("/web-disaster-evaluate/web/disaster/evaluate/exposure")
public class ExposureDataController {
	private static final  Logger logger = LoggerFactory.getLogger(ExposureDataController.class);
    
	@Resource
    private ExposureDataService exposureDataService;
	

    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ExposureDataController接口程序中未能预见的异常信息，保证前端相应结果的一致性)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
   @ExceptionHandler(Exception.class)
   public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("ExposureDataController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
   }
   /**
	  * @Title queryDikeTreeInfos 
	  * @Description (根据承灾体类型查询承灾体树结构查询接口)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/tree")
	 @ApiOperation(value = "根据承灾体类型查询承灾体树结构信息接口", notes = "{字段说明【\n" +
			 "exposureType(承灾体类型，如Dike岸堤、Fishery渔港、CultureArea海水养殖区、TouristArea滨海旅游区，必传)\n" +
	         "】")
	 public ExecuteResult<List<ExposureTreeVo>> queryExposureTreeInfos(@RequestParam(value = "exposureType") String exposureType){
		 return exposureDataService.queryExposureTreeInfos(exposureType);
	 }

	 @GetMapping("/list")
	 @ApiOperation(value = "承灾体列表（字段对齐前端台账，无数据时前端回落 MOCK）", notes = "exposureType: Dike/Fishery/CultureArea/TouristArea 或 seawall/fishing/mariculture/tourist")
	 public ExecuteResult<List<Map<String, Object>>> queryExposureList(
			 @RequestParam(value = "exposureType") String exposureType,
			 @RequestParam(value = "keyword", required = false) String keyword) {
		 return exposureDataService.queryExposureList(exposureType, keyword);
	 }
}
