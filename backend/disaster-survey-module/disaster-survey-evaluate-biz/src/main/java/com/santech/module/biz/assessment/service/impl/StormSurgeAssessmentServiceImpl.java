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
import com.santech.module.biz.assessment.service.StormSurgeAssessmentService;
import com.santech.module.biz.assessment.support.AssessmentRecordSupport;
import com.santech.module.biz.assessment.support.EconomyLossCalculator;
import com.santech.module.biz.entity.dto.EvaluateHistorySaveParamsDto;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;
import com.santech.module.biz.service.EvaluateTaskService;

@Service
public class StormSurgeAssessmentServiceImpl implements StormSurgeAssessmentService {

	private static final Logger logger = LoggerFactory.getLogger(StormSurgeAssessmentServiceImpl.class);

	@Resource
	private AssessmentRecordSupport recordSupport;
	@Resource
	private EconomyLossCalculator economyLossCalculator;
	@Resource
	private EvaluateTaskService evaluateTaskService;

	@Override
	@SuppressWarnings("unchecked")
	public ExecuteResult<Map<String, Object>> integrate(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.findConfirmedBaseline(eventId);
			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);

			List<Map<String, Object>> rawItems = new ArrayList<>();
			Object qr = input.get("quickReport");
			if (qr instanceof List) {
				rawItems = (List<Map<String, Object>>) qr;
			} else if (input.get("items") instanceof List) {
				rawItems = (List<Map<String, Object>>) input.get("items");
			}

			List<Map<String, Object>> normalized = new ArrayList<>();
			for (Map<String, Object> item : rawItems) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("metric", first(str(item.get("metric")), str(item.get("name")), "指标"));
				row.put("unit", str(item.get("unit")));
				row.put("value", EconomyLossCalculator.num(item.get("value"),
						EconomyLossCalculator.num(item.get("quickValue"),
								EconomyLossCalculator.num(item.get("reportValue"), 0))));
				row.put("source", first(str(item.get("source")), "速报"));
				row.put("normalized", true);
				normalized.add(row);
			}
			if (normalized.isEmpty() && baseline != null && baseline.getAffectedPopulation() != null) {
				Map<String, Object> pop = new LinkedHashMap<>();
				pop.put("metric", "受灾人口");
				pop.put("unit", "人");
				pop.put("value", baseline.getAffectedPopulation());
				pop.put("source", "调查基线");
				pop.put("normalized", true);
				normalized.add(pop);
			}

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "storm-integrate");
			result.put("eventId", eventId);
			result.put("district", first(str(input.get("districtName")), event.getRegionName()));
			result.put("items", normalized);
			result.put("itemCount", normalized.size());
			result.put("baselineId", baseline != null ? baseline.getId() : null);

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.STORM_SURGE,
					AssessmentSubTypeEnum.STORM_INFO_INTEGRATION, event.getRegionCode(),
					baseline != null ? baseline.getId() : null, input, result, operator);

			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("风暴潮信息整合完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("storm integrate error", e);
			return ExecuteResult.error("信息整合失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> economicLoss(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.requireConfirmedBaseline(eventId);
			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);

			double ratio = EconomyLossCalculator.num(input.get("inundationRatio"), 0.46);
			String district = first(str(input.get("districtName")), baseline.getDistrict(), event.getRegionName());
			double statArea = EconomyLossCalculator.num(input.get("statArea"), 680);
			double density = EconomyLossCalculator.num(input.get("density"), 500);
			double affectedKm2;
			if (input.get("affectedKm2") != null) {
				affectedKm2 = EconomyLossCalculator.num(input.get("affectedKm2"), 0);
			} else {
				affectedKm2 = EconomyLossCalculator.round2(statArea * 0.078d * (0.72d + ratio));
			}
			int affectedPop = baseline.getAffectedPopulation() != null
					? baseline.getAffectedPopulation().intValue()
					: (int) Math.round(density * affectedKm2);

			Map<String, Object> economy = economyLossCalculator.resolveEconomy(district);
			Map<String, Object> loss = economyLossCalculator.calcEconomicLoss(economy, affectedKm2);

			Map<String, Object> summary = new HashMap<>();
			summary.put("areaKm2", affectedKm2);
			summary.put("affectedPopulation", affectedPop);
			summary.put("countyCount", 1);
			summary.put("district", district);

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "storm-economic-loss");
			result.put("eventId", eventId);
			result.put("baselineId", baseline.getId());
			result.put("sceneName", first(str(input.get("sceneName")), "灾后遥感影像"));
			result.put("economy", economy);
			result.put("summary", summary);
			result.putAll(loss);

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.STORM_SURGE,
					AssessmentSubTypeEnum.STORM_ECONOMIC_LOSS, event.getRegionCode(), baseline.getId(), input, result,
					operator);

			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			out.put("lossResult", result);
			return ExecuteResult.success("经济损失评估完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("storm economic loss error", e);
			return ExecuteResult.error("经济损失评估失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> comprehensive(String eventId, Map<String, Object> body, String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			BizSurveyBaselineModel baseline = recordSupport.requireConfirmedBaseline(eventId);

			BizAssessmentRecordModel integrate = recordSupport.dao().getByEventAndSubType(eventId,
					AssessmentSubTypeEnum.STORM_INFO_INTEGRATION.getCode());
			BizAssessmentRecordModel loss = recordSupport.dao().getByEventAndSubType(eventId,
					AssessmentSubTypeEnum.STORM_ECONOMIC_LOSS.getCode());
			if (loss == null) {
				ExecuteResult<Map<String, Object>> lossRes = economicLoss(eventId, body, operator);
				if (!"S".equals(lossRes.getResultCode())) {
					return lossRes;
				}
				loss = recordSupport.dao().getByEventAndSubType(eventId,
						AssessmentSubTypeEnum.STORM_ECONOMIC_LOSS.getCode());
			}

			Map<String, Object> lossData = parseMap(loss != null ? loss.getResultDataJson() : null);
			double totalLoss = EconomyLossCalculator.num(lossData.get("totalLoss"), 0);
			double gdpShock = EconomyLossCalculator.num(lossData.get("gdpShock"), 0);
			double pop = baseline.getAffectedPopulation() == null ? 0
					: baseline.getAffectedPopulation().doubleValue();

			double score = EconomyLossCalculator.round2(Math.max(0,
					100 - gdpShock * 1.2 - Math.min(30, pop / 5000d) - Math.min(20, totalLoss / 50000d)));
			String grade = score >= 80 ? "较轻" : score >= 60 ? "中等" : score >= 40 ? "较重" : "严重";

			Map<String, Object> factors = new LinkedHashMap<>();
			factors.put("populationImpact", Math.min(100, pop / 100d));
			factors.put("economicLoss", Math.min(100, totalLoss / 1000d));
			factors.put("gdpShock", gdpShock);
			factors.put("baselineConfirmed", true);

			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);
			Map<String, Object> result = new HashMap<>();
			result.put("kind", "storm-comprehensive");
			result.put("eventId", eventId);
			result.put("baselineId", baseline.getId());
			result.put("totalScore", score);
			result.put("grade", grade);
			result.put("factors", factors);
			result.put("integrateRef", integrate != null ? integrate.getId() : null);
			result.put("lossRef", loss != null ? loss.getId() : null);
			result.put("lossSummary", lossData.get("summary"));
			result.put("totalLoss", totalLoss);
			result.put("recommend", "建议结合现场核查结果完善承灾体修复方案，并持续跟踪重点岸段潮位风险。");

			BizAssessmentRecordModel record = recordSupport.upsertSyncRecord(eventId, DisasterTypeEnum.STORM_SURGE,
					AssessmentSubTypeEnum.STORM_COMPREHENSIVE, event.getRegionCode(), baseline.getId(), input, result,
					operator);

			Map<String, Object> out = recordSupport.toMap(record);
			out.put("result", result);
			return ExecuteResult.success("风暴潮综合评估完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("storm comprehensive error", e);
			return ExecuteResult.error("综合评估失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> computeLossLegacy(StepComputeRequestDto dto) {
		try {
			double ratio = dto.getInundationRatio() == null ? 0.46d : dto.getInundationRatio();
			double statArea = dto.getStatArea() == null ? 680d : dto.getStatArea();
			double density = dto.getDensity() == null ? 500d : dto.getDensity();
			double affectedKm2 = EconomyLossCalculator.round2(statArea * 0.078d * (0.72d + ratio));
			int affectedPop = (int) Math.round(density * affectedKm2);

			Map<String, Object> economy = economyLossCalculator.resolveEconomy(dto.getDistrictName());
			Map<String, Object> loss = economyLossCalculator.calcEconomicLoss(economy, affectedKm2);

			Map<String, Object> summary = new HashMap<>();
			summary.put("areaKm2", affectedKm2);
			summary.put("countyCount", 1);
			List<Map<String, Object>> counties = new ArrayList<>();
			Map<String, Object> c0 = new HashMap<>();
			c0.put("name", nvl(dto.getDistrictName()));
			c0.put("city", nvl(dto.getCity()));
			c0.put("areaKm2", affectedKm2);
			c0.put("affectedPopulation", affectedPop);
			counties.add(c0);
			summary.put("counties", counties);

			Map<String, Object> lossResult = new HashMap<>();
			lossResult.put("sceneName", first(dto.getSceneName(), "灾后遥感影像"));
			lossResult.put("sceneDate", nvl(dto.getSceneDate()));
			lossResult.put("sensor", "孔径雷达卫星");
			lossResult.put("economy", economy);
			lossResult.put("summary", summary);
			lossResult.put("landHint", "叠加县域 GDP 与土地利用结构测算分项损失");
			lossResult.putAll(loss);

			Map<String, Object> resultData = new HashMap<>();
			resultData.put("kind", "loss");
			resultData.put("typeId", "storm");
			resultData.put("sceneId", nvl(dto.getSceneId()));
			resultData.put("district", nvl(dto.getDistrictName()));
			resultData.put("lossResult", lossResult);

			String taskId = "";
			boolean persist = dto.getPersist() == null || dto.getPersist();
			if (persist) {
				EvaluateHistorySaveParamsDto save = new EvaluateHistorySaveParamsDto();
				save.setName("经济损失预估-" + first(dto.getDistrictName(), dto.getDisasterType(), ""));
				save.setDisasterType(first(dto.getDisasterType(), "1"));
				save.setSceneId(nvl(dto.getSceneId()));
				save.setLiteMode(true);
				save.setCjr(first(dto.getCjr(), "system"));
				Map<String, Object> report = new HashMap<>();
				report.put("kind", "loss");
				report.put("district", nvl(dto.getDistrictName()));
				report.put("sceneName", lossResult.get("sceneName"));
				report.put("summary", lossResult.get("summary"));
				save.setReportData(JSON.toJSONString(report));
				save.setResultData(JSON.toJSONString(resultData));
				ExecuteResult<String> res = evaluateTaskService.buildEvaluateTaskInfo(save);
				taskId = res != null && "S".equals(res.getResultCode()) ? nvl(res.getData()) : "";
			}

			Map<String, Object> out = new HashMap<>();
			out.put("kind", "loss");
			out.put("lossResult", lossResult);
			out.put("taskId", taskId);
			out.put("resultData", resultData);
			return ExecuteResult.success("损失计算成功", out);
		} catch (Exception e) {
			logger.error("computeLossLegacy error", e);
			return ExecuteResult.error("损失计算失败:" + e.getMessage(), null);
		}
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

	private static String nvl(String v) {
		return v == null ? "" : v;
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
