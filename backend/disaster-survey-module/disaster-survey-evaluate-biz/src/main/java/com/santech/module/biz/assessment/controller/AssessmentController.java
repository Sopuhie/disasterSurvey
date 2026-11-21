package com.santech.module.biz.assessment.controller;

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
import com.santech.module.biz.assessment.service.AssessmentReportService;
import com.santech.module.biz.assessment.service.SeaWaveAssessmentService;
import com.santech.module.biz.assessment.service.SeaWindAssessmentService;
import com.santech.module.biz.assessment.service.StormSurgeAssessmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 灾害评估 API：三灾种分轨 + 记录查询 + 报告归档
 */
@Api(tags = "API-灾害评估")
@RestController
@RequestMapping("/api/v1/assessment")
public class AssessmentController {

	private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

	@Resource
	private StormSurgeAssessmentService stormSurgeAssessmentService;
	@Resource
	private SeaWindAssessmentService seaWindAssessmentService;
	@Resource
	private SeaWaveAssessmentService seaWaveAssessmentService;
	@Resource
	private AssessmentReportService assessmentReportService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("AssessmentController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	// ---------- 风暴潮 ----------

	@PostMapping("/events/{eventId}/storm-surge/integrate")
	@ApiOperation("风暴潮：速报受灾信息标准化整合")
	public ExecuteResult<Map<String, Object>> stormIntegrate(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return stormSurgeAssessmentService.integrate(eventId, body, operator(request));
	}

	@PostMapping("/events/{eventId}/storm-surge/economic-loss")
	@ApiOperation("风暴潮：经济损失评估测算（需已确认调查基线）")
	public ExecuteResult<Map<String, Object>> stormEconomicLoss(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return stormSurgeAssessmentService.economicLoss(eventId, body, operator(request));
	}

	@PostMapping("/events/{eventId}/storm-surge/comprehensive")
	@ApiOperation("风暴潮：综合评估")
	public ExecuteResult<Map<String, Object>> stormComprehensive(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return stormSurgeAssessmentService.comprehensive(eventId, body, operator(request));
	}

	// ---------- 海风 ----------

	@PostMapping("/events/{eventId}/sea-wind/damage")
	@ApiOperation("海风：SAR 破坏性分析")
	public ExecuteResult<Map<String, Object>> windDamage(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return seaWindAssessmentService.damage(eventId, body, operator(request));
	}

	@PostMapping("/events/{eventId}/sea-wind/behavior")
	@ApiOperation("海风：行为分析")
	public ExecuteResult<Map<String, Object>> windBehavior(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return seaWindAssessmentService.behavior(eventId, body, operator(request));
	}

	@PostMapping("/events/{eventId}/sea-wind/comprehensive")
	@ApiOperation("海风：综合评估")
	public ExecuteResult<Map<String, Object>> windComprehensive(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return seaWindAssessmentService.comprehensive(eventId, body, operator(request));
	}

	// ---------- 海浪 ----------

	@PostMapping("/events/{eventId}/sea-wave/damage")
	@ApiOperation("海浪：SAR 破坏性分析")
	public ExecuteResult<Map<String, Object>> waveDamage(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return seaWaveAssessmentService.damage(eventId, body, operator(request));
	}

	@PostMapping("/events/{eventId}/sea-wave/behavior")
	@ApiOperation("海浪：行为分析")
	public ExecuteResult<Map<String, Object>> waveBehavior(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return seaWaveAssessmentService.behavior(eventId, body, operator(request));
	}

	@PostMapping("/events/{eventId}/sea-wave/comprehensive")
	@ApiOperation("海浪：综合评估")
	public ExecuteResult<Map<String, Object>> waveComprehensive(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return seaWaveAssessmentService.comprehensive(eventId, body, operator(request));
	}

	// ---------- 记录与报告 ----------

	@GetMapping("/events/{eventId}/records")
	@ApiOperation("查询事件下评估子项记录")
	public ExecuteResult<List<Map<String, Object>>> records(@PathVariable("eventId") String eventId,
			@RequestParam(value = "disasterType", required = false) String disasterType) {
		return assessmentReportService.listRecords(eventId, disasterType);
	}

	@PostMapping("/events/{eventId}/reports/generate")
	@ApiOperation("生成并归档综合评估报告")
	public ExecuteResult<Map<String, Object>> generateReport(@PathVariable("eventId") String eventId,
			@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		return assessmentReportService.generateReport(eventId, body, operator(request));
	}

	@GetMapping("/reports")
	@ApiOperation("报告归档列表")
	public ExecuteResult<List<Map<String, Object>>> listReports(
			@RequestParam(value = "disasterType", required = false) String disasterType,
			@RequestParam(value = "keyword", required = false) String keyword) {
		return assessmentReportService.listReports(disasterType, keyword);
	}

	@GetMapping("/reports/{reportId}")
	@ApiOperation("报告详情/下载信息")
	public ExecuteResult<Map<String, Object>> getReport(@PathVariable("reportId") String reportId) {
		return assessmentReportService.getReport(reportId);
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
