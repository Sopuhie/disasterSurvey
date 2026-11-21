package com.santech.module.biz.survey.controller;

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
import com.santech.module.biz.survey.service.SurveyPipelineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 灾情调查 Step1：基础底数查询
 */
@Api(tags = "API-灾情调查-基础底数")
@RestController
@RequestMapping("/api/v1/survey/base")
public class SurveyBaseController {

	private static final Logger logger = LoggerFactory.getLogger(SurveyBaseController.class);

	@Resource
	private SurveyPipelineService surveyPipelineService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("SurveyBaseController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@GetMapping("/region-population")
	@ApiOperation("按区县/坐标查询行政区划与人口密度")
	public ExecuteResult<Map<String, Object>> regionPopulation(
			@RequestParam(value = "regionCode", required = false) String regionCode,
			@RequestParam(value = "regionName", required = false) String regionName,
			@RequestParam(value = "lon", required = false) Double lon,
			@RequestParam(value = "lat", required = false) Double lat) {
		return surveyPipelineService.queryRegionPopulation(regionCode, regionName, lon, lat);
	}

	@GetMapping("/exposures")
	@ApiOperation("按区县/类型查询四类承灾体基础台账（exposureType 使用 ExposureTypeEnum）")
	public ExecuteResult<Map<String, Object>> exposures(
			@RequestParam(value = "regionCode", required = false) String regionCode,
			@RequestParam(value = "regionName", required = false) String regionName,
			@RequestParam(value = "exposureType", required = false) String exposureType,
			@RequestParam(value = "keyword", required = false) String keyword) {
		return surveyPipelineService.queryBaseExposures(regionCode, regionName, exposureType, keyword);
	}
}
