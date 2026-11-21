package com.santech.module.biz.assessment.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.BizFrontendDataDao;
import com.santech.core.framework.entity.enums.biz.AssessmentSubTypeEnum;
import com.santech.core.framework.entity.enums.biz.DisasterTypeEnum;
import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.core.framework.entity.model.BizDisasterEventModel;
import com.santech.core.framework.entity.model.BizReportArchiveModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.assessment.service.AssessmentReportService;
import com.santech.module.biz.assessment.support.AssessmentRecordSupport;

@Service
public class AssessmentReportServiceImpl implements AssessmentReportService {

	private static final Logger logger = LoggerFactory.getLogger(AssessmentReportServiceImpl.class);
	private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyyMMddHHmmss");

	@Resource
	private AssessmentRecordSupport recordSupport;
	@Resource
	private BizFrontendDataDao bizFrontendDataDao;
	@Resource
	private FileBasicConfig fileBasicConfig;

	@Override
	public ExecuteResult<List<Map<String, Object>>> listRecords(String eventId, String disasterType) {
		try {
			recordSupport.requireEvent(eventId);
			List<BizAssessmentRecordModel> rows = StringUtil.isNotEmpty(disasterType)
					? recordSupport.dao().listByEventIdAndDisasterType(eventId, normalizeDisasterType(disasterType))
					: recordSupport.dao().listByEventId(eventId);
			List<Map<String, Object>> list = new ArrayList<>();
			if (rows != null) {
				for (BizAssessmentRecordModel row : rows) {
					list.add(recordSupport.toMap(row));
				}
			}
			return ExecuteResult.success("查询成功", list);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), new ArrayList<>());
		} catch (Exception e) {
			logger.error("list assessment records error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> generateReport(String eventId, Map<String, Object> body,
			String operator) {
		try {
			BizDisasterEventModel event = recordSupport.requireEvent(eventId);
			Map<String, Object> input = body == null ? new HashMap<>() : new HashMap<>(body);
			String disasterType = normalizeDisasterType(
					first(str(input.get("disasterType")), pickPrimaryDisasterType(event.getDisasterTypes())));
			DisasterTypeEnum typeEnum = DisasterTypeEnum.fromCode(disasterType);
			if (typeEnum == null) {
				return ExecuteResult.error("disasterType 无效", null);
			}

			AssessmentSubTypeEnum comprehensiveSub = comprehensiveOf(typeEnum);
			BizAssessmentRecordModel comprehensive = recordSupport.dao().getByEventAndSubType(eventId,
					comprehensiveSub.getCode());
			if (comprehensive == null) {
				return ExecuteResult.error("请先完成该灾种综合评估后再生成报告", null);
			}

			List<BizAssessmentRecordModel> records = recordSupport.dao().listByEventIdAndDisasterType(eventId,
					typeEnum.getCode());
			Map<String, Object> summary = parseMap(comprehensive.getResultDataJson());
			Map<String, Object> reportPayload = new LinkedHashMap<>();
			reportPayload.put("eventId", eventId);
			reportPayload.put("eventCode", event.getEventCode());
			reportPayload.put("eventName", event.getEventName());
			reportPayload.put("disasterType", typeEnum.getCode());
			reportPayload.put("disasterLabel", typeEnum.getLabel());
			reportPayload.put("regionCode", event.getRegionCode());
			reportPayload.put("regionName", event.getRegionName());
			reportPayload.put("assessmentId", comprehensive.getId());
			reportPayload.put("totalScore", summary.get("totalScore"));
			reportPayload.put("grade", summary.get("grade"));
			reportPayload.put("factors", summary.get("factors"));
			reportPayload.put("recommend", summary.get("recommend"));
			reportPayload.put("generatedAt", TS.format(new Date()));
			List<Map<String, Object>> recordBriefs = new ArrayList<>();
			if (records != null) {
				for (BizAssessmentRecordModel r : records) {
					recordBriefs.add(recordSupport.toMap(r));
				}
			}
			reportPayload.put("records", recordBriefs);

			String reportId = "ar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
			String title = first(str(input.get("reportTitle")),
					event.getEventName() + "-" + typeEnum.getLabel() + "综合评估报告");
			String format = first(str(input.get("reportFormat")), "JSON").toUpperCase();
			String relativePath = writeReportFile(reportId, reportPayload, format);

			BizReportArchiveModel archive = new BizReportArchiveModel();
			archive.setId(reportId);
			archive.setEventId(eventId);
			archive.setAssessmentId(comprehensive.getId());
			archive.setDisasterType(typeEnum.getCode());
			archive.setReportFilePath(relativePath);
			archive.setReportFormat(format);
			archive.setSummaryJson(JSON.toJSONString(summary));
			archive.setReportNo("RPT" + DAY.format(new Date()));
			archive.setTitle(title);
			archive.setTypeId(typeEnum.getCode());
			archive.setTypeLabel(typeEnum.getLabel());
			archive.setDistrict(event.getRegionName());
			archive.setSceneName(str(input.get("sceneName")));
			archive.setGrade(str(summary.get("grade")));
			Object score = summary.get("totalScore");
			if (score instanceof Number) {
				archive.setTotalScore(java.math.BigDecimal.valueOf(((Number) score).doubleValue()));
			}
			archive.setArchivedAt(TS.format(new Date()));
			archive.setArchivedBy(first(operator, "system"));
			archive.setSnapshot(JSON.toJSONString(reportPayload));
			archive.setCjr(first(operator, "system"));
			archive.setCjsj(new Date());
			archive.setZhgxr(first(operator, "system"));
			archive.setZhgxsj(new Date());
			bizFrontendDataDao.insertArchive(archive);

			Map<String, Object> out = archiveToMap(archive, true);
			out.put("downloadPath", relativePath);
			return ExecuteResult.success("报告生成并归档成功", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("generate assessment report error", e);
			return ExecuteResult.error("报告生成失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> listReports(String disasterType, String keyword) {
		try {
			List<BizReportArchiveModel> rows = bizFrontendDataDao.listArchives();
			List<Map<String, Object>> list = new ArrayList<>();
			String type = normalizeDisasterType(disasterType);
			for (BizReportArchiveModel row : rows) {
				if (StringUtil.isNotEmpty(type) && !type.equalsIgnoreCase(nvl(row.getDisasterType()))
						&& !type.equalsIgnoreCase(nvl(row.getTypeId()))) {
					continue;
				}
				if (StringUtil.isNotEmpty(keyword)) {
					String blob = nvl(row.getTitle()) + nvl(row.getReportNo()) + nvl(row.getDistrict())
							+ nvl(row.getEventId());
					if (!blob.contains(keyword)) {
						continue;
					}
				}
				list.add(archiveToMap(row, false));
			}
			return ExecuteResult.success("查询成功", list);
		} catch (Exception e) {
			logger.error("list assessment reports error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getReport(String reportId) {
		try {
			if (StringUtil.isEmpty(reportId)) {
				return ExecuteResult.error("reportId 必填", null);
			}
			BizReportArchiveModel row = bizFrontendDataDao.getArchiveById(reportId);
			if (row == null) {
				return ExecuteResult.error("报告不存在", null);
			}
			return ExecuteResult.success("查询成功", archiveToMap(row, true));
		} catch (Exception e) {
			logger.error("get assessment report error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	private String writeReportFile(String reportId, Map<String, Object> payload, String format) throws Exception {
		String relativePath = "assessment_report" + File.separator + reportId + File.separator
				+ ("PDF".equals(format) ? "综合评估报告.pdf" : "综合评估报告.json");
		String full = getFileRootPath() + relativePath;
		File file = new File(full);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		// 当前环境以结构化 JSON 归档；Word/PDF 模板可在后续接入
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			writer.write(JSON.toJSONString(payload, true));
		}
		return relativePath;
	}

	private Map<String, Object> archiveToMap(BizReportArchiveModel row, boolean withSnapshot) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", row.getId());
		map.put("reportId", row.getId());
		map.put("eventId", row.getEventId());
		map.put("assessmentId", row.getAssessmentId());
		map.put("disasterType", row.getDisasterType());
		map.put("reportFilePath", row.getReportFilePath());
		map.put("reportFormat", row.getReportFormat());
		map.put("reportNo", row.getReportNo());
		map.put("reportTitle", row.getTitle());
		map.put("title", row.getTitle());
		map.put("typeId", row.getTypeId());
		map.put("typeLabel", row.getTypeLabel());
		map.put("district", row.getDistrict());
		map.put("sceneName", row.getSceneName());
		map.put("grade", row.getGrade());
		map.put("totalScore", row.getTotalScore());
		map.put("archivedAt", row.getArchivedAt());
		map.put("archivedBy", row.getArchivedBy());
		map.put("summary", parseJson(row.getSummaryJson()));
		if (withSnapshot) {
			map.put("snapshot", parseJson(row.getSnapshot()));
		}
		return map;
	}

	private AssessmentSubTypeEnum comprehensiveOf(DisasterTypeEnum type) {
		if (type == DisasterTypeEnum.SEA_WIND) {
			return AssessmentSubTypeEnum.WIND_COMPREHENSIVE;
		}
		if (type == DisasterTypeEnum.SEA_WAVE) {
			return AssessmentSubTypeEnum.WAVE_COMPREHENSIVE;
		}
		return AssessmentSubTypeEnum.STORM_COMPREHENSIVE;
	}

	private String pickPrimaryDisasterType(String disasterTypes) {
		if (StringUtil.isEmpty(disasterTypes)) {
			return DisasterTypeEnum.STORM_SURGE.getCode();
		}
		String first = disasterTypes.split("[,，]")[0].trim();
		return normalizeDisasterType(first);
	}

	private String normalizeDisasterType(String raw) {
		if (StringUtil.isEmpty(raw)) {
			return "";
		}
		DisasterTypeEnum e = DisasterTypeEnum.fromCode(raw.trim());
		if (e != null) {
			return e.getCode();
		}
		if ("1".equals(raw) || "storm".equalsIgnoreCase(raw) || "economic".equalsIgnoreCase(raw)) {
			return DisasterTypeEnum.STORM_SURGE.getCode();
		}
		if ("2".equals(raw) || "wind".equalsIgnoreCase(raw) || "seabreeze".equalsIgnoreCase(raw)) {
			return DisasterTypeEnum.SEA_WIND.getCode();
		}
		if ("3".equals(raw) || "wave".equalsIgnoreCase(raw) || "seawave".equalsIgnoreCase(raw)) {
			return DisasterTypeEnum.SEA_WAVE.getCode();
		}
		return raw.trim().toUpperCase();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseMap(String json) {
		Object obj = parseJson(json);
		if (obj instanceof Map) {
			return (Map<String, Object>) obj;
		}
		return new HashMap<>();
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

	private String getFileRootPath() {
		String names = System.getProperty("os.name");
		String base = names != null && names.toLowerCase().startsWith("win") ? fileBasicConfig.getRootForWin()
				: fileBasicConfig.getRootForLiunx();
		if (base == null) {
			base = "";
		}
		if (!base.endsWith("/") && !base.endsWith("\\")) {
			base = base + File.separator;
		}
		return base;
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
