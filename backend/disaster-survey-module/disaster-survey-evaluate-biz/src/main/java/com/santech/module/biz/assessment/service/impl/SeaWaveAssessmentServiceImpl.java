package com.santech.module.biz.assessment.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.entity.enums.biz.AssessmentSubTypeEnum;
import com.santech.core.framework.entity.enums.biz.DisasterTypeEnum;
import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.core.framework.entity.model.BizDisasterEventModel;
import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.assessment.service.SeaWaveAssessmentService;
import com.santech.module.biz.assessment.support.AssessmentRecordSupport;
import com.santech.module.biz.assessment.support.EconomyLossCalculator;
import com.santech.module.biz.engine.ExternalModelExecutor;

@Service
public class SeaWaveAssessmentServiceImpl implements SeaWaveAssessmentService {

	private static final Logger logger = LoggerFactory.getLogger(SeaWaveAssessmentServiceImpl.class);

	@Resource
	private AssessmentRecordSupport recordSupport;
	@Resource
	private ExternalModelExecutor externalModelExecutor;

	@Override
	@SuppressWarnings("unchecked")
	public ExecuteResult<Map<String, Object>> damage(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.findLatestBaseline(eventId);
			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);
			if ("ASYNC".equalsIgnoreCase(str(input.get("computeMode")))) {
				BizAssessmentRecordModel running = recordSupport.upsertAsyncRunning(eventId, DisasterTypeEnum.SEA_WAVE,
						AssessmentSubTypeEnum.WAVE_DAMAGE_ANALYSIS, event.getRegionCode(),
						baseline != null ? baseline.getId() : null, input, operator);
				externalModelExecutor.submitAssessment(running.getId());
				return ExecuteResult.success("海浪破坏性分析已提交异步计算", recordSupport.toMap(running));
			}

			double maxHeight = EconomyLossCalculator.num(input.get("maxHeight"), 4);
			String sceneName = first(str(input.get("sceneName")), str(input.get("sarImageId")), "SAR海浪场景");
			List<Map<String, Object>> sampleRows = asList(input.get("rows"));
			if (sampleRows.isEmpty()) {
				sampleRows = defaultWaveDamageRows(maxHeight, sceneName);
			} else {
				sampleRows = scaleAndDecorateWave(sampleRows, maxHeight, sceneName);
			}

			Map<String, Object> totals = new HashMap<>();
			totals.put("count", sampleRows.size());
			totals.put("maxHeight", maxHeight);
			totals.put("severeCount", countLevel(sampleRows, 1, 2));

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "wave-damage");
			result.put("eventId", eventId);
			result.put("sceneName", sceneName);
			result.put("rows", sampleRows);
			result.put("totals", totals);

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.SEA_WAVE,
					AssessmentSubTypeEnum.WAVE_DAMAGE_ANALYSIS, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);
			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("海浪破坏性分析完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("sea-wave damage error", e);
			return ExecuteResult.error("海浪破坏性分析失败:" + e.getMessage(), null);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ExecuteResult<Map<String, Object>> behavior(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.findLatestBaseline(eventId);
			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);

			List<Map<String, Object>> points = asList(input.get("points"));
			if (points.isEmpty()) {
				points = defaultWaveTrack();
			}
			double maxHeight = 0;
			double maxSpeed = 0;
			for (Map<String, Object> p : points) {
				maxHeight = Math.max(maxHeight, EconomyLossCalculator.num(p.get("height"), 0));
				maxSpeed = Math.max(maxSpeed, EconomyLossCalculator.num(p.get("speed"), 0));
			}
			if (maxHeight <= 0) {
				maxHeight = EconomyLossCalculator.num(input.get("maxHeight"), 3.8);
			}

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "wave-behavior");
			result.put("eventId", eventId);
			result.put("eventName", first(str(input.get("eventName")), event.getEventName()));
			result.put("maxHeight", maxHeight);
			result.put("maxSpeed", maxSpeed);
			result.put("points", points);
			result.put("erosionEstimateKm", EconomyLossCalculator.round2(maxHeight * 0.35));
			result.put("inundationHint", "高潮位叠加涌浪抬升，近岸低洼区需重点关注");

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.SEA_WAVE,
					AssessmentSubTypeEnum.WAVE_BEHAVIOR_ANALYSIS, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);
			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("海浪行为分析完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("sea-wave behavior error", e);
			return ExecuteResult.error("海浪行为分析失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> comprehensive(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.findLatestBaseline(eventId);
			BizAssessmentRecordModel damage = recordSupport.dao().getByEventAndSubType(eventId,
					AssessmentSubTypeEnum.WAVE_DAMAGE_ANALYSIS.getCode());
			BizAssessmentRecordModel behavior = recordSupport.dao().getByEventAndSubType(eventId,
					AssessmentSubTypeEnum.WAVE_BEHAVIOR_ANALYSIS.getCode());
			if (damage == null) {
				ExecuteResult<Map<String, Object>> dmg = this.damage(eventId, body, operator);
				if (!"S".equals(dmg.getResultCode())) {
					return dmg;
				}
				damage = recordSupport.dao().getByEventAndSubType(eventId,
						AssessmentSubTypeEnum.WAVE_DAMAGE_ANALYSIS.getCode());
			}
			if (behavior == null) {
				ExecuteResult<Map<String, Object>> beh = this.behavior(eventId, body, operator);
				if (!"S".equals(beh.getResultCode())) {
					return beh;
				}
				behavior = recordSupport.dao().getByEventAndSubType(eventId,
						AssessmentSubTypeEnum.WAVE_BEHAVIOR_ANALYSIS.getCode());
			}

			Map<String, Object> damageData = parseMap(damage.getResultDataJson());
			Map<String, Object> behaviorData = parseMap(behavior.getResultDataJson());
			double maxHeight = EconomyLossCalculator.num(behaviorData.get("maxHeight"),
					EconomyLossCalculator.num(
							((Map<?, ?>) damageData.getOrDefault("totals", new HashMap<>())).get("maxHeight"), 4));
			int severe = (int) EconomyLossCalculator.num(
					((Map<?, ?>) damageData.getOrDefault("totals", new HashMap<>())).get("severeCount"), 0);
			double erosion = EconomyLossCalculator.num(behaviorData.get("erosionEstimateKm"), 0);
			double score = EconomyLossCalculator
					.round2(Math.max(0, 100 - (maxHeight - 2) * 12 - severe * 5 - erosion * 3));
			String grade = score >= 80 ? "较轻" : score >= 60 ? "中等" : score >= 40 ? "较重" : "严重";

			Map<String, Object> factors = new LinkedHashMap<>();
			factors.put("maxHeight", maxHeight);
			factors.put("severeCount", severe);
			factors.put("erosionEstimateKm", erosion);

			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);
			Map<String, Object> result = new HashMap<>();
			result.put("kind", "wave-comprehensive");
			result.put("eventId", eventId);
			result.put("baselineId", baseline != null ? baseline.getId() : null);
			result.put("totalScore", score);
			result.put("grade", grade);
			result.put("factors", factors);
			result.put("damageRef", damage.getId());
			result.put("behaviorRef", behavior.getId());
			result.put("recommend", "建议加强近岸防护与沙坝养护，并对高浪冲击岸段开展断面复核。");

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.SEA_WAVE,
					AssessmentSubTypeEnum.WAVE_COMPREHENSIVE, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);
			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("海浪综合评估完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("sea-wave comprehensive error", e);
			return ExecuteResult.error("海浪综合评估失败:" + e.getMessage(), null);
		}
	}

	private List<Map<String, Object>> defaultWaveDamageRows(double maxHeight, String sceneName) {
		double factor = Math.max(0.55, Math.min(maxHeight / 4d, 1.45));
		double[] heights = { 4.8 * factor, 4.1 * factor, 3.4 * factor, 2.7 * factor };
		double[] speeds = { 18 * factor, 15 * factor, 12 * factor, 9 * factor };
		String[] names = { "岸段A", "渔港B", "养殖区C", "旅游区D" };
		List<Map<String, Object>> rows = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			Map<String, Object> row = new HashMap<>();
			row.put("name", names[i]);
			row.put("height", EconomyLossCalculator.round2(heights[i]));
			row.put("speed", EconomyLossCalculator.round2(speeds[i]));
			row.put("analysis", "SAR 海浪谱显示涌浪能量偏高（依据 " + sceneName + "）");
			decorateWaveLevel(row);
			rows.add(row);
		}
		return rows;
	}

	private List<Map<String, Object>> scaleAndDecorateWave(List<Map<String, Object>> rows, double maxHeight,
			String sceneName) {
		double factor = Math.max(0.55, Math.min(maxHeight / 4d, 1.45));
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> item : rows) {
			Map<String, Object> row = new HashMap<>(item);
			if (item.get("height") != null) {
				row.put("height",
						EconomyLossCalculator.round2(EconomyLossCalculator.num(item.get("height"), 0) * factor));
			}
			if (item.get("speed") != null) {
				row.put("speed",
						EconomyLossCalculator.round2(EconomyLossCalculator.num(item.get("speed"), 0) * factor * 0.95));
			}
			if (StringUtil.isEmpty(str(row.get("analysis")))) {
				row.put("analysis", "依据 " + sceneName + " 的 SAR 海浪强度估算");
			}
			decorateWaveLevel(row);
			out.add(row);
		}
		return out;
	}

	private void decorateWaveLevel(Map<String, Object> row) {
		double height = EconomyLossCalculator.num(row.get("height"), 0);
		int level = height >= 4.5 ? 1 : height >= 3.5 ? 2 : height >= 2.8 ? 3 : 4;
		row.put("level", level);
		row.put("levelLabel", level == 1 ? "严重" : level == 2 ? "较重" : level == 3 ? "中等" : "较轻");
	}

	private List<Map<String, Object>> defaultWaveTrack() {
		List<Map<String, Object>> points = new ArrayList<>();
		double[][] data = { { 119.55, 26.05, 2.6, 10 }, { 119.7, 26.15, 3.3, 13 }, { 119.9, 26.25, 4.1, 16 },
				{ 120.05, 26.32, 3.6, 14 } };
		for (double[] d : data) {
			Map<String, Object> p = new HashMap<>();
			p.put("lng", d[0]);
			p.put("lat", d[1]);
			p.put("height", d[2]);
			p.put("speed", d[3]);
			points.add(p);
		}
		return points;
	}

	private int countLevel(List<Map<String, Object>> rows, int... levels) {
		int count = 0;
		for (Map<String, Object> row : rows) {
			int level = (int) EconomyLossCalculator.num(row.get("level"), 99);
			for (int l : levels) {
				if (level == l) {
					count++;
					break;
				}
			}
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> asList(Object obj) {
		if (obj instanceof List) {
			return (List<Map<String, Object>>) obj;
		}
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseMap(String json) {
		if (StringUtil.isEmpty(json)) {
			return new HashMap<>();
		}
		try {
			Object obj = JSON.parse(json);
			if (obj instanceof Map) {
				return (Map<String, Object>) obj;
			}
		} catch (Exception ignored) {
			// ignore
		}
		return new HashMap<>();
	}

	private static String str(Object v) {
		return v == null ? "" : String.valueOf(v);
	}

	private static String first(String... vals) {
		if (vals == null) {
			return "";
		}
		for (String v : vals) {
			if (StringUtil.isNotEmpty(v)) {
				return v;
			}
		}
		return "";
	}
}
