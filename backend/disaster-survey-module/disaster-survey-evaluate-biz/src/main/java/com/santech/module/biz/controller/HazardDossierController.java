package com.santech.module.biz.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.service.HazardDossierService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 调查对象档案（现场资料 / 附件 / 核对 / 归档）
 * 无数据时前端回落 MOCK。
 */
@Api(tags = "Evaluate-调查对象档案接口")
@RestController
@RequestMapping("/web-disaster-evaluate/web/disaster/evaluate/dossier")
public class HazardDossierController {

	private static final Logger logger = LoggerFactory.getLogger(HazardDossierController.class);

	@Resource
	private HazardDossierService hazardDossierService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("HazardDossierController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@GetMapping("/get")
	@ApiOperation("按承灾体获取档案；无记录时 data=null，前端回落 MOCK")
	public ExecuteResult<Map<String, Object>> get(
			@RequestParam("hazardType") String hazardType,
			@RequestParam("hazardName") String hazardName) {
		return hazardDossierService.get(hazardType, hazardName);
	}

	@PostMapping("/save")
	@ApiOperation("保存/更新完整档案（含 materials、attachments、核对归档字段）")
	public ExecuteResult<Map<String, Object>> save(@RequestBody Map<String, Object> dossier, HttpServletRequest request) {
		return hazardDossierService.save(dossier, operator(request));
	}

	@GetMapping("/listArchived")
	@ApiOperation("已归档档案列表")
	public ExecuteResult<List<Map<String, Object>>> listArchived(
			@RequestParam(value = "keyword", required = false) String keyword) {
		return hazardDossierService.listArchived(keyword);
	}

	@PostMapping(value = "/upload", consumes = "multipart/form-data")
	@ApiOperation("上传现场资料或附件（kind=material|attachment），写入档案并落盘")
	public ExecuteResult<Map<String, Object>> upload(
			@RequestPart("file") MultipartFile file,
			@RequestParam("kind") String kind,
			@RequestParam("hazardType") String hazardType,
			@RequestParam("hazardName") String hazardName,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "summary", required = false) String summary,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "collector", required = false) String collector,
			@RequestParam(value = "uploader", required = false) String uploader,
			@RequestParam(value = "remark", required = false) String remark,
			HttpServletRequest request) {
		Map<String, String> meta = new HashMap<>();
		meta.put("title", title);
		meta.put("category", category);
		meta.put("summary", summary);
		meta.put("location", location);
		meta.put("collector", collector);
		meta.put("uploader", uploader);
		meta.put("remark", remark);
		return hazardDossierService.upload(file, kind, hazardType, hazardName, meta, operator(request));
	}

	@GetMapping("/download")
	@ApiOperation("下载档案关联文件")
	public void download(@RequestParam("filePath") String filePath, HttpServletResponse response) {
		hazardDossierService.download(filePath, response);
	}

	@PostMapping("/removeItem")
	@ApiOperation("删除现场资料或附件条目（同步删盘）")
	public ExecuteResult<Map<String, Object>> removeItem(@RequestBody Map<String, Object> body,
			HttpServletRequest request) {
		String hazardType = body == null ? null : String.valueOf(body.get("hazardType"));
		String hazardName = body == null ? null : String.valueOf(body.get("hazardName"));
		String kind = body == null ? null : String.valueOf(body.get("kind"));
		String itemId = body == null ? null : String.valueOf(body.get("itemId"));
		return hazardDossierService.removeItem(hazardType, hazardName, kind, itemId, operator(request));
	}

	private String operator(HttpServletRequest request) {
		Object name = request.getAttribute(Constants.HEADER_USERNAME);
		if (name != null) {
			return String.valueOf(name);
		}
		Object id = request.getAttribute(Constants.HEADER_USERID);
		return id == null ? "system" : String.valueOf(id);
	}
}
