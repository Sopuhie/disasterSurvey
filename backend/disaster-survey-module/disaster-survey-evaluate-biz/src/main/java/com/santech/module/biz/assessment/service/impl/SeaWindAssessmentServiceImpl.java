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
import com.santech.module.biz.assessment.service.SeaWindAssessmentService;
import com.santech.module.biz.assessment.support.AssessmentRecordSupport;
import com.santech.module.biz.assessment.support.EconomyLossCalculator;
import com.santech.module.biz.engine.ExternalModelExecutor;

@Service
public class SeaWindAssessmentServiceImpl implements SeaWindAssessmentService {

	private static final Logger logger = LoggerFactory.getLogger(SeaWindAssessmentServiceImpl.class);

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
			if (isAsync(input)) {
				BizAssessmentRecordModel running = recordSupport.upsertAsyncRunning(eventId, DisasterTypeEnum.SEA_WIND,
						AssessmentSubTypeEnum.WIND_DAMAGE_ANALYSIS, event.getRegionCode(),
						baseline != null ? baseline.getId() : null, input, operator);
				externalModelExecutor.submitAssessment(running.getId());
				return ExecuteResult.success("海风破坏性分析已提交异步计算", recordSupport.toMap(running));
			}

			double maxWind = EconomyLossCalculator.num(input.get("maxWind"), 30);
			String sceneName = first(str(input.get("sceneName")), str(input.get("sarImageId")), "SAR海风场景");
			List<Map<String, Object>> sampleRows = asList(input.get("rows"));
			if (sampleRows.isEmpty()) {
				sampleRows = defaultWindDamageRows(maxWind, sceneName);
			} else {
				sampleRows = scaleAndDecorateWind(sampleRows, maxWind, sceneName);
			}

			Map<String, Object> totals = new HashMap<>();
			totals.put("count", sampleRows.size());
			totals.put("maxWind", maxWind);
			totals.put("severeCount", countLevel(sampleRows, 1, 2));

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "wind-damage");
			result.put("eventId", eventId);
			result.put("sceneName", sceneName);
			result.put("rows", sampleRows);
			result.put("totals", totals);

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.SEA_WIND,
					AssessmentSubTypeEnum.WIND_DAMAGE_ANALYSIS, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);
			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("海风破坏性分析完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("sea-wind damage error", e);
			return ExecuteResult.error("海风破坏性分析失败:" + e.getMessage(), null);
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
				points = defaultWindTrack();
			}
			double maxWind = 0;
			for (Map<String, Object> p : points) {
				maxWind = Math.max(maxWind, EconomyLossCalculator.num(p.get("wind"), 0));
			}
			if (maxWind <= 0) {
				maxWind = EconomyLossCalculator.num(input.get("maxWind"), 28);
			}
			Map<String, Object> peak = points.get(points.size() - 1);
			for (Map<String, Object> p : points) {
				if (EconomyLossCalculator.num(p.get("wind"), 0) >= maxWind) {
					peak = p;
				}
			}

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "wind-behavior");
			result.put("eventId", eventId);
			result.put("eventName", first(str(input.get("eventName")), event.getEventName()));
			result.put("maxWind", maxWind);
			result.put("avgDir", first(str(peak.get("dir")), str(input.get("avgDir")), "-"));
			result.put("points", points);
			result.put("peak", peak);
			result.put("inundationRadiusKm", EconomyLossCalculator.round2(Math.max(maxWind * 0.42, 12)));

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.SEA_WIND,
					AssessmentSubTypeEnum.WIND_BEHAVIOR_ANALYSIS, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);
			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("海风行为分析完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("sea-wind behavior error", e);
			return ExecuteResult.error("海风行为分析失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> comprehensive(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.findLatestBaseline(eventId);
			BizAssessmentRecordModel damage = recordSupport.dao().getByEventAndSubType(eventId,
					AssessmentSubTypeEnum.WIND_DAMAGE_ANALYSIS.getCode());
			BizAssessmentRecordModel behavior = recordSupport.dao().getByEventAndSubType(eventId,
					AssessmentSubTypeEnum.WIND_BEHAVIOR_ANALYSIS.getCode());
			if (damage == null) {
				ExecuteResult<Map<String, Object>> dmg = this.damage(eventId, body, operator);
				if (!"S".equals(dmg.getResultCode())) {
					return dmg;
				}
				damage = recordSupport.dao().getByEventAndSubType(eventId,
						AssessmentSubTypeEnum.WIND_DAMAGE_ANALYSIS.getCode());
			}
			if (behavior == null) {
				ExecuteResult<Map<String, Object>> beh = this.behavior(eventId, body, operator);
				if (!"S".equals(beh.getResultCode())) {
					return beh;
				}
				behavior = recordSupport.dao().getByEventAndSubType(eventId,
						AssessmentSubTypeEnum.WIND_BEHAVIOR_ANALYSIS.getCode());
			}

			Map<String, Object> damageData = parseMap(damage.getResultDataJson());
			Map<String, Object> behaviorData = parseMap(behavior.getResultDataJson());
			double maxWind = EconomyLossCalculator.num(behaviorData.get("maxWind"),
					EconomyLossCalculator.num(((Map<?, ?>) damageData.getOrDefault("totals", new HashMap<>())).get("maxWind"),
							30));
			int severe = (int) EconomyLossCalculator.num(
					((Map<?, ?>) damageData.getOrDefault("totals", new HashMap<>())).get("severeCount"), 0);
			double score = EconomyLossCalculator.round2(Math.max(0, 100 - (maxWind - 20) * 2.2 - severe * 4));
			String grade = score >= 80 ? "较轻" : score >= 60 ? "中等" : score >= 40 ? "较重" : "严重";

			Map<String, Object> factors = new LinkedHashMap<>();
			factors.put("maxWind", maxWind);
			factors.put("severeCount", severe);
			factors.put("behaviorScore", Math.min(100, maxWind * 2));

			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);
			Map<String, Object> result = new HashMap<>();
			result.put("kind", "wind-comprehensive");
			result.put("eventId", eventId);
			result.put("baselineId", baseline != null ? baseline.getId() : null);
			result.put("totalScore", score);
			result.put("grade", grade);
			result.put("factors", factors);
			result.put("damageRef", damage.getId());
			result.put("behaviorRef", behavior.getId());
			result.put("recommend", "建议对高破坏等级岸段开展加固复核，并结合 SAR 风场轨迹完善预警阈值。");

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.SEA_WIND,
					AssessmentSubTypeEnum.WIND_COMPREHENSIVE, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);
			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("海风综合评估完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("sea-wind comprehensive error", e);
			return ExecuteResult.error("海风综合评估失败:" + e.getMessage(), null);
		}
	}

	private List<Map<String, Object>> defaultWindDamageRows(double maxWind, String sceneName) {
		double factor = Math.max(0.55, Math.min(maxWind / 30d, 1.45));
		double[] winds = { 34 * factor, 30 * factor, 26 * factor, 22 * factor };
		String[] names = { "岸段A", "渔港B", "养殖区C", "旅游区D" };
		List<Map<String, Object>> rows = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			Map<String, Object> row = new HashMap<>();
			row.put("name", names[i]);
			row.put("wind", EconomyLossCalculator.round2(winds[i]));
			row.put("analysis", "SAR 反演风场显示局部阵风偏强（依据 " + sceneName + "）");
			decorateWindLevel(row);
			rows.add(row);
		}
		return rows;
	}

	private List<Map<String, Object>> scaleAndDecorateWind(List<Map<String, Object>> rows, double maxWind,
			String sceneName) {
		double factor = Math.max(0.55, Math.min(maxWind / 30d, 1.45));
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> item : rows) {
			Map<String, Object> row = new HashMap<>(item);
			if (item.get("wind") != null) {
				row.put("wind", EconomyLossCalculator.round2(EconomyLossCalculator.num(item.get("wind"), 0) * factor));
			}
			if (StringUtil.isEmpty(str(row.get("analysis")))) {
				row.put("analysis", "依据 " + sceneName + " 的 SAR 风场强度估算");
			}
			decorateWindLevel(row);
			out.add(row);
		}
		return out;
	}

	private void decorateWindLevel(Map<String, Object> row) {
		double wind = EconomyLossCalculator.num(row.get("wind"), 0);
		int level = wind >= 32 ? 1 : wind >= 28 ? 2 : wind >= 24 ? 3 : 4;
		row.put("level", level);
		row.put("levelLabel", level == 1 ? "严重" : level == 2 ? "较重" : level == 3 ? "中等" : "较轻");
	}

	private List<Map<String, Object>> defaultWindTrack() {
		List<Map<String, Object>> points = new ArrayList<>();
		double[][] data = { { 119.6, 26.1, 22, 45 }, { 119.7, 26.2, 26, 50 }, { 119.85, 26.3, 31, 55 },
				{ 120.0, 26.35, 28, 60 } };
		for (double[] d : data) {
			Map<String, Object> p = new HashMap<>();
			p.put("lng", d[0]);
			p.put("lat", d[1]);
			p.put("wind", d[2]);
			p.put("dir", d[3] + "°");
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

	private boolean isAsync(Map<String, Object> input) {
		return "ASYNC".equalsIgnoreCase(str(input.get("computeMode")));
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
