package com.santech.module.biz.assessment.service;

import java.util.Map;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;

/**
 * 风暴潮评估：信息整合 / 经济损失 / 综合评估
 */
public interface StormSurgeAssessmentService {

	ExecuteResult<Map<String, Object>> integrate(String eventId, Map<String, Object> body, String operator);

	ExecuteResult<Map<String, Object>> economicLoss(String eventId, Map<String, Object> body, String operator);

	ExecuteResult<Map<String, Object>> comprehensive(String eventId, Map<String, Object> body, String operator);

	/** 兼容旧 StepCompute 经济损失入口 */
	ExecuteResult<Map<String, Object>> computeLossLegacy(StepComputeRequestDto dto);
}
