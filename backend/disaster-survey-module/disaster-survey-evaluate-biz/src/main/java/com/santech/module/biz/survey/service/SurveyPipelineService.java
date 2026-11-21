package com.santech.module.biz.survey.service;

import java.util.List;
import java.util.Map;

import com.santech.framework.global.ExecuteResult;

/**
 * 灾情调查 4 步闭环流水线（按 event_id 主线）。
 */
public interface SurveyPipelineService {

	/** Step1：行政区划与人口密度 */
	ExecuteResult<Map<String, Object>> queryRegionPopulation(String regionCode, String regionName, Double lon,
			Double lat);

	/** Step1：四类承灾体基础台账 */
	ExecuteResult<Map<String, Object>> queryBaseExposures(String regionCode, String regionName, String exposureType,
			String keyword);

	/** Step2：保存受灾范围 GeoJSON */
	ExecuteResult<Map<String, Object>> saveAffectedArea(String eventId, Map<String, Object> body, String operator);

	/** Step3：叠置分析并写入基线草稿 */
	ExecuteResult<Map<String, Object>> runOverlayAnalysis(String eventId, Map<String, Object> body, String operator);

	/** Step3：承灾体遥感影像导出（路径清单） */
	ExecuteResult<Map<String, Object>> exportExposureImages(String eventId, Map<String, Object> body, String operator);

	/** Step4：遥感 vs 速报对比核查 */
	ExecuteResult<Map<String, Object>> compareQuickReport(String eventId, Map<String, Object> body, String operator);

	/** Step4：确认锁定调查基线 DRAFT -> CONFIRMED */
	ExecuteResult<Map<String, Object>> confirmBaseline(String eventId, Map<String, Object> body, String operator);

	/** 获取事件最新调查基线 */
	ExecuteResult<Map<String, Object>> getBaseline(String eventId);

	/** 事件下承灾体档案列表 */
	ExecuteResult<List<Map<String, Object>>> listDossiers(String eventId, String exposureType);

	/** 保存/更新事件下承灾体档案 */
	ExecuteResult<Map<String, Object>> saveDossier(String eventId, Map<String, Object> dossier, String operator);
}
