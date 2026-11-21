package com.santech.module.biz.assessment.service;

import java.util.Map;

import com.santech.framework.global.ExecuteResult;

/**
 * 海浪评估：破坏性分析 / 行为分析 / 综合评估
 */
public interface SeaWaveAssessmentService {

	ExecuteResult<Map<String, Object>> damage(String eventId, Map<String, Object> body, String operator);

	ExecuteResult<Map<String, Object>> behavior(String eventId, Map<String, Object> body, String operator);

	ExecuteResult<Map<String, Object>> comprehensive(String eventId, Map<String, Object> body, String operator);
}
