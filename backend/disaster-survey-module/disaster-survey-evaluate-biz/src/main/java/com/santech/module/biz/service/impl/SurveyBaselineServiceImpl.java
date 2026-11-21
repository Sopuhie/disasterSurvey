package com.santech.module.biz.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.santech.core.framework.dao.SurveyBaselineDao;
import com.santech.core.framework.entity.enums.biz.BaselineStatusEnum;
import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.SurveyBaselineService;

@Service
public class SurveyBaselineServiceImpl implements SurveyBaselineService {

	private static final Logger logger = LoggerFactory.getLogger(SurveyBaselineServiceImpl.class);
	private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private SurveyBaselineDao surveyBaselineDao;

	@Override
	public ExecuteResult<Map<String, Object>> getBySceneId(String sceneId) {
		try {
			if (StringUtil.isEmpty(sceneId)) {
				return ExecuteResult.error("sceneId 必填", null);
			}
			BizSurveyBaselineModel row = surveyBaselineDao.getBySceneId(sceneId);
			if (row == null) {
				return ExecuteResult.success("无数据", null);
			}
			return ExecuteResult.success("查询成功", toMap(row));
		} catch (Exception e) {
			logger.error("get survey baseline error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getByEventId(String eventId) {
		try {
			if (StringUtil.isEmpty(eventId)) {
				return ExecuteResult.error("eventId 必填", null);
			}
			BizSurveyBaselineModel row = surveyBaselineDao.getLatestByEventId(eventId);
			if (row == null) {
				return ExecuteResult.success("无数据", null);
			}
			return ExecuteResult.success("查询成功", toMap(row));
		} catch (Exception e) {
			logger.error("get survey baseline by event error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getLatest() {
		try {
			BizSurveyBaselineModel row = surveyBaselineDao.getLatest();
			if (row == null) {
				return ExecuteResult.success("无数据", null);
			}
			return ExecuteResult.success("查询成功", toMap(row));
		} catch (Exception e) {
			logger.error("get latest survey baseline error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> save(Map<String, Object> payload, String operator) {
		try {
			if (payload == null) {
				return ExecuteResult.error("基线内容为空", null);
			}
			String eventId = str(payload.get("eventId"));
			String sceneId = str(payload.get("sceneId"));
			if (StringUtil.isEmpty(sceneId) && StringUtil.isEmpty(eventId)) {
				return ExecuteResult.error("sceneId 或 eventId 至少填一个", null);
			}

			BizSurveyBaselineModel exist = null;
			if (StringUtil.isNotEmpty(eventId)) {
				exist = surveyBaselineDao.getLatestByEventId(eventId);
			}
			if (exist == null && StringUtil.isNotEmpty(sceneId)) {
				exist = surveyBaselineDao.getBySceneId(sceneId);
			}
			if (exist != null && isLocked(exist.getStatus())) {
				return ExecuteResult.error("调查基线已锁定(" + exist.getStatus() + ")，不可修改", toMap(exist));
			}

			String district = firstNonEmpty(str(payload.get("district")), districtName(payload.get("queriedDistrict")));
			Date now = new Date();
			String id = firstNonEmpty(str(payload.get("id")), exist != null ? exist.getId() : null,
					"sb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
			payload.put("id", id);
			if (StringUtil.isNotEmpty(sceneId)) {
				payload.put("sceneId", sceneId);
			}
			if (StringUtil.isNotEmpty(eventId)) {
				payload.put("eventId", eventId);
			}
			payload.put("district", district);
			payload.put("updatedAt", TS.format(now));
			if (StringUtil.isEmpty(str(payload.get("status")))) {
				payload.put("status", BaselineStatusEnum.DRAFT.getCode());
			}

			BizSurveyBaselineModel model = exist != null ? exist : new BizSurveyBaselineModel();
			model.setId(id);
			model.setEventId(firstNonEmpty(eventId, model.getEventId()));
			model.setSceneId(firstNonEmpty(sceneId, model.getSceneId(), eventId));
			model.setDistrict(district);
			model.setStatus(firstNonEmpty(str(payload.get("status")), BaselineStatusEnum.DRAFT.getCode()));
			if (payload.get("versionNo") != null) {
				model.setVersionNo((int) num(payload.get("versionNo"), 1));
			} else if (model.getVersionNo() == null) {
				model.setVersionNo(1);
			}
			model.setRsImageId(firstNonEmpty(str(payload.get("rsImageId")), model.getRsImageId()));
			if (payload.get("affectedAreaGeojson") != null) {
				model.setAffectedAreaGeojson(str(payload.get("affectedAreaGeojson")));
			}
			if (payload.get("affectedPopulation") != null) {
				model.setAffectedPopulation(BigDecimal.valueOf(num(payload.get("affectedPopulation"), 0)));
			}
			if (payload.get("townAreaStats") != null) {
				model.setTownAreaStatsJson(JSON.toJSONString(payload.get("townAreaStats")));
			}
			if (payload.get("exposureOverlay") != null) {
				model.setExposureOverlayJson(JSON.toJSONString(payload.get("exposureOverlay")));
			}
			if (payload.get("compareCheck") != null) {
				model.setCompareCheckJson(JSON.toJSONString(payload.get("compareCheck")));
			}
			model.setJsonData(JSON.toJSONString(payload));
			model.setRemark(str(payload.get("remark")));
			model.setZhgxr(operator);
			model.setZhgxsj(now);
			if (exist == null) {
				model.setCjr(operator);
				model.setCjsj(now);
				surveyBaselineDao.insert(model);
			} else {
				surveyBaselineDao.update(model);
			}
			return ExecuteResult.success("保存成功", toMap(model));
		} catch (Exception e) {
			logger.error("save survey baseline error", e);
			return ExecuteResult.error("保存失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> confirm(String eventId, String operator) {
		try {
			if (StringUtil.isEmpty(eventId)) {
				return ExecuteResult.error("eventId 必填", null);
			}
			BizSurveyBaselineModel baseline = surveyBaselineDao.getLatestByEventId(eventId);
			if (baseline == null) {
				return ExecuteResult.error("该事件尚无调查基线", null);
			}
			if (BaselineStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(nvl(baseline.getStatus()))) {
				return ExecuteResult.error("基线已确认锁定", toMap(baseline));
			}
			if (BaselineStatusEnum.ARCHIVED.getCode().equalsIgnoreCase(nvl(baseline.getStatus()))) {
				return ExecuteResult.error("基线已归档，无法确认", toMap(baseline));
			}
			Date now = new Date();
			baseline.setStatus(BaselineStatusEnum.CONFIRMED.getCode());
			baseline.setConfirmedBy(firstNonEmpty(operator, "system"));
			baseline.setConfirmedTime(now);
			baseline.setZhgxr(firstNonEmpty(operator, "system"));
			baseline.setZhgxsj(now);
			surveyBaselineDao.update(baseline);
			return ExecuteResult.success("调查基线已确认锁定", toMap(baseline));
		} catch (Exception e) {
			logger.error("confirm survey baseline error", e);
			return ExecuteResult.error("确认失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> list(String keyword) {
		try {
			List<BizSurveyBaselineModel> rows = surveyBaselineDao.list(keyword);
			List<Map<String, Object>> list = new ArrayList<>();
			if (rows != null) {
				for (BizSurveyBaselineModel row : rows) {
					Map<String, Object> payload = toMap(row);
					if (payload != null) {
						list.add(payload);
					}
				}
			}
			return ExecuteResult.success("查询成功", list);
		} catch (Exception e) {
			logger.error("list survey baseline error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	private Map<String, Object> toMap(BizSurveyBaselineModel row) {
		if (row == null) {
			return null;
		}
		Map<String, Object> map = parsePayload(row);
		if (map == null) {
			map = new HashMap<>();
		}
		map.put("id", row.getId());
		map.put("baselineId", row.getId());
		map.put("eventId", row.getEventId());
		map.put("versionNo", row.getVersionNo());
		map.put("status", row.getStatus());
		map.put("rsImageId", row.getRsImageId());
		map.put("affectedAreaGeojson", row.getAffectedAreaGeojson());
		map.put("affectedPopulation", row.getAffectedPopulation());
		map.put("townAreaStats", parseLoose(row.getTownAreaStatsJson()));
		map.put("exposureOverlay", parseLoose(row.getExposureOverlayJson()));
		map.put("compareCheck", parseLoose(row.getCompareCheckJson()));
		map.put("confirmedTime", row.getConfirmedTime() == null ? null : TS.format(row.getConfirmedTime()));
		map.put("confirmedBy", row.getConfirmedBy());
		map.put("sceneId", row.getSceneId());
		map.put("district", row.getDistrict());
		return map;
	}

	private Map<String, Object> parsePayload(BizSurveyBaselineModel row) {
		if (row == null || StringUtil.isEmpty(row.getJsonData())) {
			return new HashMap<>();
		}
		try {
			Map<String, Object> map = JSON.parseObject(row.getJsonData(), new TypeReference<Map<String, Object>>() {
			});
			return map == null ? new HashMap<>() : map;
		} catch (Exception e) {
			logger.warn("parse baseline json fail: {}", e.getMessage());
			Map<String, Object> fallback = new HashMap<>();
			fallback.put("raw", row.getJsonData());
			return fallback;
		}
	}

	private Object parseLoose(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		try {
			return JSON.parse(json);
		} catch (Exception e) {
			return json;
		}
	}

	private boolean isLocked(String status) {
		return BaselineStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(nvl(status))
				|| BaselineStatusEnum.ARCHIVED.getCode().equalsIgnoreCase(nvl(status));
	}

	@SuppressWarnings("unchecked")
	private String districtName(Object districtObj) {
		if (districtObj instanceof Map) {
			Object name = ((Map<String, Object>) districtObj).get("name");
			return name == null ? "" : String.valueOf(name);
		}
		return "";
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

	private static String firstNonEmpty(String... vals) {
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
