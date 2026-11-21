package com.santech.module.biz.service.impl;

import java.math.BigDecimal;
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
import com.santech.core.framework.dao.BizFrontendDataDao;
import com.santech.core.framework.entity.model.BizFrontendDataModel;
import com.santech.core.framework.entity.model.BizReportArchiveModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.BizFrontendDataQueryDto;
import com.santech.module.biz.entity.dto.BizFrontendDataSaveDto;
import com.santech.module.biz.service.BizFrontendDataService;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class BizFrontendDataServiceImpl implements BizFrontendDataService {

	private static final Logger logger = LoggerFactory.getLogger(BizFrontendDataServiceImpl.class);

	@Resource
	private BizFrontendDataDao bizFrontendDataDao;

	@Override
	public ExecuteResult<List<Map<String, Object>>> list(String dataType, String keyword, String extraType) {
		try {
			List<BizFrontendDataModel> rows = bizFrontendDataDao.listByType(dataType, keyword, extraType);
			return ExecuteResult.success("查询成功", toPayloadList(rows));
		} catch (Exception e) {
			logger.error("list biz data error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> queryWithPage(BizFrontendDataQueryDto query) {
		try {
			PageInfo pageInfo = query.getPageInfo() != null ? query.getPageInfo() : new PageInfo();
			List<BizFrontendDataModel> rows = bizFrontendDataDao.listByTypeWithPage(
					query.getDataType(), query.getKeyword(), query.getExtraType(), pageInfo);
			return ExecuteResult.success("查询成功", toPayloadList(rows), pageInfo);
		} catch (Exception e) {
			logger.error("queryWithPage biz data error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getById(String id) {
		try {
			BizFrontendDataModel row = bizFrontendDataDao.getById(id);
			if (row == null) {
				return ExecuteResult.error("记录不存在", null);
			}
			return ExecuteResult.success("查询成功", parsePayload(row));
		} catch (Exception e) {
			logger.error("getById biz data error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> save(BizFrontendDataSaveDto dto, String operator) {
		try {
			if (dto == null || StringUtil.isEmpty(dto.getDataType()) || dto.getData() == null) {
				return ExecuteResult.error("dataType 与 data 必填", null);
			}
			Map<String, Object> data = new HashMap<>(dto.getData());
			String bizId = str(data.get("id"));
			if (StringUtil.isEmpty(bizId)) {
				bizId = "bf_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
				data.put("id", bizId);
			}
			BizFrontendDataModel model = new BizFrontendDataModel();
			model.setId(bizId);
			model.setDataType(dto.getDataType());
			model.setBizKey(bizId);
			model.setName(firstNonEmpty(str(data.get("name")), str(data.get("hazardName")), str(data.get("title")), bizId));
			model.setExtraType(firstNonEmpty(str(data.get("type")), str(data.get("kind")), str(data.get("hazardType"))));
			model.setJsonData(JSON.toJSONString(data));
			model.setSortIndex(data.get("sortIndex") instanceof Number ? ((Number) data.get("sortIndex")).intValue() : 0);
			model.setRemark(str(data.get("remark")));
			model.setCjr(operator);
			model.setCjsj(new Date());
			model.setZhgxr(operator);
			model.setZhgxsj(new Date());
			bizFrontendDataDao.insert(model);
			return ExecuteResult.success("新增成功", data);
		} catch (Exception e) {
			logger.error("save biz data error", e);
			return ExecuteResult.error("新增失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> update(BizFrontendDataSaveDto dto, String operator) {
		try {
			if (dto == null || dto.getData() == null) {
				return ExecuteResult.error("data 必填", null);
			}
			Map<String, Object> data = new HashMap<>(dto.getData());
			String bizId = str(data.get("id"));
			if (StringUtil.isEmpty(bizId)) {
				return ExecuteResult.error("data.id 必填", null);
			}
			BizFrontendDataModel exist = bizFrontendDataDao.getById(bizId);
			if (exist == null) {
				return save(dto, operator);
			}
			String dataType = StringUtil.isNotEmpty(dto.getDataType()) ? dto.getDataType() : exist.getDataType();
			exist.setDataType(dataType);
			exist.setName(firstNonEmpty(str(data.get("name")), str(data.get("hazardName")), exist.getName()));
			exist.setExtraType(firstNonEmpty(str(data.get("type")), str(data.get("kind")), exist.getExtraType()));
			exist.setJsonData(JSON.toJSONString(data));
			exist.setRemark(str(data.get("remark")));
			exist.setZhgxr(operator);
			exist.setZhgxsj(new Date());
			bizFrontendDataDao.update(exist);
			return ExecuteResult.success("更新成功", data);
		} catch (Exception e) {
			logger.error("update biz data error", e);
			return ExecuteResult.error("更新失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> batchSave(String dataType, List<Map<String, Object>> list, String operator) {
		try {
			if (StringUtil.isEmpty(dataType) || list == null || list.isEmpty()) {
				return ExecuteResult.error("dataType 与 list 必填", null);
			}
			int ok = 0;
			int fail = 0;
			for (Map<String, Object> item : list) {
				if (item == null) {
					fail++;
					continue;
				}
				BizFrontendDataSaveDto dto = new BizFrontendDataSaveDto();
				dto.setDataType(dataType);
				dto.setData(item);
				ExecuteResult<Map<String, Object>> one = update(dto, operator);
				if (one != null && "S".equalsIgnoreCase(one.getResultCode())) {
					ok++;
				} else {
					fail++;
				}
			}
			Map<String, Object> summary = new HashMap<>();
			summary.put("dataType", dataType);
			summary.put("success", ok);
			summary.put("fail", fail);
			summary.put("total", list.size());
			return ExecuteResult.success("批量保存完成", summary);
		} catch (Exception e) {
			logger.error("batchSave biz data error", e);
			return ExecuteResult.error("批量保存失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<String> delete(String id) {
		try {
			bizFrontendDataDao.deleteById(id);
			return ExecuteResult.success("删除成功", id);
		} catch (Exception e) {
			logger.error("delete biz data error", e);
			return ExecuteResult.error("删除失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> archiveReport(Map<String, Object> payload, String operator) {
		try {
			if (payload == null) {
				return ExecuteResult.error("归档内容为空", null);
			}
			String id = firstNonEmpty(str(payload.get("id")), "ar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
			BizReportArchiveModel model = new BizReportArchiveModel();
			model.setId(id);
			model.setReportNo(str(payload.get("reportNo")));
			model.setTitle(str(payload.get("title")));
			model.setTypeId(str(payload.get("typeId")));
			model.setTypeLabel(str(payload.get("typeLabel")));
			model.setDistrict(str(payload.get("district")));
			model.setSceneName(str(payload.get("sceneName")));
			model.setGrade(str(payload.get("grade")));
			Object total = payload.get("total");
			if (total instanceof Number) {
				model.setTotalScore(BigDecimal.valueOf(((Number) total).doubleValue()));
			}
			model.setArchivedAt(firstNonEmpty(str(payload.get("archivedAt")), new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
			model.setArchivedBy(firstNonEmpty(str(payload.get("archivedBy")), operator));
			model.setRemark(str(payload.get("remark")));
			Object snapshot = payload.get("snapshot");
			model.setSnapshot(snapshot == null ? JSON.toJSONString(payload) : (snapshot instanceof String ? (String) snapshot : JSON.toJSONString(snapshot)));
			model.setCjr(operator);
			model.setCjsj(new Date());
			model.setZhgxr(operator);
			model.setZhgxsj(new Date());
			bizFrontendDataDao.insertArchive(model);

			Map<String, Object> out = new HashMap<>(payload);
			out.put("id", id);
			out.put("archivedAt", model.getArchivedAt());
			out.put("archivedBy", model.getArchivedBy());
			return ExecuteResult.success("归档成功", out);
		} catch (Exception e) {
			logger.error("archive report error", e);
			return ExecuteResult.error("归档失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> listArchives() {
		try {
			List<BizReportArchiveModel> rows = bizFrontendDataDao.listArchives();
			List<Map<String, Object>> list = new ArrayList<>();
			for (BizReportArchiveModel row : rows) {
				list.add(archiveToMap(row, false));
			}
			return ExecuteResult.success("查询成功", list);
		} catch (Exception e) {
			logger.error("list archives error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> getArchiveById(String id) {
		try {
			BizReportArchiveModel row = bizFrontendDataDao.getArchiveById(id);
			if (row == null) {
				return ExecuteResult.error("归档不存在", null);
			}
			return ExecuteResult.success("查询成功", archiveToMap(row, true));
		} catch (Exception e) {
			logger.error("get archive error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	private List<Map<String, Object>> toPayloadList(List<BizFrontendDataModel> rows) {
		List<Map<String, Object>> list = new ArrayList<>();
		if (rows == null) {
			return list;
		}
		for (BizFrontendDataModel row : rows) {
			Map<String, Object> payload = parsePayload(row);
			if (payload != null) {
				list.add(payload);
			}
		}
		return list;
	}

	private Map<String, Object> parsePayload(BizFrontendDataModel row) {
		if (row == null) {
			return null;
		}
		try {
			Map<String, Object> map = JSON.parseObject(row.getJsonData(), new TypeReference<Map<String, Object>>() {});
			if (map == null) {
				map = new HashMap<>();
			}
			if (!map.containsKey("id")) {
				map.put("id", row.getId());
			}
			map.put("_dataType", row.getDataType());
			return map;
		} catch (Exception e) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", row.getId());
			map.put("name", row.getName());
			map.put("_raw", row.getJsonData());
			return map;
		}
	}

	private Map<String, Object> archiveToMap(BizReportArchiveModel row, boolean withSnapshot) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", row.getId());
		map.put("reportNo", row.getReportNo());
		map.put("title", row.getTitle());
		map.put("typeId", row.getTypeId());
		map.put("typeLabel", row.getTypeLabel());
		map.put("district", row.getDistrict());
		map.put("sceneName", row.getSceneName());
		map.put("grade", row.getGrade());
		map.put("total", row.getTotalScore());
		map.put("archivedAt", row.getArchivedAt());
		map.put("archivedBy", row.getArchivedBy());
		map.put("remark", row.getRemark());
		if (withSnapshot && StringUtil.isNotEmpty(row.getSnapshot())) {
			try {
				map.put("snapshot", JSON.parse(row.getSnapshot()));
			} catch (Exception e) {
				map.put("snapshot", row.getSnapshot());
			}
		}
		return map;
	}

	private String str(Object v) {
		return v == null ? "" : String.valueOf(v);
	}

	private String firstNonEmpty(String... values) {
		if (values == null) {
			return "";
		}
		for (String v : values) {
			if (StringUtil.isNotEmpty(v)) {
				return v;
			}
		}
		return "";
	}
}
