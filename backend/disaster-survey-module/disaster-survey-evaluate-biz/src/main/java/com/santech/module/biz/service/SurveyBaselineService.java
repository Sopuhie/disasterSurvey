package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import com.santech.framework.global.ExecuteResult;

/**
 * 调查基线包：调查成果供评估消费
 */
public interface SurveyBaselineService {

	ExecuteResult<Map<String, Object>> getBySceneId(String sceneId);

	ExecuteResult<Map<String, Object>> getByEventId(String eventId);

	ExecuteResult<Map<String, Object>> getLatest();

	ExecuteResult<Map<String, Object>> save(Map<String, Object> payload, String operator);

	/** 确认锁定：DRAFT -> CONFIRMED */
	ExecuteResult<Map<String, Object>> confirm(String eventId, String operator);

	ExecuteResult<List<Map<String, Object>>> list(String keyword);
}
