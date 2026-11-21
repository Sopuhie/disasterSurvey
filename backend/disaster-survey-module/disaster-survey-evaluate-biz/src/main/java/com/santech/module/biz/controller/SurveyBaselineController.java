package com.santech.module.biz.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.service.SurveyBaselineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 调查基线包：调查成果供评估消费 / 情景复盘
 */
@Api(tags = "Evaluate-调查基线包接口")
@RestController
@RequestMapping("/web-disaster-evaluate/web/disaster/evaluate/baseline")
public class SurveyBaselineController {

	private static final Logger logger = LoggerFactory.getLogger(SurveyBaselineController.class);

	@Resource
	private SurveyBaselineService surveyBaselineService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("SurveyBaselineController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@GetMapping("/get")
	@ApiOperation("按场景获取调查基线；无记录时 data=null")
	public ExecuteResult<Map<String, Object>> get(@RequestParam("sceneId") String sceneId) {
		return surveyBaselineService.getBySceneId(sceneId);
	}

	@GetMapping("/latest")
	@ApiOperation("获取最近一条调查基线")
	public ExecuteResult<Map<String, Object>> latest() {
		return surveyBaselineService.getLatest();
	}

	@PostMapping("/save")
	@ApiOperation("按 sceneId 保存/更新调查基线包")
	public ExecuteResult<Map<String, Object>> save(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
		return surveyBaselineService.save(payload, operator(request));
	}

	@GetMapping("/list")
	@ApiOperation("调查基线列表（可按场景/区县关键词）")
	public ExecuteResult<List<Map<String, Object>>> list(
			@RequestParam(value = "keyword", required = false) String keyword) {
		return surveyBaselineService.list(keyword);
	}

	private String operator(HttpServletRequest request) {
		Object name = request.getAttribute(Constants.HEADER_USERNAME);
		if (name != null) {
			return String.valueOf(name);
		}
		Object id = request.getAttribute(Constants.HEADER_USERID);
		return id == null ? "system" : String.valueOf(id);
	}
}
