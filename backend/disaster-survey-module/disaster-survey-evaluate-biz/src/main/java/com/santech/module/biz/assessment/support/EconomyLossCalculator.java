package com.santech.module.biz.assessment.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.BizFrontendDataService;

/**
 * 县域经济底数与分项经济损失测算（风暴潮评估复用）。
 */
@Component
public class EconomyLossCalculator {

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

	public Map<String, Object> resolveEconomy(String districtName) {
		Map<String, Object> economy = new HashMap<>();
		economy.put("name", nvl(districtName));
		economy.put("gdp", 320d);
		Map<String, Double> land = new LinkedHashMap<>(DEFAULT_LAND_PRICE);
		Map<String, Double> ratios = new LinkedHashMap<>();
		ratios.put("耕地", 0.28);
		ratios.put("建设用地", 0.18);
		ratios.put("养殖水面", 0.16);
		ratios.put("林地", 0.22);
		ratios.put("水域", 0.16);

		for (Map<String, Object> row : listBiz("assessCountyEconomy")) {
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
		for (Map<String, Object> row : listBiz("assessLandPrice")) {
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
	public Map<String, Object> calcEconomicLoss(Map<String, Object> economy, double affectedKm2) {
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

	private List<Map<String, Object>> listBiz(String dataType) {
		ExecuteResult<List<Map<String, Object>>> res = bizFrontendDataService.list(dataType, null, null);
		return res != null && res.getData() != null ? res.getData() : new ArrayList<>();
	}

	public static double round2(double v) {
		return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public static double num(Object v, double def) {
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

	public static String str(Object v) {
		return v == null ? "" : String.valueOf(v);
	}

	public static String nvl(String v) {
		return v == null ? "" : v;
	}
}
