package com.santech.module.biz.survey.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.entity.enums.biz.ExposureTypeEnum;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskSaveParamsDto;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;
import com.santech.module.biz.service.BizFrontendDataService;
import com.santech.module.biz.service.DisasterSurveyTaskService;
import com.santech.module.biz.service.ExposureDataService;
import com.santech.module.biz.survey.service.SurveyOverlayComputeService;

/**
 * 灾情调查叠置计算实现（自 StepComputeServiceImpl 拆分）。
 */
@Service
public class SurveyOverlayComputeServiceImpl implements SurveyOverlayComputeService {

	private static final Logger logger = LoggerFactory.getLogger(SurveyOverlayComputeServiceImpl.class);

	@Resource
	private BizFrontendDataService bizFrontendDataService;
	@Resource
	private ExposureDataService exposureDataService;
	@Resource
	private DisasterSurveyTaskService disasterSurveyTaskService;

	@Override
	public ExecuteResult<Map<String, Object>> computeIdentify(StepComputeRequestDto dto) {
		try {
			Map<String, Object> meta = buildIdentifyMeta(dto);
			Map<String, Object> resultData = new HashMap<>();
			resultData.put("kind", "identify");
			resultData.put("sceneId", nvl(dto.getSceneId()));
			resultData.put("district", nvl(dto.getDistrictName()));
			resultData.put("meta", meta);

			String taskId = maybePersistSurvey(dto, "Pop",
					"受灾范围识别-" + first(dto.getSceneName(), dto.getSceneId(), "default"), resultData);

			Map<String, Object> out = new HashMap<>();
			out.put("kind", "identify");
			out.put("meta", meta);
			out.put("sceneId", nvl(dto.getSceneId()));
			out.put("taskId", taskId);
			out.put("resultData", resultData);
			return ExecuteResult.success("识别计算成功", out);
		} catch (Exception e) {
			logger.error("computeIdentify error", e);
			return ExecuteResult.error("识别计算失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> computeOverlay(StepComputeRequestDto dto) {
		try {
			String target = normalizeTarget(dto.getTargetEntity());
			if (StringUtil.isEmpty(target)) {
				return ExecuteResult.error("targetEntity 不能为空", null);
			}
			Map<String, Object> pack;
			if ("Pop".equals(target)) {
				Map<String, Object> meta = buildIdentifyMeta(dto);
				pack = buildPopulationOverlay(meta);
			} else if ("Xzq".equals(target)) {
				pack = buildDistrictOverlay(dto);
			} else {
				pack = buildHazardOverlay(target, dto.getHazardName());
			}

			String frontendTarget = toFrontendTarget(target);
			String exposureCode = toExposureCode(target);
			Map<String, Object> resultData = new HashMap<>();
			resultData.put("kind", "overlay");
			resultData.put("sceneId", nvl(dto.getSceneId()));
			resultData.put("targetId", frontendTarget);
			resultData.put("exposureType", exposureCode);
			resultData.put("hazardName", nvl(dto.getHazardName()));
			resultData.put("totals", pack.get("totals"));
			resultData.put("rows", pack.get("rows"));

			String taskId = maybePersistSurvey(dto, target,
					"叠置分析-" + first(exposureCode, frontendTarget)
							+ (StringUtil.isNotEmpty(dto.getHazardName()) ? "-" + dto.getHazardName() : ""),
					resultData);

			Map<String, Object> out = new HashMap<>();
			out.put("kind", "overlay");
			out.put("targetId", frontendTarget);
			out.put("exposureType", exposureCode);
			out.put("targetEntity", target);
			out.put("rows", pack.get("rows"));
			out.put("totals", pack.get("totals"));
			out.put("taskId", taskId);
			out.put("resultData", resultData);
			return ExecuteResult.success("叠置计算成功", out);
		} catch (Exception e) {
			logger.error("computeOverlay error", e);
			return ExecuteResult.error("叠置计算失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> computeFullOverlay(StepComputeRequestDto dto) {
		try {
			Map<String, Object> identifyMeta = buildIdentifyMeta(dto);
			Map<String, Object> population = buildPopulationOverlay(identifyMeta);
			Map<String, Object> town = buildDistrictOverlay(dto);

			Map<String, Object> exposures = new LinkedHashMap<>();
			for (ExposureTypeEnum type : ExposureTypeEnum.values()) {
				Map<String, Object> pack = buildHazardOverlay(type.getEntityType(), dto.getHazardName());
				exposures.put(type.getCode(), pack);
			}

			int affectedPop = 0;
			Object popTotals = population.get("totals");
			if (popTotals instanceof Map) {
				affectedPop = (int) num(((Map<?, ?>) popTotals).get("affectedPopulation"), 0);
			}

			Map<String, Object> out = new HashMap<>();
			out.put("kind", "full-overlay");
			out.put("identifyMeta", identifyMeta);
			out.put("affectedPopulation", affectedPop);
			out.put("population", population);
			out.put("townAreaStats", town);
			out.put("exposureOverlay", exposures);
			out.put("sceneId", nvl(dto.getSceneId()));
			out.put("district", nvl(dto.getDistrictName()));
			return ExecuteResult.success("全量叠置计算成功", out);
		} catch (Exception e) {
			logger.error("computeFullOverlay error", e);
			return ExecuteResult.error("全量叠置计算失败:" + e.getMessage(), null);
		}
	}

	public Map<String, Object> buildIdentifyMeta(StepComputeRequestDto dto) {
		double ratio = dto.getInundationRatio() == null ? 0.45d : dto.getInundationRatio();
		List<Map<String, Object>> counties = listBiz("county");
		List<Map<String, Object>> pool = new ArrayList<>();
		String city = nvl(dto.getCity());
		String focus = nvl(dto.getDistrictName());
		if (StringUtil.isNotEmpty(city)) {
			for (Map<String, Object> item : counties) {
				String c = str(item.get("city"));
				if (c.contains(city) || city.contains(c)) {
					pool.add(item);
				}
			}
		}
		if (pool.isEmpty()) {
			pool.addAll(counties);
		}
		if (StringUtil.isNotEmpty(focus)) {
			pool.sort(Comparator.comparingInt(a -> focus.equals(str(a.get("name"))) ? 0 : 1));
		}
		List<Map<String, Object>> hit = pool.size() > 6 ? pool.subList(0, 6) : pool;
		List<Map<String, Object>> rows = new ArrayList<>();
		double areaSum = 0;
		for (int i = 0; i < hit.size(); i++) {
			Map<String, Object> item = hit.get(i);
			double base = num(item.get("statArea"), num(item.get("areaKm2"), 400));
			double areaKm2 = round2(base * 0.035d * (0.85d + ratio) * (1 - i * 0.06d));
			double density = num(item.get("density"), 500);
			Map<String, Object> row = new HashMap<>();
			row.put("name", str(item.get("name")));
			row.put("city", str(item.get("city")));
			row.put("regionCode", str(item.get("code")));
			row.put("areaKm2", areaKm2);
			row.put("areaWanMu", km2ToWanMu(areaKm2));
			row.put("density", density);
			row.put("affectedPopulation", (int) Math.round(density * areaKm2));
			rows.add(row);
			areaSum += areaKm2;
		}
		areaSum = round2(areaSum);
		Map<String, Object> meta = new HashMap<>();
		meta.put("areaKm2", areaSum);
		meta.put("areaWanMu", km2ToWanMu(areaSum));
		meta.put("countyCount", rows.size());
		meta.put("counties", rows);
		meta.put("sceneName", nvl(dto.getSceneName()));
		meta.put("sceneDate", nvl(dto.getSceneDate()));
		meta.put("regionHint", nvl(dto.getRegionHint()));
		meta.put("sourceHint", rows.isEmpty() ? "empty" : "ledger");
		return meta;
	}

	private Map<String, Object> buildPopulationOverlay(Map<String, Object> meta) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> counties = (List<Map<String, Object>>) meta.get("counties");
		List<Map<String, Object>> rows = counties == null ? new ArrayList<>() : counties;
		double area = 0;
		int pop = 0;
		for (Map<String, Object> r : rows) {
			area += num(r.get("areaKm2"), 0);
			pop += (int) num(r.get("affectedPopulation"), 0);
		}
		Map<String, Object> totals = new HashMap<>();
		totals.put("count", rows.size());
		totals.put("areaKm2", round2(area));
		totals.put("affectedPopulation", pop);
		Map<String, Object> pack = new HashMap<>();
		pack.put("rows", rows);
		pack.put("totals", totals);
		return pack;
	}

	private Map<String, Object> buildDistrictOverlay(StepComputeRequestDto dto) {
		List<Map<String, Object>> towns = listBiz("town");
		String focus = nvl(dto.getDistrictName());
		List<Map<String, Object>> rows = new ArrayList<>();
		double areaSum = 0;
		for (Map<String, Object> item : towns) {
			if (StringUtil.isNotEmpty(focus)) {
				String county = str(item.get("county"));
				if (StringUtil.isNotEmpty(county) && !county.contains(focus) && !focus.contains(county)) {
					continue;
				}
			}
			double area = num(item.get("areaKm2"), num(item.get("statArea"), 0));
			Map<String, Object> row = new HashMap<>();
			row.put("name", str(item.get("name")));
			row.put("county", str(item.get("county")));
			row.put("city", str(item.get("city")));
			row.put("areaKm2", area);
			row.put("areaWanMu", km2ToWanMu(area));
			rows.add(row);
			areaSum += area;
		}
		Map<String, Object> totals = new HashMap<>();
		totals.put("count", rows.size());
		totals.put("areaKm2", round2(areaSum));
		Map<String, Object> pack = new HashMap<>();
		pack.put("rows", rows);
		pack.put("totals", totals);
		return pack;
	}

	private Map<String, Object> buildHazardOverlay(String targetEntity, String hazardName) {
		ExposureTypeEnum exposureType = ExposureTypeEnum.fromAny(targetEntity);
		String queryType = exposureType != null ? exposureType.getCode() : targetEntity;
		ExecuteResult<List<Map<String, Object>>> res = exposureDataService.queryExposureList(queryType, null);
		List<Map<String, Object>> list = res != null && res.getData() != null ? res.getData() : new ArrayList<>();
		if (list.isEmpty() && exposureType != null) {
			list = listBiz(exposureType.getLegacyAlias());
		}
		if (StringUtil.isNotEmpty(hazardName)) {
			List<Map<String, Object>> filtered = new ArrayList<>();
			for (Map<String, Object> item : list) {
				String name = first(str(item.get("name")), str(item.get("dangerName")));
				if (hazardName.equals(name)) {
					filtered.add(item);
				}
			}
			if (!filtered.isEmpty()) {
				list = filtered;
			}
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		double areaSum = 0;
		double lengthSum = 0;
		int take = Math.max(3, list.size() > 0 ? list.size() - 1 : 0);
		for (int i = 0; i < list.size() && i < take; i++) {
			Map<String, Object> item = list.get(i);
			double scale = 0.55d - i * 0.06d;
			double areaKm2 = round2(num(item.get("areaKm2"), 0.8) * Math.max(scale, 0.2));
			double lengthKm = round2(
					num(item.get("lengthKm"), num(item.get("length"), 0)) * Math.max(0.35, 0.72 - i * 0.08));
			Map<String, Object> row = new HashMap<>();
			row.put("id", str(item.get("id")));
			row.put("name", first(str(item.get("name")), str(item.get("dangerName"))));
			row.put("exposureType", exposureType != null ? exposureType.getCode() : queryType);
			row.put("division", str(item.get("division")));
			row.put("town", str(item.get("town")));
			row.put("areaKm2", areaKm2);
			row.put("areaWanMu", km2ToWanMu(areaKm2));
			row.put("lengthKm", lengthKm);
			rows.add(row);
			areaSum += areaKm2;
			lengthSum += lengthKm;
		}
		Map<String, Object> totals = new HashMap<>();
		totals.put("count", rows.size());
		totals.put("areaKm2", round2(areaSum));
		totals.put("lengthKm", round2(lengthSum));
		Map<String, Object> pack = new HashMap<>();
		pack.put("rows", rows);
		pack.put("totals", totals);
		pack.put("exposureType", exposureType != null ? exposureType.getCode() : queryType);
		return pack;
	}

	private String maybePersistSurvey(StepComputeRequestDto dto, String targetEntity, String name,
			Map<String, Object> resultData) {
		boolean persist = dto.getPersist() == null || dto.getPersist();
		if (!persist) {
			return "";
		}
		DisasterSurveyTaskSaveParamsDto save = new DisasterSurveyTaskSaveParamsDto();
		save.setName(name);
		save.setTargetEntity(targetEntity);
		save.setSceneId(nvl(dto.getSceneId()));
		save.setLiteMode(true);
		save.setCjr(first(dto.getCjr(), "system"));
		save.setResultData(JSON.toJSONString(resultData));
		ExecuteResult<String> res = disasterSurveyTaskService.buildDisasterSurveyTaskInfo(save);
		return res != null && "S".equals(res.getResultCode()) ? nvl(res.getData()) : "";
	}

	private List<Map<String, Object>> listBiz(String dataType) {
		ExecuteResult<List<Map<String, Object>>> res = bizFrontendDataService.list(dataType, null, null);
		return res != null && res.getData() != null ? res.getData() : new ArrayList<>();
	}

	private static String normalizeTarget(String raw) {
		if (StringUtil.isEmpty(raw)) {
			return "";
		}
		if ("population".equalsIgnoreCase(raw) || "Pop".equalsIgnoreCase(raw)) {
			return "Pop";
		}
		if ("district".equalsIgnoreCase(raw) || "Xzq".equalsIgnoreCase(raw) || "town".equalsIgnoreCase(raw)) {
			return "Xzq";
		}
		ExposureTypeEnum type = ExposureTypeEnum.fromAny(raw);
		if (type != null) {
			return type.getEntityType();
		}
		return raw;
	}

	private static String toFrontendTarget(String targetEntity) {
		if ("Pop".equals(targetEntity)) {
			return "population";
		}
		if ("Xzq".equals(targetEntity)) {
			return "district";
		}
		ExposureTypeEnum type = ExposureTypeEnum.fromAny(targetEntity);
		return type != null ? type.getLegacyAlias() : targetEntity;
	}

	private static String toExposureCode(String targetEntity) {
		if ("Pop".equals(targetEntity) || "Xzq".equals(targetEntity)) {
			return "";
		}
		ExposureTypeEnum type = ExposureTypeEnum.fromAny(targetEntity);
		return type != null ? type.getCode() : targetEntity;
	}

	private static double km2ToWanMu(double km2) {
		return round2(km2 * 0.15d);
	}

	private static double round2(double v) {
		return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	private static double num(Object v, double def) {
		if (v == null) {
			return def;
		}
		if (v instanceof Number) {
			return ((Number) v).doubleValue();
		}
		try {
			return Double.parseDouble(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
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
