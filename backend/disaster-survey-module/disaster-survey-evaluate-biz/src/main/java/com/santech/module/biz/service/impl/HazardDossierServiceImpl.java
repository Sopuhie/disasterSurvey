package com.santech.module.biz.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.HazardDossierDao;
import com.santech.core.framework.entity.model.BizHazardDossierModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.FileUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.HazardDossierService;

@Service
public class HazardDossierServiceImpl implements HazardDossierService {

	private static final Logger logger = LoggerFactory.getLogger(HazardDossierServiceImpl.class);
	private static final String DOSSIER_DIR = "dossier";
	private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Resource
	private HazardDossierDao hazardDossierDao;

	@Resource
	private FileBasicConfig fileBasicConfig;

	@Override
	public ExecuteResult<Map<String, Object>> get(String hazardType, String hazardName) {
		try {
			if (StringUtil.isEmpty(hazardType) || StringUtil.isEmpty(hazardName)) {
				return ExecuteResult.error("hazardType 与 hazardName 必填", null);
			}
			BizHazardDossierModel row = hazardDossierDao.getByBizKey(bizKey(hazardType, hazardName));
			if (row == null) {
				return ExecuteResult.success("无数据", null);
			}
			return ExecuteResult.success("查询成功", parsePayload(row));
		} catch (Exception e) {
			logger.error("get hazard dossier error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> save(Map<String, Object> dossier, String operator) {
		try {
			if (dossier == null) {
				return ExecuteResult.error("档案内容为空", null);
			}
			String hazardType = str(dossier.get("hazardType"));
			String hazardName = str(dossier.get("hazardName"));
			if (StringUtil.isEmpty(hazardType) || StringUtil.isEmpty(hazardName)) {
				return ExecuteResult.error("hazardType 与 hazardName 必填", null);
			}
			persistDossier(dossier, operator);
			return ExecuteResult.success("保存成功", dossier);
		} catch (Exception e) {
			logger.error("save hazard dossier error", e);
			return ExecuteResult.error("保存失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> listArchived(String keyword) {
		try {
			List<BizHazardDossierModel> rows = hazardDossierDao.listArchived(keyword);
			List<Map<String, Object>> list = new ArrayList<>();
			if (rows != null) {
				for (BizHazardDossierModel row : rows) {
					Map<String, Object> payload = parsePayload(row);
					if (payload != null) {
						list.add(payload);
					}
				}
			}
			return ExecuteResult.success("查询成功", list);
		} catch (Exception e) {
			logger.error("list archived dossier error", e);
			return ExecuteResult.error("查询失败:" + e.getMessage(), new ArrayList<>());
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> upload(MultipartFile file, String kind, String hazardType,
			String hazardName, Map<String, String> meta, String operator) {
		try {
			if (file == null || file.isEmpty()) {
				return ExecuteResult.error("请选择上传文件", null);
			}
			if (StringUtil.isEmpty(hazardType) || StringUtil.isEmpty(hazardName)) {
				return ExecuteResult.error("hazardType 与 hazardName 必填", null);
			}
			String itemKind = normalizeKind(kind);
			if (itemKind == null) {
				return ExecuteResult.error("kind 仅支持 material 或 attachment", null);
			}

			String originalName = file.getOriginalFilename();
			if (StringUtil.isEmpty(originalName)) {
				return ExecuteResult.error("文件名无效", null);
			}
			String suffix = "";
			int dot = originalName.lastIndexOf('.');
			if (dot >= 0 && dot < originalName.length() - 1) {
				suffix = originalName.substring(dot + 1);
			}
			String storedName = System.currentTimeMillis() + "_"
					+ UUID.randomUUID().toString().replace("-", "").substring(0, 8)
					+ (StringUtil.isEmpty(suffix) ? "" : "." + suffix);
			String relativeDir = DOSSIER_DIR + File.separator + safeSegment(hazardType) + File.separator
					+ safeSegment(hazardName) + File.separator + itemKind;
			String relativePath = relativeDir + File.separator + storedName;
			String absolutePath = getFileRootPath() + relativePath;
			File target = new File(absolutePath);
			File parent = target.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				return ExecuteResult.error("创建存储目录失败", null);
			}
			FileUtil.writeFromStream(file.getInputStream(), absolutePath);

			Map<String, Object> dossier = loadOrCreateDossier(hazardType, hazardName, operator);
			Map<String, Object> item = buildFileItem(itemKind, originalName, suffix, file.getSize(), relativePath,
					meta, operator);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) dossier
					.get(itemKind.equals("material") ? "materials" : "attachments");
			if (list == null) {
				list = new ArrayList<>();
				dossier.put(itemKind.equals("material") ? "materials" : "attachments", list);
			}
			list.add(0, item);
			persistDossier(dossier, operator);

			Map<String, Object> result = new HashMap<>();
			result.put("item", item);
			result.put("dossier", dossier);
			return ExecuteResult.success("上传成功", result);
		} catch (Exception e) {
			logger.error("upload hazard dossier file error", e);
			return ExecuteResult.error("上传失败:" + e.getMessage(), null);
		}
	}

	@Override
	public void download(String filePath, HttpServletResponse response) {
		try {
			String relative = normalizeRelativePath(filePath);
			if (relative == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("非法文件路径");
				return;
			}
			File file = new File(getFileRootPath() + relative);
			if (!file.exists() || !file.isFile()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().write("文件不存在");
				return;
			}
			String fileName = file.getName();
			response.reset();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition",
					"attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
			response.setContentLengthLong(file.length());
			try (FileInputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
				byte[] buf = new byte[8192];
				int len;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
				out.flush();
			}
		} catch (Exception e) {
			logger.error("download hazard dossier file error, path={}", filePath, e);
			try {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("下载失败");
			} catch (Exception ignored) {
				/* ignore */
			}
		}
	}

	@Override
	public ExecuteResult<Map<String, Object>> removeItem(String hazardType, String hazardName, String kind,
			String itemId, String operator) {
		try {
			if (StringUtil.isEmpty(hazardType) || StringUtil.isEmpty(hazardName) || StringUtil.isEmpty(itemId)) {
				return ExecuteResult.error("参数不完整", null);
			}
			String itemKind = normalizeKind(kind);
			if (itemKind == null) {
				return ExecuteResult.error("kind 仅支持 material 或 attachment", null);
			}
			Map<String, Object> dossier = loadOrCreateDossier(hazardType, hazardName, operator);
			String listKey = itemKind.equals("material") ? "materials" : "attachments";
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) dossier.get(listKey);
			if (list == null || list.isEmpty()) {
				return ExecuteResult.error("条目不存在", null);
			}
			Map<String, Object> removed = null;
			List<Map<String, Object>> next = new ArrayList<>();
			for (Map<String, Object> row : list) {
				if (itemId.equals(str(row.get("id")))) {
					removed = row;
				} else {
					next.add(row);
				}
			}
			if (removed == null) {
				return ExecuteResult.error("条目不存在", null);
			}
			dossier.put(listKey, next);
			persistDossier(dossier, operator);

			String relative = normalizeRelativePath(str(removed.get("filePath")));
			if (relative != null) {
				File disk = new File(getFileRootPath() + relative);
				if (disk.exists() && disk.isFile() && !disk.delete()) {
					logger.warn("delete dossier file failed: {}", disk.getAbsolutePath());
				}
			}
			Map<String, Object> result = new HashMap<>();
			result.put("removedId", itemId);
			result.put("dossier", dossier);
			return ExecuteResult.success("删除成功", result);
		} catch (Exception e) {
			logger.error("remove hazard dossier item error", e);
			return ExecuteResult.error("删除失败:" + e.getMessage(), null);
		}
	}

	private Map<String, Object> buildFileItem(String kind, String originalName, String suffix, long size,
			String relativePath, Map<String, String> meta, String operator) {
		Map<String, String> m = meta == null ? new HashMap<>() : meta;
		String now = TS.format(new Date());
		Map<String, Object> item = new HashMap<>();
		item.put("id", (kind.equals("material") ? "mat_" : "att_")
				+ UUID.randomUUID().toString().replace("-", "").substring(0, 12));
		item.put("fileName", originalName);
		item.put("filePath", relativePath.replace("\\", "/"));
		item.put("fileType", suffix);
		item.put("fileSize", size);
		item.put("size", formatSize(size));
		item.put("verified", false);
		if (kind.equals("material")) {
			String title = firstNonEmpty(m.get("title"), stripExt(originalName));
			item.put("title", title);
			item.put("category", firstNonEmpty(m.get("category"), "现场踏勘"));
			item.put("summary", str(m.get("summary")));
			item.put("location", str(m.get("location")));
			item.put("collector", firstNonEmpty(m.get("collector"), operator));
			item.put("collectTime", now);
		} else {
			item.put("name", originalName);
			item.put("type", guessAttachType(suffix));
			item.put("uploader", firstNonEmpty(m.get("uploader"), operator));
			item.put("uploadTime", now);
			item.put("remark", str(m.get("remark")));
		}
		return item;
	}

	private Map<String, Object> loadOrCreateDossier(String hazardType, String hazardName, String operator)
			throws Exception {
		BizHazardDossierModel row = hazardDossierDao.getByBizKey(bizKey(hazardType, hazardName));
		if (row != null) {
			Map<String, Object> payload = parsePayload(row);
			if (payload != null) {
				return payload;
			}
		}
		Map<String, Object> dossier = new HashMap<>();
		String now = TS.format(new Date());
		dossier.put("id", "ds_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
		dossier.put("hazardType", hazardType);
		dossier.put("hazardName", hazardName);
		dossier.put("profile", new HashMap<String, Object>());
		dossier.put("surveySummary", new HashMap<String, Object>());
		dossier.put("materials", new ArrayList<Map<String, Object>>());
		dossier.put("attachments", new ArrayList<Map<String, Object>>());
		dossier.put("verifyStatus", "待核对");
		dossier.put("verifyRemark", "");
		dossier.put("verifiedBy", "");
		dossier.put("verifiedAt", "");
		dossier.put("archiveStatus", "未归档");
		dossier.put("archiveNo", "");
		dossier.put("archivedBy", "");
		dossier.put("archivedAt", "");
		dossier.put("archiveRemark", "");
		dossier.put("createdAt", now);
		dossier.put("updatedAt", now);
		dossier.put("createdBy", operator);
		return dossier;
	}

	private void persistDossier(Map<String, Object> dossier, String operator) throws Exception {
		String hazardType = str(dossier.get("hazardType"));
		String hazardName = str(dossier.get("hazardName"));
		String key = bizKey(hazardType, hazardName);
		BizHazardDossierModel exist = hazardDossierDao.getByBizKey(key);
		Date now = new Date();
		String id = firstNonEmpty(str(dossier.get("id")), exist != null ? exist.getId() : null,
				"ds_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
		dossier.put("id", id);
		dossier.put("hazardType", hazardType);
		dossier.put("hazardName", hazardName);
		dossier.put("updatedAt", TS.format(now));

		BizHazardDossierModel model = exist != null ? exist : new BizHazardDossierModel();
		model.setId(id);
		model.setBizKey(key);
		model.setHazardType(hazardType);
		model.setHazardName(hazardName);
		model.setVerifyStatus(str(dossier.get("verifyStatus")));
		model.setArchiveStatus(str(dossier.get("archiveStatus")));
		model.setArchiveNo(str(dossier.get("archiveNo")));
		model.setJsonData(JSON.toJSONString(dossier));
		model.setRemark(str(dossier.get("archiveRemark")));
		model.setZhgxr(operator);
		model.setZhgxsj(now);
		if (exist == null) {
			model.setCjr(operator);
			model.setCjsj(now);
			if (StringUtil.isEmpty(str(dossier.get("createdAt")))) {
				dossier.put("createdAt", TS.format(now));
				model.setJsonData(JSON.toJSONString(dossier));
			}
			hazardDossierDao.insert(model);
		} else {
			hazardDossierDao.update(model);
		}
	}

	private Map<String, Object> parsePayload(BizHazardDossierModel row) {
		if (row == null) {
			return null;
		}
		try {
			Map<String, Object> map = JSON.parseObject(row.getJsonData(), new TypeReference<Map<String, Object>>() {
			});
			if (map == null) {
				map = new HashMap<>();
			}
			map.put("id", row.getId());
			map.put("hazardType", row.getHazardType());
			map.put("hazardName", row.getHazardName());
			if (StringUtil.isNotEmpty(row.getVerifyStatus())) {
				map.put("verifyStatus", row.getVerifyStatus());
			}
			if (StringUtil.isNotEmpty(row.getArchiveStatus())) {
				map.put("archiveStatus", row.getArchiveStatus());
			}
			if (StringUtil.isNotEmpty(row.getArchiveNo())) {
				map.put("archiveNo", row.getArchiveNo());
			}
			return map;
		} catch (Exception e) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", row.getId());
			map.put("hazardType", row.getHazardType());
			map.put("hazardName", row.getHazardName());
			map.put("_raw", row.getJsonData());
			return map;
		}
	}

	private String getFileRootPath() {
		String names = System.getProperty("os.name");
		String baseFilePath;
		if (names != null && names.toLowerCase().startsWith("win")) {
			baseFilePath = fileBasicConfig.getRootForWin();
		} else {
			baseFilePath = fileBasicConfig.getRootForLiunx();
		}
		if (baseFilePath == null) {
			baseFilePath = "";
		}
		if (!baseFilePath.endsWith("/") && !baseFilePath.endsWith("\\")) {
			baseFilePath = baseFilePath + File.separator;
		}
		return baseFilePath;
	}

	private String normalizeKind(String kind) {
		String k = str(kind).trim().toLowerCase();
		if ("material".equals(k) || "materials".equals(k) || "现场资料".equals(kind)) {
			return "material";
		}
		if ("attachment".equals(k) || "attachments".equals(k) || "附件".equals(kind)) {
			return "attachment";
		}
		return null;
	}

	/** 仅允许 dossier/ 下相对路径，防目录穿越 */
	private String normalizeRelativePath(String filePath) {
		if (StringUtil.isEmpty(filePath)) {
			return null;
		}
		String normalized = filePath.replace("\\", "/").replaceAll("^/+", "");
		if (!normalized.startsWith(DOSSIER_DIR + "/")) {
			return null;
		}
		if (normalized.contains("..")) {
			return null;
		}
		return normalized.replace("/", File.separator);
	}

	private String safeSegment(String text) {
		String raw = str(text).trim();
		if (raw.isEmpty()) {
			return "unknown";
		}
		return raw.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
	}

	private String formatSize(long size) {
		if (size < 1024) {
			return size + " B";
		}
		double kb = size / 1024.0;
		if (kb < 1024) {
			return new DecimalFormat("0.#").format(kb) + " KB";
		}
		return new DecimalFormat("0.##").format(kb / 1024.0) + " MB";
	}

	private String guessAttachType(String suffix) {
		String s = str(suffix).toLowerCase();
		if ("jpg,jpeg,png,gif,bmp,webp".contains(s) && !s.isEmpty()) {
			return "图片";
		}
		if ("zip,rar,7z".contains(s) && !s.isEmpty()) {
			return "压缩包";
		}
		if ("xls,xlsx,csv".contains(s) && !s.isEmpty()) {
			return "表格";
		}
		if ("doc,docx,pdf,txt".contains(s) && !s.isEmpty()) {
			return "文档";
		}
		return StringUtil.isEmpty(s) ? "文件" : s.toUpperCase();
	}

	private String stripExt(String name) {
		int idx = name.lastIndexOf('.');
		return idx > 0 ? name.substring(0, idx) : name;
	}

	private String bizKey(String hazardType, String hazardName) {
		return hazardType + "::" + hazardName;
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
