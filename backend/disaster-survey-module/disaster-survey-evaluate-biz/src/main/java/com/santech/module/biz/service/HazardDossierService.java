package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.santech.framework.global.ExecuteResult;

public interface HazardDossierService {

	ExecuteResult<Map<String, Object>> get(String hazardType, String hazardName);

	ExecuteResult<Map<String, Object>> save(Map<String, Object> dossier, String operator);

	ExecuteResult<List<Map<String, Object>>> listArchived(String keyword);

	/**
	 * 上传现场资料或附件：落盘并写入档案 JSON
	 * @param kind material | attachment
	 */
	ExecuteResult<Map<String, Object>> upload(MultipartFile file, String kind, String hazardType, String hazardName,
			Map<String, String> meta, String operator);

	/** 下载档案关联文件（相对路径须位于 dossier/ 下） */
	void download(String filePath, HttpServletResponse response);

	/** 删除资料/附件条目，并尝试删除磁盘文件 */
	ExecuteResult<Map<String, Object>> removeItem(String hazardType, String hazardName, String kind, String itemId,
			String operator);
}
