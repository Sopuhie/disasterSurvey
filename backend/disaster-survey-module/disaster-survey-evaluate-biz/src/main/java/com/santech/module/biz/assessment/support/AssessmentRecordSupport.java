package com.santech.module.biz.assessment.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.dao.AssessmentRecordDao;
import com.santech.core.framework.dao.DisasterEventDao;
import com.santech.core.framework.dao.SurveyBaselineDao;
import com.santech.core.framework.entity.enums.biz.AssessmentSubTypeEnum;
import com.santech.core.framework.entity.enums.biz.BaselineStatusEnum;
import com.santech.core.framework.entity.enums.biz.ComputeTaskStatusEnum;
import com.santech.core.framework.entity.enums.biz.DisasterTypeEnum;
import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.core.framework.entity.model.BizDisasterEventModel;
import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.utils.tool.StringUtil;

/**
 * 评估记录落库与事件/基线校验公共能力。
 */
@Component
public class AssessmentRecordSupport {

	private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DisasterEventDao disasterEventDao;
	@Resource
	private SurveyBaselineDao surveyBaselineDao;
	@Resource
	private AssessmentRecordDao assessmentRecordDao;

	public BizDisasterEventModel requireEvent(String eventId) throws Exception {
		if (StringUtil.isEmpty(eventId)) {
			throw new IllegalStateException("eventId 必填");
		}
		BizDisasterEventModel event = disasterEventDao.getById(eventId);
		if (event == null) {
			throw new IllegalStateException("灾害事件不存在: " + eventId);
		}
		return event;
	}

	public BizSurveyBaselineModel findConfirmedBaseline(String eventId) throws Exception {
		return surveyBaselineDao.getConfirmedByEventId(eventId);
	}

	public BizSurveyBaselineModel requireConfirmedBaseline(String eventId) throws Exception {
		BizSurveyBaselineModel baseline = findConfirmedBaseline(eventId);
		if (baseline == null
				|| !BaselineStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(nvl(baseline.getStatus()))) {
			throw new IllegalStateException("风暴潮评估需要已确认的调查基线，请先完成灾情调查并锁定基线");
		}
		return baseline;
	}

	public BizSurveyBaselineModel findLatestBaseline(String eventId) throws Exception {
		return surveyBaselineDao.getLatestByEventId(eventId);
	}

	public BizAssessmentRecordModel upsertSyncRecord(String eventId, DisasterTypeEnum disasterType,
			AssessmentSubTypeEnum subType, String regionCode, String baselineId, Map<String, Object> input,
			Map<String, Object> result, String operator) throws Exception {
		return upsertRecord(eventId, disasterType, subType, regionCode, baselineId, input, result, "SYNC",
				ComputeTaskStatusEnum.SUCCEEDED, operator);
	}

	public BizAssessmentRecordModel upsertAsyncRunning(String eventId, DisasterTypeEnum disasterType,
			AssessmentSubTypeEnum subType, String regionCode, String baselineId, Map<String, Object> input,
			String operator) throws Exception {
		return upsertRecord(eventId, disasterType, subType, regionCode, baselineId, input, null, "ASYNC",
				ComputeTaskStatusEnum.RUNNING, operator);
	}

	public BizAssessmentRecordModel upsertRecord(String eventId, DisasterTypeEnum disasterType,
			AssessmentSubTypeEnum subType, String regionCode, String baselineId, Map<String, Object> input,
			Map<String, Object> result, String computeMode, ComputeTaskStatusEnum status, String operator)
			throws Exception {
		BizAssessmentRecordModel exist = assessmentRecordDao.getByEventAndSubType(eventId, subType.getCode());
		Date now = new Date();
		BizAssessmentRecordModel model = exist != null ? exist : new BizAssessmentRecordModel();
		if (exist == null) {
			model.setId("as_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
			model.setCjr(first(operator, "system"));
			model.setCjsj(now);
		}
		model.setEventId(eventId);
		model.setBaselineId(baselineId);
		model.setDisasterType(disasterType.getCode());
		model.setSubType(subType.getCode());
		model.setRegionCode(nvl(regionCode));
		model.setInputParamsJson(input == null ? null : JSON.toJSONString(input));
		model.setResultDataJson(result == null ? null : JSON.toJSONString(result));
		model.setComputeMode(computeMode);
		model.setStatus(status.getCode());
		model.setZhgxr(first(operator, "system"));
		model.setZhgxsj(now);
		if (exist == null) {
			assessmentRecordDao.insert(model);
		} else {
			assessmentRecordDao.update(model);
		}
		return model;
	}

	public Map<String, Object> toMap(BizAssessmentRecordModel row) {
		Map<String, Object> map = new HashMap<>();
		if (row == null) {
			return map;
		}
		map.put("id", row.getId());
		map.put("assessmentId", row.getId());
		map.put("eventId", row.getEventId());
		map.put("baselineId", row.getBaselineId());
		map.put("disasterType", row.getDisasterType());
		map.put("subType", row.getSubType());
		map.put("regionCode", row.getRegionCode());
		map.put("computeMode", row.getComputeMode());
		map.put("status", row.getStatus());
		map.put("inputParams", parseJson(row.getInputParamsJson()));
		map.put("resultData", parseJson(row.getResultDataJson()));
		map.put("createBy", row.getCjr());
		map.put("createTime", row.getCjsj() == null ? null : TS.format(row.getCjsj()));
		map.put("updateTime", row.getZhgxsj() == null ? null : TS.format(row.getZhgxsj()));
		return map;
	}

	public AssessmentRecordDao dao() {
		return assessmentRecordDao;
	}

	private Object parseJson(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		try {
			return JSON.parse(json);
		} catch (Exception e) {
			return json;
		}
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
