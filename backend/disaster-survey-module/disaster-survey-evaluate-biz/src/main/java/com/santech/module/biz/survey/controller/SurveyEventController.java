package com.santech.module.biz.survey.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.survey.service.SurveyPipelineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 灾情调查 Step2~4：按 eventId 的调查闭环接口
 */
@Api(tags = "API-灾情调查-事件流水线")
@RestController
@RequestMapping("/api/v1/survey/events")
public class SurveyEventController {

	private static final Logger logger = LoggerFactory.getLogger(SurveyEventController.class);

	@Resource
	private SurveyPipelineService surveyPipelineService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("SurveyEventController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@PostMapping("/{eventId}/affected-area")
	@ApiOperation("Step2：加载/保存受灾范围 GeoJSON")
	public ExecuteResult<Map<String, Object>> affectedArea(@PathVariable("eventId") String eventId,
			@RequestBody Map<String, Object> body, HttpServletRequest request) {
		return surveyPipelineService.saveAffectedArea(eventId, body, operator(request));
	}

	@PostMapping("/{eventId}/overlay-analysis")
	@ApiOperation("Step3：受灾范围与人口/乡镇/四类承灾体叠置分析")
	public ExecuteResult<Map<String, Object>> overlayAnalysis(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return surveyPipelineService.runOverlayAnalysis(eventId, body, operator(request));
	}

	@PostMapping("/{eventId}/exposure-images/export")
	@ApiOperation("Step3：按四类承灾体导出灾后遥感影像清单")
	public ExecuteResult<Map<String, Object>> exportExposureImages(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return surveyPipelineService.exportExposureImages(eventId, body, operator(request));
	}

	@PostMapping("/{eventId}/compare-quick-report")
	@ApiOperation("Step4：遥感解译 vs 速报上报对比核查")
	public ExecuteResult<Map<String, Object>> compareQuickReport(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return surveyPipelineService.compareQuickReport(eventId, body, operator(request));
	}

	@PostMapping("/{eventId}/baseline/confirm")
	@ApiOperation("Step4：确认核查结果并锁定调查基线 DRAFT -> CONFIRMED")
	public ExecuteResult<Map<String, Object>> confirmBaseline(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return surveyPipelineService.confirmBaseline(eventId, body, operator(request));
	}

	@GetMapping("/{eventId}/baseline")
	@ApiOperation("获取事件最新调查基线详情")
	public ExecuteResult<Map<String, Object>> getBaseline(@PathVariable("eventId") String eventId) {
		return surveyPipelineService.getBaseline(eventId);
	}

	@GetMapping("/{eventId}/dossiers")
	@ApiOperation("查询事件下承灾体现场核查档案")
	public ExecuteResult<List<Map<String, Object>>> listDossiers(@PathVariable("eventId") String eventId,
			@RequestParam(value = "exposureType", required = false) String exposureType) {
		return surveyPipelineService.listDossiers(eventId, exposureType);
	}

	@PostMapping("/{eventId}/dossiers")
	@ApiOperation("保存/更新事件下承灾体现场核查档案")
	public ExecuteResult<Map<String, Object>> saveDossier(@PathVariable("eventId") String eventId,
			@RequestBody Map<String, Object> dossier, HttpServletRequest request) {
		return surveyPipelineService.saveDossier(eventId, dossier, operator(request));
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
