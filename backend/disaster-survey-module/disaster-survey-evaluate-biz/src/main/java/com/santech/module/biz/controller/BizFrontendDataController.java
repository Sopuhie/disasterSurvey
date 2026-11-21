package com.santech.module.biz.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.BizFrontendDataQueryDto;
import com.santech.module.biz.entity.dto.BizFrontendDataSaveDto;
import com.santech.module.biz.service.BizFrontendDataService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 前端调查/评估 MOCK 台账对应的真实 CRUD 接口。
 * 表为空时前端自行回落 MOCK。
 */
@Api(tags = "Evaluate-前端业务台账接口")
@RestController
@RequestMapping("/web-disaster-evaluate/web/disaster/evaluate/bizdata")
public class BizFrontendDataController {

	private static final Logger logger = LoggerFactory.getLogger(BizFrontendDataController.class);

	@Resource
	private BizFrontendDataService bizFrontendDataService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("BizFrontendDataController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@GetMapping("/list")
	@ApiOperation("按类型列出业务数据（无数据时返回空数组，前端回落 MOCK）")
	public ExecuteResult<List<Map<String, Object>>> list(
			@RequestParam("dataType") String dataType,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "extraType", required = false) String extraType) {
		return bizFrontendDataService.list(dataType, keyword, extraType);
	}

	@PostMapping("/queryWithPage")
	@ApiOperation("分页查询业务数据")
	public ExecuteResult<List<Map<String, Object>>> queryWithPage(@RequestBody BizFrontendDataQueryDto query) {
		return bizFrontendDataService.queryWithPage(query);
	}

	@GetMapping("/getById")
	@ApiOperation("按 ID 查询")
	public ExecuteResult<Map<String, Object>> getById(@RequestParam("id") String id) {
		return bizFrontendDataService.getById(id);
	}

	@PostMapping("/save")
	@ApiOperation("新增业务数据")
	public ExecuteResult<Map<String, Object>> save(@RequestBody BizFrontendDataSaveDto dto, HttpServletRequest request) {
		return bizFrontendDataService.save(dto, operator(request));
	}

	@PostMapping("/update")
	@ApiOperation("更新业务数据")
	public ExecuteResult<Map<String, Object>> update(@RequestBody BizFrontendDataSaveDto dto, HttpServletRequest request) {
		return bizFrontendDataService.update(dto, operator(request));
	}

	@PostMapping("/batchSave")
	@ApiOperation("批量写入业务数据（可用于把前端 MOCK 推送到服务器）")
	public ExecuteResult<Map<String, Object>> batchSave(@RequestBody Map<String, Object> body, HttpServletRequest request) {
		String dataType = body == null ? null : String.valueOf(body.get("dataType"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = body == null ? null : (List<Map<String, Object>>) body.get("list");
		return bizFrontendDataService.batchSave(dataType, list, operator(request));
	}

	@GetMapping("/delete")
	@ApiOperation("删除业务数据")
	public ExecuteResult<String> delete(@RequestParam("id") String id) {
		return bizFrontendDataService.delete(id);
	}

	@PostMapping("/reportArchive/save")
	@ApiOperation("归档评估报告")
	public ExecuteResult<Map<String, Object>> archiveReport(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
		return bizFrontendDataService.archiveReport(payload, operator(request));
	}

	@GetMapping("/reportArchive/list")
	@ApiOperation("归档报告列表")
	public ExecuteResult<List<Map<String, Object>>> listArchives() {
		return bizFrontendDataService.listArchives();
	}

	@GetMapping("/reportArchive/getById")
	@ApiOperation("归档报告详情")
	public ExecuteResult<Map<String, Object>> getArchiveById(@RequestParam("id") String id) {
		return bizFrontendDataService.getArchiveById(id);
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
