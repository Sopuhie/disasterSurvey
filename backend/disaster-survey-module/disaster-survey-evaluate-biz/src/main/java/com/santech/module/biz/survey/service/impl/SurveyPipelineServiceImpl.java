package com.santech.module.biz.survey.service.impl;

import java.math.BigDecimal;
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
import com.santech.core.framework.dao.DisasterEventDao;
import com.santech.core.framework.dao.HazardDossierDao;
import com.santech.core.framework.dao.SurveyBaselineDao;
import com.santech.core.framework.entity.enums.biz.BaselineStatusEnum;
import com.santech.core.framework.entity.enums.biz.ExposureTypeEnum;
import com.santech.core.framework.entity.model.BizDisasterEventModel;
import com.santech.core.framework.entity.model.BizHazardDossierModel;
import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;
import com.santech.module.biz.service.BizFrontendDataService;
import com.santech.module.biz.service.ExposureDataService;
import com.santech.module.biz.survey.service.SurveyOverlayComputeService;
import com.santech.module.biz.survey.service.SurveyPipelineService;

@Service
public class SurveyPipelineServiceImpl implements SurveyPipelineService {

	private static final Logger logger = LoggerFactory.getLogger(SurveyPipelineServiceImpl.class);
	private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private DisasterEventDao disasterEventDao;
	@Resource
	private SurveyBaselineDao surveyBaselineDao;
	@Resource
	private HazardDossierDao hazardDossierDao;
	@Resource
	private BizFrontendDataService bizFrontendDataService;
	@Resource
	private ExposureDataService exposureDataService;
	@Resource
	private SurveyOverlayComputeService surveyOverlayComputeService;

	@Override
	public ExecuteResult<Map<String, Object>> queryRegionPopulation(String regionCode, String regionName, Double lon,
			Double lat) {
		try {
			List<Map<String, Object>> counties = listBiz("county");
			List<Map<String, Object>> matched = new ArrayList<>();
			for (Map<String, Object> item : counties) {
				boolean hit = true;
				if (StringUtil.isNotEmpty(regionCode)) {
					String code = first(str(item.get("code")), str(item.get("regionCode")));
					hit = regionCode.equals(code) || code.startsWith(regionCode) || regionCode.startsWith(code);
				}
				if (hit && StringUtil.isNotEmpty(regionName)) {
					String name = str(item.get("name"));
					hit = name.contains(regionName) || regionName.contains(name);
				}
				if (hit) {
					Map<String, Object> row = new HashMap<>(item);
					row.put("regionCode", first(str(item.get("code")), str(item.get("regionCode"))));
					row.put("regionName", str(item.get("name")));
					row.put("density", num(item.get("density"), 0));
					row.put("statArea", num(item.get("statArea"), num(item.get("areaKm2"), 0)));
					row.put("population", num(item.get("population"),
							num(item.get("density"), 0) * num(item.get("statArea"), num(item.get("areaKm2"), 0))));
					matched.add(row);
				}
			}
			Map<String, Object> out = new HashMap<>();
			out.put("query", queryHint(regionCode, regionName, lon, lat));
			out.put("count", matched.size());
			out.put("rows", matched);
			if (!matched.isEmpty()) {
				out.put("primary", matched.get(0));
			}
			return ExecuteResult.success("查询成功", out);
		} catch (Exception e) {
			logger.error("queryRegionPopulation error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> queryBaseExposures(String regionCode, String regionName,
			String exposureType, String keyword) {
		try {
			Map<String, Object> grouped = new LinkedHashMap<>();
			List<ExposureTypeEnum> types = new ArrayList<>();
			if (StringUtil.isNotEmpty(exposureType)) {
				ExposureTypeEnum one = ExposureTypeEnum.fromAny(exposureType);
				if (one == null) {
					return ExecuteResult.error("不能识别的承灾体类型，请使用 ExposureTypeEnum 编码", null);
				}
				types.add(one);
			} else {
				for (ExposureTypeEnum t : ExposureTypeEnum.values()) {
					types.add(t);
				}
			}
			int total = 0;
			for (ExposureTypeEnum type : types) {
				ExecuteResult<List<Map<String, Object>>> res = exposureDataService.queryExposureList(type.getCode(),
						keyword);
				List<Map<String, Object>> list = res != null && res.getData() != null ? res.getData()
						: new ArrayList<>();
				List<Map<String, Object>> filtered = filterByRegion(list, regionCode, regionName);
				grouped.put(type.getCode(), filtered);
				total += filtered.size();
			}
			Map<String, Object> out = new HashMap<>();
			out.put("regionCode", nvl(regionCode));
			out.put("regionName", nvl(regionName));
			out.put("total", total);
			out.put("exposures", grouped);
			return ExecuteResult.success("查询成功", out);
		} catch (Exception e) {
			logger.error("queryBaseExposures error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> saveAffectedArea(String eventId, Map<String, Object> body,
			String operator) {
		try {
			BizDisasterEventModel event = requireEvent(eventId);
			BizSurveyBaselineModel baseline = requireEditableBaseline(eventId, event, operator);
			String geojson = first(str(body.get("affectedAreaGeojson")), str(body.get("geojson")),
					body.get("geometry") != null ? JSON.toJSONString(body.get("geometry")) : "");
			if (StringUtil.isEmpty(geojson)) {
				return ExecuteResult.error("affectedAreaGeojson / geojson 必填", null);
			}
			String rsImageId = first(str(body.get("rsImageId")), str(body.get("sceneId")), baseline.getRsImageId());
			baseline.setRsImageId(rsImageId);
			baseline.setAffectedAreaGeojson(geojson);
			baseline.setSceneId(first(str(body.get("sceneId")), baseline.getSceneId(), rsImageId));
			baseline.setDistrict(first(str(body.get("districtName")), str(body.get("regionName")), event.getRegionName(),
					baseline.getDistrict()));
			baseline.setStatus(BaselineStatusEnum.DRAFT.getCode());
			touch(baseline, operator);
			persistBaseline(baseline);

			// 同步识别 meta，便于后续叠置
			StepComputeRequestDto dto = buildComputeDto(event, body);
			dto.setPersist(false);
			ExecuteResult<Map<String, Object>> identify = surveyOverlayComputeService.computeIdentify(dto);
			Map<String, Object> out = toBaselineMap(baseline);
			out.put("identify", identify != null ? identify.getData() : null);
			return ExecuteResult.success("受灾范围已保存", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("saveAffectedArea error", e);
			return ExecuteResult.error("保存失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> runOverlayAnalysis(String eventId, Map<String, Object> body,
			String operator) {
		try {
			BizDisasterEventModel event = requireEvent(eventId);
			BizSurveyBaselineModel baseline = requireEditableBaseline(eventId, event, operator);
			if (StringUtil.isEmpty(baseline.getAffectedAreaGeojson())
					&& (body == null || StringUtil.isEmpty(str(body.get("affectedAreaGeojson"))))) {
				return ExecuteResult.error("请先完成 Step2 受灾范围识别", null);
			}
			if (body != null && StringUtil.isNotEmpty(str(body.get("affectedAreaGeojson")))) {
				baseline.setAffectedAreaGeojson(str(body.get("affectedAreaGeojson")));
			}

			StepComputeRequestDto dto = buildComputeDto(event, body);
			dto.setDistrictName(first(dto.getDistrictName(), baseline.getDistrict(), event.getRegionName()));
			dto.setSceneId(first(dto.getSceneId(), baseline.getSceneId(), baseline.getRsImageId()));
			dto.setPersist(false);
			ExecuteResult<Map<String, Object>> overlayRes = surveyOverlayComputeService.computeFullOverlay(dto);
			if (overlayRes == null || !"S".equals(overlayRes.getResultCode()) || overlayRes.getData() == null) {
				return ExecuteResult.error(
						overlayRes != null ? overlayRes.getResultMsg() : "叠置计算失败", null);
			}
			Map<String, Object> overlay = overlayRes.getData();
			baseline.setAffectedPopulation(BigDecimal.valueOf(num(overlay.get("affectedPopulation"), 0)));
			baseline.setTownAreaStatsJson(JSON.toJSONString(overlay.get("townAreaStats")));
			baseline.setExposureOverlayJson(JSON.toJSONString(overlay.get("exposureOverlay")));
			baseline.setStatus(BaselineStatusEnum.DRAFT.getCode());
			touch(baseline, operator);
			persistBaseline(baseline);

			Map<String, Object> out = toBaselineMap(baseline);
			out.put("overlay", overlay);
			return ExecuteResult.success("叠置分析完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("runOverlayAnalysis error", e);
			return ExecuteResult.error("叠置分析失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> exportExposureImages(String eventId, Map<String, Object> body,
			String operator) {
		try {
			BizDisasterEventModel event = requireEvent(eventId);
			BizSurveyBaselineModel baseline = requireEditableBaseline(eventId, event, operator);
			Map<String, Object> exposureOverlay = parseJsonMap(baseline.getExposureOverlayJson());
			if (exposureOverlay.isEmpty() && body != null && body.get("exposureOverlay") != null) {
				exposureOverlay = asMap(body.get("exposureOverlay"));
			}
			String rsImageId = first(str(body != null ? body.get("rsImageId") : null), baseline.getRsImageId(),
					baseline.getSceneId());
			List<Map<String, Object>> exports = new ArrayList<>();
			for (ExposureTypeEnum type : ExposureTypeEnum.values()) {
				Object packObj = exposureOverlay.get(type.getCode());
				List<Map<String, Object>> rows = extractRows(packObj);
				for (Map<String, Object> row : rows) {
					String exposureId = first(str(row.get("id")), str(row.get("exposureId")));
					String name = first(str(row.get("name")), "unknown");
					Map<String, Object> item = new HashMap<>();
					item.put("exposureType", type.getCode());
					item.put("exposureId", exposureId);
					item.put("exposureName", name);
					item.put("rsImageId", rsImageId);
					item.put("clipImageUrl",
							"/survey/clips/" + eventId + "/" + type.getCode() + "/"
									+ (StringUtil.isNotEmpty(exposureId) ? exposureId : safeSeg(name)) + ".tif");
					item.put("status", "READY");
					exports.add(item);
				}
			}
			Map<String, Object> exportPack = new HashMap<>();
			exportPack.put("eventId", eventId);
			exportPack.put("baselineId", baseline.getId());
			exportPack.put("rsImageId", rsImageId);
			exportPack.put("items", exports);
			exportPack.put("exportedAt", TS.format(new Date()));

			Map<String, Object> overlay = parseJsonMap(baseline.getExposureOverlayJson());
			overlay.put("_imageExports", exportPack);
			baseline.setExposureOverlayJson(JSON.toJSONString(overlay));
			touch(baseline, operator);
			persistBaseline(baseline);

			Map<String, Object> out = toBaselineMap(baseline);
			out.put("exports", exportPack);
			return ExecuteResult.success("影像导出清单已生成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("exportExposureImages error", e);
			return ExecuteResult.error("影像导出失败:" + e.getMessage(), null);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ExecuteResult<Map<String, Object>> compareQuickReport(String eventId, Map<String, Object> body,
			String operator) {
		try {
			BizDisasterEventModel event = requireEvent(eventId);
			BizSurveyBaselineModel baseline = requireEditableBaseline(eventId, event, operator);

			List<Map<String, Object>> quickItems = new ArrayList<>();
			if (body != null) {
				Object qr = body.get("quickReport");
				if (qr instanceof List) {
					quickItems = (List<Map<String, Object>>) qr;
				} else if (body.get("items") instanceof List) {
					quickItems = (List<Map<String, Object>>) body.get("items");
				}
			}

			List<Map<String, Object>> compareRows = new ArrayList<>();
			double rsPop = baseline.getAffectedPopulation() == null ? 0
					: baseline.getAffectedPopulation().doubleValue();
			double qrPop = 0;
			for (Map<String, Object> item : quickItems) {
				String metric = first(str(item.get("metric")), str(item.get("name")), "指标");
				double rsValue = num(item.get("rsValue"), "受灾人口".equals(metric) || "population".equalsIgnoreCase(metric)
						? rsPop
						: num(item.get("remoteValue"), 0));
				double qrValue = num(item.get("quickValue"), num(item.get("reportValue"), 0));
				if ("受灾人口".equals(metric) || "population".equalsIgnoreCase(metric)) {
					qrPop = qrValue;
					if (item.get("rsValue") == null && item.get("remoteValue") == null) {
						rsValue = rsPop;
					}
				}
				double diff = round2(rsValue - qrValue);
				double diffPct = qrValue == 0 ? (rsValue == 0 ? 0 : 100) : round2(diff / qrValue * 100);
				Map<String, Object> row = new HashMap<>();
				row.put("metric", metric);
				row.put("unit", str(item.get("unit")));
				row.put("rsValue", rsValue);
				row.put("quickValue", qrValue);
				row.put("diff", diff);
				row.put("diffPct", diffPct);
				row.put("confirmedValue",
						num(item.get("confirmedValue"), Math.abs(diffPct) <= 15 ? qrValue : rsValue));
				compareRows.add(row);
			}
			if (compareRows.isEmpty()) {
				Map<String, Object> popRow = new HashMap<>();
				popRow.put("metric", "受灾人口");
				popRow.put("unit", "人");
				popRow.put("rsValue", rsPop);
				popRow.put("quickValue", qrPop);
				popRow.put("diff", round2(rsPop - qrPop));
				popRow.put("diffPct", 0);
				popRow.put("confirmedValue", rsPop);
				compareRows.add(popRow);
			}

			Map<String, Object> chart = new HashMap<>();
			List<String> categories = new ArrayList<>();
			List<Double> rsSeries = new ArrayList<>();
			List<Double> qrSeries = new ArrayList<>();
			for (Map<String, Object> row : compareRows) {
				categories.add(str(row.get("metric")));
				rsSeries.add(num(row.get("rsValue"), 0));
				qrSeries.add(num(row.get("quickValue"), 0));
			}
			chart.put("categories", categories);
			chart.put("rsSeries", rsSeries);
			chart.put("quickSeries", qrSeries);

			Map<String, Object> comparePack = new HashMap<>();
			comparePack.put("eventId", eventId);
			comparePack.put("baselineId", baseline.getId());
			comparePack.put("rows", compareRows);
			comparePack.put("chart", chart);
			comparePack.put("comparedAt", TS.format(new Date()));

			baseline.setCompareCheckJson(JSON.toJSONString(comparePack));
			touch(baseline, operator);
			persistBaseline(baseline);

			Map<String, Object> out = toBaselineMap(baseline);
			out.put("compare", comparePack);
			return ExecuteResult.success("对比核查完成", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("compareQuickReport error", e);
			return ExecuteResult.error("对比核查失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> confirmBaseline(String eventId, Map<String, Object> body,
			String operator) {
		try {
			BizDisasterEventModel event = requireEvent(eventId);
			BizSurveyBaselineModel baseline = surveyBaselineDao.getLatestByEventId(eventId);
			if (baseline == null) {
				return ExecuteResult.error("该事件尚无调查基线，请先完成调查步骤", null);
			}
			String status = nvl(baseline.getStatus());
			if (BaselineStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(status)) {
				return ExecuteResult.error("基线已确认锁定，不可重复确认", toBaselineMap(baseline));
			}
			if (BaselineStatusEnum.ARCHIVED.getCode().equalsIgnoreCase(status)) {
				return ExecuteResult.error("基线已归档，无法确认", toBaselineMap(baseline));
			}
			if (StringUtil.isEmpty(baseline.getAffectedAreaGeojson())) {
				return ExecuteResult.error("缺少受灾范围，无法确认基线", null);
			}
			if (baseline.getCompareCheckJson() == null || baseline.getCompareCheckJson().trim().isEmpty()) {
				return ExecuteResult.error("请先完成速报对比核查再确认基线", null);
			}

			Date now = new Date();
			baseline.setStatus(BaselineStatusEnum.CONFIRMED.getCode());
			baseline.setConfirmedBy(first(operator, "system"));
			baseline.setConfirmedTime(now);
			if (body != null && StringUtil.isNotEmpty(str(body.get("remark")))) {
				baseline.setRemark(str(body.get("remark")));
			}
			touch(baseline, operator);
			persistBaseline(baseline);

			Map<String, Object> out = toBaselineMap(baseline);
			out.put("event", briefEvent(event));
			return ExecuteResult.success("调查基线已确认锁定", out);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("confirmBaseline error", e);
			return ExecuteResult.error("确认失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getBaseline(String eventId) {
		try {
			requireEvent(eventId);
			BizSurveyBaselineModel baseline = surveyBaselineDao.getLatestByEventId(eventId);
			if (baseline == null) {
				return ExecuteResult.success("无数据", null);
			}
			return ExecuteResult.success("查询成功", toBaselineMap(baseline));
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("getBaseline error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> listDossiers(String eventId, String exposureType) {
		try {
			requireEvent(eventId);
			List<BizHazardDossierModel> rows;
			if (StringUtil.isNotEmpty(exposureType)) {
				ExposureTypeEnum type = ExposureTypeEnum.fromAny(exposureType);
				if (type == null) {
					return ExecuteResult.error("不能识别的承灾体类型", new ArrayList<>());
				}
				rows = hazardDossierDao.listByEventIdAndExposureType(eventId, type.getCode());
			} else {
				rows = hazardDossierDao.listByEventId(eventId);
			}
			List<Map<String, Object>> list = new ArrayList<>();
			if (rows != null) {
				for (BizHazardDossierModel row : rows) {
					list.add(toDossierMap(row));
				}
			}
			return ExecuteResult.success("查询成功", list);
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), new ArrayList<>());
		} catch (Exception e) {
			logger.error("listDossiers error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> saveDossier(String eventId, Map<String, Object> dossier,
			String operator) {
		try {
			requireEvent(eventId);
			if (dossier == null) {
				return ExecuteResult.error("档案内容为空", null);
			}
			BizSurveyBaselineModel baseline = surveyBaselineDao.getLatestByEventId(eventId);
			String exposureTypeRaw = first(str(dossier.get("exposureType")), str(dossier.get("hazardType")));
			ExposureTypeEnum exposureType = ExposureTypeEnum.fromAny(exposureTypeRaw);
			String hazardName = first(str(dossier.get("hazardName")), str(dossier.get("exposureName")));
			if (exposureType == null || StringUtil.isEmpty(hazardName)) {
				return ExecuteResult.error("exposureType 与 hazardName/exposureName 必填", null);
			}
			String exposureId = first(str(dossier.get("exposureId")), str(dossier.get("id")));
			String bizKey = eventId + "::" + exposureType.getCode() + "::"
					+ first(exposureId, hazardName);

			BizHazardDossierModel exist = hazardDossierDao.getByBizKey(bizKey);
			Date now = new Date();
			String id = first(str(dossier.get("id")), exist != null ? exist.getId() : null,
					"ds_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));

			dossier.put("id", id);
			dossier.put("eventId", eventId);
			dossier.put("baselineId", baseline != null ? baseline.getId() : str(dossier.get("baselineId")));
			dossier.put("exposureType", exposureType.getCode());
			dossier.put("exposureId", exposureId);
			dossier.put("hazardType", exposureType.getLegacyAlias());
			dossier.put("hazardName", hazardName);
			dossier.put("updatedAt", TS.format(now));

			BizHazardDossierModel model = exist != null ? exist : new BizHazardDossierModel();
			model.setId(id);
			model.setEventId(eventId);
			model.setBaselineId(baseline != null ? baseline.getId() : null);
			model.setExposureType(exposureType.getCode());
			model.setExposureId(exposureId);
			model.setRsClipImageUrl(str(dossier.get("rsClipImageUrl")));
			Object attachments = dossier.get("fieldAttachments");
			if (attachments == null) {
				attachments = dossier.get("attachments");
			}
			model.setFieldAttachmentsJson(attachments == null ? null : JSON.toJSONString(attachments));
			model.setBizKey(bizKey);
			model.setHazardType(exposureType.getLegacyAlias());
			model.setHazardName(hazardName);
			model.setVerifyStatus(str(dossier.get("verifyStatus")));
			model.setArchiveStatus(str(dossier.get("archiveStatus")));
			model.setArchiveNo(str(dossier.get("archiveNo")));
			model.setJsonData(JSON.toJSONString(dossier));
			model.setRemark(str(dossier.get("remark")));
			model.setZhgxr(operator);
			model.setZhgxsj(now);
			if (exist == null) {
				model.setCjr(operator);
				model.setCjsj(now);
				hazardDossierDao.insert(model);
			} else {
				hazardDossierDao.update(model);
			}
			return ExecuteResult.success("保存成功", toDossierMap(model));
		} catch (IllegalStateException ex) {
			return ExecuteResult.error(ex.getMessage(), null);
		} catch (Exception e) {
			logger.error("saveDossier error", e);
			return ExecuteResult.error("保存失败:" + e.getMessage(), null);
		}
	}

	private BizDisasterEventModel requireEvent(String eventId) throws Exception {
		if (StringUtil.isEmpty(eventId)) {
			throw new IllegalStateException("eventId 必填");
		}
		BizDisasterEventModel event = disasterEventDao.getById(eventId);
		if (event == null) {
			throw new IllegalStateException("灾害事件不存在: " + eventId);
		}
		return event;
	}

	private BizSurveyBaselineModel requireEditableBaseline(String eventId, BizDisasterEventModel event,
			String operator) throws Exception {
		BizSurveyBaselineModel baseline = surveyBaselineDao.getLatestByEventId(eventId);
		Date now = new Date();
		if (baseline != null) {
			String status = nvl(baseline.getStatus());
			if (BaselineStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(status)
					|| BaselineStatusEnum.ARCHIVED.getCode().equalsIgnoreCase(status)) {
				// 严禁覆盖已锁定基线：自动新建 DRAFT 且 version_no+1
				int nextVer = baseline.getVersionNo() == null ? 2 : baseline.getVersionNo() + 1;
				BizSurveyBaselineModel draft = new BizSurveyBaselineModel();
				draft.setId("sb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
				draft.setEventId(eventId);
				draft.setVersionNo(nextVer);
				draft.setStatus(BaselineStatusEnum.DRAFT.getCode());
				draft.setDistrict(first(baseline.getDistrict(), event.getRegionName()));
				draft.setSceneId(first(baseline.getSceneId(), eventId));
				draft.setRsImageId(baseline.getRsImageId());
				draft.setCjr(first(operator, "system"));
				draft.setCjsj(now);
				draft.setZhgxr(first(operator, "system"));
				draft.setZhgxsj(now);
				surveyBaselineDao.insert(draft);
				logger.info("基线已锁定({}), 自动新建 DRAFT 版本 versionNo={}", status, nextVer);
				return draft;
			}
			return baseline;
		}
		baseline = new BizSurveyBaselineModel();
		baseline.setId("sb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
		baseline.setEventId(eventId);
		baseline.setVersionNo(1);
		baseline.setStatus(BaselineStatusEnum.DRAFT.getCode());
		baseline.setDistrict(event.getRegionName());
		baseline.setSceneId(eventId);
		baseline.setCjr(first(operator, "system"));
		baseline.setCjsj(now);
		baseline.setZhgxr(first(operator, "system"));
		baseline.setZhgxsj(now);
		surveyBaselineDao.insert(baseline);
		return baseline;
	}

	private void persistBaseline(BizSurveyBaselineModel baseline) throws Exception {
		surveyBaselineDao.update(baseline);
	}

	private void touch(BizSurveyBaselineModel baseline, String operator) {
		baseline.setZhgxr(first(operator, "system"));
		baseline.setZhgxsj(new Date());
		if (baseline.getVersionNo() == null) {
			baseline.setVersionNo(1);
		}
		if (StringUtil.isEmpty(baseline.getStatus())) {
			baseline.setStatus(BaselineStatusEnum.DRAFT.getCode());
		}
	}

	private StepComputeRequestDto buildComputeDto(BizDisasterEventModel event, Map<String, Object> body) {
		StepComputeRequestDto dto = new StepComputeRequestDto();
		if (body != null) {
			dto.setSceneId(str(body.get("sceneId")));
			dto.setSceneName(str(body.get("sceneName")));
			dto.setSceneDate(str(body.get("sceneDate")));
			dto.setRegionHint(str(body.get("regionHint")));
			dto.setDistrictName(first(str(body.get("districtName")), str(body.get("regionName"))));
			dto.setCity(str(body.get("city")));
			if (body.get("inundationRatio") != null) {
				dto.setInundationRatio(num(body.get("inundationRatio"), 0.45));
			}
			if (body.get("statArea") != null) {
				dto.setStatArea(num(body.get("statArea"), 0));
			}
			if (body.get("density") != null) {
				dto.setDensity(num(body.get("density"), 0));
			}
			dto.setHazardName(str(body.get("hazardName")));
			dto.setCjr(str(body.get("cjr")));
		}
		if (StringUtil.isEmpty(dto.getDistrictName())) {
			dto.setDistrictName(event.getRegionName());
		}
		return dto;
	}

	private Map<String, Object> toBaselineMap(BizSurveyBaselineModel row) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", row.getId());
		map.put("baselineId", row.getId());
		map.put("eventId", row.getEventId());
		map.put("versionNo", row.getVersionNo());
		map.put("status", row.getStatus());
		map.put("rsImageId", row.getRsImageId());
		map.put("affectedAreaGeojson", row.getAffectedAreaGeojson());
		map.put("affectedPopulation", row.getAffectedPopulation());
		map.put("townAreaStats", parseJson(row.getTownAreaStatsJson()));
		map.put("exposureOverlay", parseJson(row.getExposureOverlayJson()));
		map.put("compareCheck", parseJson(row.getCompareCheckJson()));
		map.put("confirmedTime", row.getConfirmedTime() == null ? null : TS.format(row.getConfirmedTime()));
		map.put("confirmedBy", row.getConfirmedBy());
		map.put("sceneId", row.getSceneId());
		map.put("district", row.getDistrict());
		map.put("remark", row.getRemark());
		map.put("createTime", row.getCjsj() == null ? null : TS.format(row.getCjsj()));
		map.put("updateTime", row.getZhgxsj() == null ? null : TS.format(row.getZhgxsj()));
		return map;
	}

	private Map<String, Object> toDossierMap(BizHazardDossierModel row) {
		Map<String, Object> map = new HashMap<>();
		if (row == null) {
			return map;
		}
		Object parsed = parseJson(row.getJsonData());
		if (parsed instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> json = (Map<String, Object>) parsed;
			map.putAll(json);
		}
		map.put("id", row.getId());
		map.put("eventId", row.getEventId());
		map.put("baselineId", row.getBaselineId());
		map.put("exposureType", row.getExposureType());
		map.put("exposureId", row.getExposureId());
		map.put("rsClipImageUrl", row.getRsClipImageUrl());
		map.put("fieldAttachments", parseJson(row.getFieldAttachmentsJson()));
		map.put("hazardType", row.getHazardType());
		map.put("hazardName", row.getHazardName());
		map.put("verifyStatus", row.getVerifyStatus());
		map.put("archiveStatus", row.getArchiveStatus());
		map.put("archiveNo", row.getArchiveNo());
		return map;
	}

	private Map<String, Object> briefEvent(BizDisasterEventModel event) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", event.getId());
		map.put("eventCode", event.getEventCode());
		map.put("eventName", event.getEventName());
		map.put("regionCode", event.getRegionCode());
		map.put("regionName", event.getRegionName());
		map.put("status", event.getStatus());
		return map;
	}

	private List<Map<String, Object>> filterByRegion(List<Map<String, Object>> list, String regionCode,
			String regionName) {
		if (StringUtil.isEmpty(regionCode) && StringUtil.isEmpty(regionName)) {
			return list;
		}
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> item : list) {
			String division = first(str(item.get("division")), str(item.get("regionName")), str(item.get("address")));
			String code = first(str(item.get("regionCode")), str(item.get("code")));
			boolean hit = true;
			if (StringUtil.isNotEmpty(regionCode) && StringUtil.isNotEmpty(code)) {
				hit = code.startsWith(regionCode) || regionCode.startsWith(code);
			}
			if (hit && StringUtil.isNotEmpty(regionName) && StringUtil.isNotEmpty(division)) {
				hit = division.contains(regionName) || regionName.contains(division);
			}
			if (hit) {
				out.add(item);
			}
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> extractRows(Object packObj) {
		if (!(packObj instanceof Map)) {
			return new ArrayList<>();
		}
		Object rows = ((Map<String, Object>) packObj).get("rows");
		if (!(rows instanceof List)) {
			return new ArrayList<>();
		}
		return (List<Map<String, Object>>) rows;
	}

	private List<Map<String, Object>> listBiz(String dataType) {
		ExecuteResult<List<Map<String, Object>>> res = bizFrontendDataService.list(dataType, null, null);
		return res != null && res.getData() != null ? res.getData() : new ArrayList<>();
	}

	private Map<String, Object> queryHint(String regionCode, String regionName, Double lon, Double lat) {
		Map<String, Object> q = new HashMap<>();
		q.put("regionCode", nvl(regionCode));
		q.put("regionName", nvl(regionName));
		q.put("lon", lon);
		q.put("lat", lat);
		return q;
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseJsonMap(String json) {
		Object obj = parseJson(json);
		if (obj instanceof Map) {
			return new HashMap<>((Map<String, Object>) obj);
		}
		return new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object obj) {
		if (obj instanceof Map) {
			return new HashMap<>((Map<String, Object>) obj);
		}
		if (obj != null) {
			try {
				Object parsed = JSON.parse(JSON.toJSONString(obj));
				if (parsed instanceof Map) {
					return (Map<String, Object>) parsed;
				}
			} catch (Exception ignored) {
				// ignore
			}
		}
		return new HashMap<>();
	}

	private static String safeSeg(String text) {
		return str(text).replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
	}

	private static double round2(double v) {
		return Math.round(v * 100d) / 100d;
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
