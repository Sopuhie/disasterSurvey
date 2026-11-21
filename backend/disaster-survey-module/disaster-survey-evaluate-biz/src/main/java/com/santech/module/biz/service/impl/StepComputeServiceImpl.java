package com.santech.module.biz.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.assessment.service.StormSurgeAssessmentService;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;
import com.santech.module.biz.service.StepComputeService;
import com.santech.module.biz.survey.service.SurveyOverlayComputeService;

/**
 * 步骤 UI 兼容入口。
 * 调查计算委托 SurveyOverlayComputeService；经济损失委托 StormSurgeAssessmentService。
 *
 * @deprecated 请改用 {@link com.santech.module.biz.survey.service.SurveyOverlayComputeService}
 *             与 {@link com.santech.module.biz.assessment.service.StormSurgeAssessmentService}。
 */
@Deprecated
@Service
public class StepComputeServiceImpl implements StepComputeService {

	@Resource
	private SurveyOverlayComputeService surveyOverlayComputeService;
	@Resource
	private StormSurgeAssessmentService stormSurgeAssessmentService;

	@Override
	public ExecuteResult<Map<String, Object>> computeIdentify(StepComputeRequestDto dto) {
		return surveyOverlayComputeService.computeIdentify(dto);
	}

	@Override
	public ExecuteResult<Map<String, Object>> computeOverlay(StepComputeRequestDto dto) {
		return surveyOverlayComputeService.computeOverlay(dto);
	}

	@Override
	public ExecuteResult<Map<String, Object>> computeLoss(StepComputeRequestDto dto) {
		return stormSurgeAssessmentService.computeLossLegacy(dto);
	}
}
