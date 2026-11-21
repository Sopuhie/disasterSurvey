package com.santech.module.biz.event.service.impl;

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

import com.santech.core.framework.dao.AssessmentRecordDao;
import com.santech.core.framework.dao.DisasterEventDao;
import com.santech.core.framework.dao.SurveyBaselineDao;
import com.santech.core.framework.entity.enums.biz.DisasterTypeEnum;
import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.core.framework.entity.model.BizDisasterEventModel;
import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.event.entity.dto.DisasterEventCreateDto;
import com.santech.module.biz.event.service.DisasterEventService;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class DisasterEventServiceImpl implements DisasterEventService {

	private static final Logger logger = LoggerFactory.getLogger(DisasterEventServiceImpl.class);
	private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DisasterEventDao disasterEventDao;
	@Resource
	private SurveyBaselineDao surveyBaselineDao;
	@Resource
	private AssessmentRecordDao assessmentRecordDao;

	@Override
	public ExecuteResult<Map<String, Object>> create(DisasterEventCreateDto dto, String operator) {
		try {
			if (dto == null || StringUtil.isEmpty(dto.getEventName())) {
				return ExecuteResult.error("eventName 必填", null);
			}
			String disasterTypes = normalizeDisasterTypes(dto.getDisasterTypes());
			if (StringUtil.isEmpty(disasterTypes)) {
				return ExecuteResult.error("disasterTypes 必填，且须为 DisasterTypeEnum 编码", null);
			}
			Date now = new Date();
			String id = "ev_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
			String code = StringUtil.isNotEmpty(dto.getEventCode()) ? dto.getEventCode().trim()
					: "DE" + new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			BizDisasterEventModel existCode = disasterEventDao.getByEventCode(code);
			if (existCode != null) {
				return ExecuteResult.error("事件编号已存在: " + code, null);
			}

			BizDisasterEventModel model = new BizDisasterEventModel();
			model.setId(id);
			model.setEventCode(code);
			model.setEventName(dto.getEventName().trim());
			model.setDisasterTypes(disasterTypes);
			model.setRegionCode(nvl(dto.getRegionCode()));
			model.setRegionName(nvl(dto.getRegionName()));
			model.setOccurTime(parseTime(dto.getOccurTime()));
			model.setStatus(StringUtil.isNotEmpty(dto.getStatus()) ? dto.getStatus().trim().toUpperCase() : "ACTIVE");
			model.setCjr(first(operator, "system"));
			model.setCjsj(now);
			model.setZhgxr(first(operator, "system"));
			model.setZhgxsj(now);
			disasterEventDao.insert(model);
			return ExecuteResult.success("创建成功", toMap(model));
		} catch (Exception e) {
			logger.error("create disaster event error", e);
			return ExecuteResult.error("创建失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> list(String keyword, String status, PageInfo pageInfo) {
		try {
			List<BizDisasterEventModel> rows;
			if (pageInfo != null) {
				rows = disasterEventDao.listWithPage(keyword, status, pageInfo);
			} else {
				rows = disasterEventDao.list(keyword, status);
			}
			List<Map<String, Object>> list = new ArrayList<>();
			if (rows != null) {
				for (BizDisasterEventModel row : rows) {
					list.add(toMap(row));
				}
			}
			if (pageInfo != null) {
				return ExecuteResult.success("查询成功", list, pageInfo);
			}
			return ExecuteResult.success("查询成功", list);
		} catch (Exception e) {
			logger.error("list disaster event error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getDetail(String eventId) {
		try {
			if (StringUtil.isEmpty(eventId)) {
				return ExecuteResult.error("eventId 必填", null);
			}
			BizDisasterEventModel event = disasterEventDao.getById(eventId);
			if (event == null) {
				return ExecuteResult.error("事件不存在", null);
			}
			Map<String, Object> detail = toMap(event);

			BizSurveyBaselineModel latest = surveyBaselineDao.getLatestByEventId(eventId);
			BizSurveyBaselineModel confirmed = surveyBaselineDao.getConfirmedByEventId(eventId);
			detail.put("baseline", baselineSummary(latest));
			detail.put("confirmedBaseline", baselineSummary(confirmed));

			List<BizAssessmentRecordModel> records = assessmentRecordDao.listByEventId(eventId);
			Map<String, Object> assessmentProgress = new HashMap<>();
			int total = records == null ? 0 : records.size();
			int succeeded = 0;
			int running = 0;
			int failed = 0;
			List<Map<String, Object>> recordBriefs = new ArrayList<>();
			if (records != null) {
				for (BizAssessmentRecordModel r : records) {
					String st = nvl(r.getStatus());
					if ("SUCCEEDED".equalsIgnoreCase(st)) {
						succeeded++;
					} else if ("RUNNING".equalsIgnoreCase(st)) {
						running++;
					} else if ("FAILED".equalsIgnoreCase(st)) {
						failed++;
					}
					Map<String, Object> brief = new HashMap<>();
					brief.put("id", r.getId());
					brief.put("disasterType", r.getDisasterType());
					brief.put("subType", r.getSubType());
					brief.put("status", r.getStatus());
					brief.put("computeMode", r.getComputeMode());
					recordBriefs.add(brief);
				}
			}
			assessmentProgress.put("total", total);
			assessmentProgress.put("succeeded", succeeded);
			assessmentProgress.put("running", running);
			assessmentProgress.put("failed", failed);
			assessmentProgress.put("records", recordBriefs);
			detail.put("assessmentProgress", assessmentProgress);
			return ExecuteResult.success("查询成功", detail);
		} catch (Exception e) {
			logger.error("get disaster event detail error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	private Map<String, Object> baselineSummary(BizSurveyBaselineModel row) {
		if (row == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("id", row.getId());
		map.put("eventId", row.getEventId());
		map.put("versionNo", row.getVersionNo());
		map.put("status", row.getStatus());
		map.put("rsImageId", row.getRsImageId());
		map.put("affectedPopulation", row.getAffectedPopulation());
		map.put("confirmedTime", row.getConfirmedTime() == null ? null : TS.format(row.getConfirmedTime()));
		map.put("confirmedBy", row.getConfirmedBy());
		map.put("district", row.getDistrict());
		map.put("sceneId", row.getSceneId());
		return map;
	}

	private Map<String, Object> toMap(BizDisasterEventModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", model.getId());
		map.put("eventId", model.getId());
		map.put("eventCode", model.getEventCode());
		map.put("eventName", model.getEventName());
		map.put("disasterTypes", model.getDisasterTypes());
		map.put("regionCode", model.getRegionCode());
		map.put("regionName", model.getRegionName());
		map.put("occurTime", model.getOccurTime() == null ? null : TS.format(model.getOccurTime()));
		map.put("status", model.getStatus());
		map.put("createBy", model.getCjr());
		map.put("createTime", model.getCjsj() == null ? null : TS.format(model.getCjsj()));
		map.put("updateTime", model.getZhgxsj() == null ? null : TS.format(model.getZhgxsj()));
		return map;
	}

	private String normalizeDisasterTypes(String raw) {
		if (StringUtil.isEmpty(raw)) {
			return "";
		}
		String[] parts = raw.split("[,，;；\\s]+");
		List<String> codes = new ArrayList<>();
		for (String p : parts) {
			if (StringUtil.isEmpty(p)) {
				continue;
			}
			DisasterTypeEnum e = DisasterTypeEnum.fromCode(p.trim());
			if (e == null) {
				return "";
			}
			if (!codes.contains(e.getCode())) {
				codes.add(e.getCode());
			}
		}
		return String.join(",", codes);
	}

	private Date parseTime(String text) {
		if (StringUtil.isEmpty(text)) {
			return null;
		}
		try {
			return TS.parse(text.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static String nvl(String v) {
		return v == null ? "" : v.trim();
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
