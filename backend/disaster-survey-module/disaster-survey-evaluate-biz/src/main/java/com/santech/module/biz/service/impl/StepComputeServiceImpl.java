package com.santech.module.biz.service.impl;

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
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskSaveParamsDto;
import com.santech.module.biz.entity.dto.EvaluateHistorySaveParamsDto;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;
import com.santech.module.biz.service.BizFrontendDataService;
import com.santech.module.biz.service.DisasterSurveyTaskService;
import com.santech.module.biz.service.EvaluateTaskService;
import com.santech.module.biz.service.ExposureDataService;
import com.santech.module.biz.service.StepComputeService;

/**
 * 对齐前端步骤 UI 的服务端推算，并可落库 lite 任务。
 */
@Service
public class StepComputeServiceImpl implements StepComputeService {

	private static final Logger logger = LoggerFactory.getLogger(StepComputeServiceImpl.class);

	private static final Map<String, String> TARGET_ALIAS = new HashMap<>();
	static {
		TARGET_ALIAS.put("population", "Pop");
		TARGET_ALIAS.put("district", "Xzq");
		TARGET_ALIAS.put("seawall", "Dike");
		TARGET_ALIAS.put("fishing", "Fishery");
		TARGET_ALIAS.put("mariculture", "CultureArea");
		TARGET_ALIAS.put("tourist", "TouristArea");
		TARGET_ALIAS.put("Pop", "Pop");
		TARGET_ALIAS.put("Xzq", "Xzq");
		TARGET_ALIAS.put("Dike", "Dike");
		TARGET_ALIAS.put("Fishery", "Fishery");
		TARGET_ALIAS.put("CultureArea", "CultureArea");
		TARGET_ALIAS.put("TouristArea", "TouristArea");
	}

	private static final Map<String, Double> DEFAULT_LAND_PRICE = new LinkedHashMap<>();
	static {
		DEFAULT_LAND_PRICE.put("耕地", 48d);
		DEFAULT_LAND_PRICE.put("建设用地", 220d);
		DEFAULT_LAND_PRICE.put("养殖水面", 95d);
		DEFAULT_LAND_PRICE.put("林地", 28d);
		DEFAULT_LAND_PRICE.put("水域", 18d);
	}

	@Resource
	private BizFrontendDataService bizFrontendDataService;
	@Resource
	private ExposureDataService exposureDataService;
	@Resource
	private DisasterSurveyTaskService disasterSurveyTaskService;
	@Resource
	private EvaluateTaskService evaluateTaskService;

	@Override
	public ExecuteResult<Map<String, Object>> computeIdentify(StepComputeRequestDto dto) {
		try {
			Map<String, Object> meta = buildIdentifyMeta(dto);
			Map<String, Object> resultData = new HashMap<>();
			resultData.put("kind", "identify");
			resultData.put("sceneId", nvl(dto.getSceneId()));
			resultData.put("district", nvl(dto.getDistrictName()));
			resultData.put("meta", meta);

			String taskId = maybePersistSurvey(dto, "Pop", "受灾范围识别-" + first(dto.getSceneName(), dto.getSceneId(), "default"),
					resultData);

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
				pack = buildDistrictOverlay();
			} else {
				pack = buildHazardOverlay(target, dto.getHazardName());
			}

			String frontendTarget = toFrontendTarget(target);
			Map<String, Object> resultData = new HashMap<>();
			resultData.put("kind", "overlay");
			resultData.put("sceneId", nvl(dto.getSceneId()));
			resultData.put("targetId", frontendTarget);
			resultData.put("hazardName", nvl(dto.getHazardName()));
			resultData.put("totals", pack.get("totals"));
			resultData.put("rows", pack.get("rows"));

			String taskId = maybePersistSurvey(dto, target,
					"叠置分析-" + frontendTarget + (StringUtil.isNotEmpty(dto.getHazardName()) ? "-" + dto.getHazardName() : ""),
					resultData);

			Map<String, Object> out = new HashMap<>();
			out.put("kind", "overlay");
			out.put("targetId", frontendTarget);
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
	public ExecuteResult<Map<String, Object>> computeLoss(StepComputeRequestDto dto) {
		try {
			double ratio = dto.getInundationRatio() == null ? 0.46d : dto.getInundationRatio();
			double statArea = dto.getStatArea() == null ? 680d : dto.getStatArea();
			double density = dto.getDensity() == null ? 500d : dto.getDensity();
			double affectedKm2 = round2(statArea * 0.078d * (0.72d + ratio));
			int affectedPop = (int) Math.round(density * affectedKm2);

			Map<String, Object> economy = resolveEconomy(dto.getDistrictName());
			Map<String, Object> loss = calcEconomicLoss(economy, affectedKm2);

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
			resultData.put("typeId", disasterToTypeId(dto.getDisasterType()));
			resultData.put("sceneId", nvl(dto.getSceneId()));
			resultData.put("district", nvl(dto.getDistrictName()));
			resultData.put("lossResult", lossResult);

			String taskId = maybePersistEvaluate(dto, resultData, lossResult);

			Map<String, Object> out = new HashMap<>();
			out.put("kind", "loss");
			out.put("lossResult", lossResult);
			out.put("taskId", taskId);
			out.put("resultData", resultData);
			return ExecuteResult.success("损失计算成功", out);
		} catch (Exception e) {
			logger.error("computeLoss error", e);
			return ExecuteResult.error("损失计算失败:" + e.getMessage(), null);
		}
	}

	private Map<String, Object> buildIdentifyMeta(StepComputeRequestDto dto) {
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

	private Map<String, Object> buildDistrictOverlay() {
		List<Map<String, Object>> towns = listBiz("town");
		List<Map<String, Object>> rows = new ArrayList<>();
		double areaSum = 0;
		for (Map<String, Object> item : towns) {
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
		ExecuteResult<List<Map<String, Object>>> res = exposureDataService.queryExposureList(targetEntity, null);
		List<Map<String, Object>> list = res != null && res.getData() != null ? res.getData() : new ArrayList<>();
		if (list.isEmpty()) {
			String dataType = toBizHazardType(targetEntity);
			if (StringUtil.isNotEmpty(dataType)) {
				list = listBiz(dataType);
			}
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
			double lengthKm = round2(num(item.get("lengthKm"), num(item.get("length"), 0)) * Math.max(0.35, 0.72 - i * 0.08));
			Map<String, Object> row = new HashMap<>();
			row.put("name", first(str(item.get("name")), str(item.get("dangerName"))));
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
		return pack;
	}

	private Map<String, Object> resolveEconomy(String districtName) {
		Map<String, Object> economy = new HashMap<>();
		economy.put("name", nvl(districtName));
		economy.put("gdp", 320d);
		Map<String, Double> land = new LinkedHashMap<>(DEFAULT_LAND_PRICE);
		// overwrite ratios from county economy if present
		Map<String, Double> ratios = new LinkedHashMap<>();
		ratios.put("耕地", 0.28);
		ratios.put("建设用地", 0.18);
		ratios.put("养殖水面", 0.16);
		ratios.put("林地", 0.22);
		ratios.put("水域", 0.16);

		List<Map<String, Object>> econRows = listBiz("assessCountyEconomy");
		for (Map<String, Object> row : econRows) {
			if (StringUtil.isNotEmpty(districtName) && districtName.equals(str(row.get("name")))) {
				economy.put("gdp", num(row.get("gdp"), 320));
				if (row.get("landFarm") != null) {
					ratios.put("耕地", num(row.get("landFarm"), 0.28));
					ratios.put("建设用地", num(row.get("landBuild"), 0.18));
					ratios.put("养殖水面", num(row.get("landAqua"), 0.16));
					ratios.put("林地", num(row.get("landForest"), 0.22));
					ratios.put("水域", num(row.get("landWater"), 0.16));
				}
				break;
			}
		}
		List<Map<String, Object>> priceRows = listBiz("assessLandPrice");
		for (Map<String, Object> row : priceRows) {
			String type = str(row.get("type"));
			if (StringUtil.isNotEmpty(type) && row.get("unit") != null) {
				land.put(type, num(row.get("unit"), land.getOrDefault(type, 50d)));
			}
		}
		economy.put("land", ratios);
		economy.put("_prices", land);
		return economy;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> calcEconomicLoss(Map<String, Object> economy, double affectedKm2) {
		Map<String, Double> ratios = (Map<String, Double>) economy.get("land");
		Map<String, Double> prices = (Map<String, Double>) economy.get("_prices");
		if (ratios == null) {
			ratios = new LinkedHashMap<>();
		}
		if (prices == null) {
			prices = DEFAULT_LAND_PRICE;
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		double totalLoss = 0;
		for (Map.Entry<String, Double> e : ratios.entrySet()) {
			double ratio = e.getValue() == null ? 0 : e.getValue();
			double area = round2(affectedKm2 * ratio);
			double unit = prices.getOrDefault(e.getKey(), 50d);
			double loss = round2(area * unit);
			Map<String, Object> row = new HashMap<>();
			row.put("type", e.getKey());
			row.put("ratio", ratio);
			row.put("ratioPct", (int) Math.round(ratio * 100));
			row.put("area", area);
			row.put("unit", unit);
			row.put("loss", loss);
			rows.add(row);
			totalLoss += loss;
		}
		totalLoss = round2(totalLoss);
		double gdp = num(economy.get("gdp"), 1);
		double gdpShock = round2((totalLoss / 10000d / gdp) * 100d);
		Map<String, Object> out = new HashMap<>();
		out.put("rows", rows);
		out.put("totalLoss", totalLoss);
		out.put("gdp", gdp);
		out.put("gdpShock", gdpShock);
		out.put("affectedKm2", affectedKm2);
		economy.remove("_prices");
		return out;
	}

	private String maybePersistSurvey(StepComputeRequestDto dto, String targetEntity, String name, Map<String, Object> resultData) {
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

	private String maybePersistEvaluate(StepComputeRequestDto dto, Map<String, Object> resultData, Map<String, Object> lossResult) {
		boolean persist = dto.getPersist() == null || dto.getPersist();
		if (!persist) {
			return "";
		}
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
		String mapped = TARGET_ALIAS.get(raw);
		return mapped != null ? mapped : raw;
	}

	private static String toFrontendTarget(String targetEntity) {
		if ("Pop".equals(targetEntity)) return "population";
		if ("Xzq".equals(targetEntity)) return "district";
		if ("Dike".equals(targetEntity)) return "seawall";
		if ("Fishery".equals(targetEntity)) return "fishing";
		if ("CultureArea".equals(targetEntity)) return "mariculture";
		if ("TouristArea".equals(targetEntity)) return "tourist";
		return targetEntity;
	}

	private static String toBizHazardType(String targetEntity) {
		if ("Dike".equals(targetEntity)) return "seawall";
		if ("Fishery".equals(targetEntity)) return "fishing";
		if ("CultureArea".equals(targetEntity)) return "mariculture";
		if ("TouristArea".equals(targetEntity)) return "tourist";
		return "";
	}

	private static String disasterToTypeId(String disasterType) {
		if ("2".equals(disasterType)) return "wind";
		if ("3".equals(disasterType)) return "wave";
		return "storm";
	}

	private static double km2ToWanMu(double km2) {
		return round2(km2 * 0.15d);
	}

	private static double round2(double v) {
		return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	private static double num(Object v, double def) {
		if (v == null) return def;
		if (v instanceof Number) return ((Number) v).doubleValue();
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
		if (vals == null) return "";
		for (String v : vals) {
			if (StringUtil.isNotEmpty(v)) return v;
		}
		return "";
	}
}
