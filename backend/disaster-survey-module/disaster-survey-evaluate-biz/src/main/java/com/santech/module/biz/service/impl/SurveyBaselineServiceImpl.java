package com.santech.module.biz.service.impl;

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
import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.SurveyBaselineService;

@Service
public class SurveyBaselineServiceImpl implements SurveyBaselineService {

	private static final Logger logger = LoggerFactory.getLogger(SurveyBaselineServiceImpl.class);

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
			return ExecuteResult.success("查询成功", parsePayload(row));
		} catch (Exception e) {
			logger.error("get survey baseline error", e);
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
			return ExecuteResult.success("查询成功", parsePayload(row));
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
			String sceneId = str(payload.get("sceneId"));
			if (StringUtil.isEmpty(sceneId)) {
				return ExecuteResult.error("sceneId 必填", null);
			}
			String district = firstNonEmpty(str(payload.get("district")), districtName(payload.get("queriedDistrict")));
			BizSurveyBaselineModel exist = surveyBaselineDao.getBySceneId(sceneId);
			Date now = new Date();
			String id = firstNonEmpty(str(payload.get("id")), exist != null ? exist.getId() : null,
					"sb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
			payload.put("id", id);
			payload.put("sceneId", sceneId);
			payload.put("district", district);
			payload.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));

			BizSurveyBaselineModel model = exist != null ? exist : new BizSurveyBaselineModel();
			model.setId(id);
			model.setSceneId(sceneId);
			model.setDistrict(district);
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
			return ExecuteResult.success("保存成功", payload);
		} catch (Exception e) {
			logger.error("save survey baseline error", e);
			return ExecuteResult.error("保存失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> list(String keyword) {
		try {
			List<BizSurveyBaselineModel> rows = surveyBaselineDao.list(keyword);
			List<Map<String, Object>> list = new ArrayList<>();
			if (rows != null) {
				for (BizSurveyBaselineModel row : rows) {
					Map<String, Object> payload = parsePayload(row);
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

	private Map<String, Object> parsePayload(BizSurveyBaselineModel row) {
		if (row == null || StringUtil.isEmpty(row.getJsonData())) {
			return null;
		}
		try {
			Map<String, Object> map = JSON.parseObject(row.getJsonData(), new TypeReference<Map<String, Object>>() {
			});
			if (map == null) {
				map = new HashMap<>();
			}
			map.put("id", row.getId());
			map.put("sceneId", row.getSceneId());
			map.put("district", row.getDistrict());
			return map;
		} catch (Exception e) {
			logger.warn("parse baseline json fail: {}", e.getMessage());
			Map<String, Object> fallback = new HashMap<>();
			fallback.put("id", row.getId());
			fallback.put("sceneId", row.getSceneId());
			fallback.put("district", row.getDistrict());
			fallback.put("raw", row.getJsonData());
			return fallback;
		}
	}

	@SuppressWarnings("unchecked")
	private String districtName(Object districtObj) {
		if (districtObj instanceof Map) {
			Object name = ((Map<String, Object>) districtObj).get("name");
			return name == null ? "" : String.valueOf(name);
		}
		return "";
	}

	private static String str(Object v) {
		return v == null ? "" : String.valueOf(v);
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
