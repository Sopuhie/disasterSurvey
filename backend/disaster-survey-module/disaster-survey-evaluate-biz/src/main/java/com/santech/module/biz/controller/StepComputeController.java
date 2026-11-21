package com.santech.module.biz.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;
import com.santech.module.biz.service.StepComputeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 步骤 UI 服务端计算接口：识别 / 叠置 / 经济损失。
 * 对齐前端调查与评估面板，可落库 lite 任务。
 *
 * @deprecated 请改用 {@link com.santech.module.biz.survey.controller.SurveyEventController}
 *             与 {@link com.santech.module.biz.assessment.controller.AssessmentController}
 *             （路径 {@code /api/v1/survey/**}、{@code /api/v1/assessment/**}）。
 */
@Deprecated
@Api(tags = "Evaluate-步骤计算接口(已废弃)")
@RestController
@RequestMapping("/web-disaster-evaluate/web/disaster/evaluate")
public class StepComputeController {

	private static final Logger logger = LoggerFactory.getLogger(StepComputeController.class);

	@Resource
	private StepComputeService stepComputeService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("StepComputeController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@PostMapping("/survey/compute/identify")
	@ApiOperation("受灾范围识别计算（县域台账推算，可落库）")
	public ExecuteResult<Map<String, Object>> identify(@RequestBody StepComputeRequestDto dto) {
		return stepComputeService.computeIdentify(dto);
	}

	@PostMapping("/survey/compute/overlay")
	@ApiOperation("叠置分析计算（人口/乡镇/承灾体，可落库）")
	public ExecuteResult<Map<String, Object>> overlay(@RequestBody StepComputeRequestDto dto) {
		return stepComputeService.computeOverlay(dto);
	}

	@PostMapping("/task/compute/loss")
	@ApiOperation("经济损失预估计算（可落库）")
	public ExecuteResult<Map<String, Object>> loss(@RequestBody StepComputeRequestDto dto) {
		return stepComputeService.computeLoss(dto);
	}
}
