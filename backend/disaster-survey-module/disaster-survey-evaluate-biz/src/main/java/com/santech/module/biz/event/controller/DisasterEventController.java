package com.santech.module.biz.event.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.module.biz.event.entity.dto.DisasterEventCreateDto;
import com.santech.module.biz.event.service.DisasterEventService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * 灾害事件管理（重构主线入口）
 */
@Api(tags = "API-灾害事件")
@RestController
@RequestMapping("/api/v1/disaster-events")
public class DisasterEventController {

	private static final Logger logger = LoggerFactory.getLogger(DisasterEventController.class);

	@Resource
	private DisasterEventService disasterEventService;

	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e) {
		logger.error("DisasterEventController error", e);
		return ExecuteResult.error(e.getMessage(), null);
	}

	@PostMapping
	@ApiOperation("创建灾害事件")
	public ExecuteResult<Map<String, Object>> create(@RequestBody DisasterEventCreateDto dto,
			HttpServletRequest request) {
		return disasterEventService.create(dto, operator(request));
	}

	@GetMapping
	@ApiOperation("分页/条件查询灾害事件列表")
	public ExecuteResult<List<Map<String, Object>>> list(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "current", required = false) Integer current,
			@RequestParam(value = "size", required = false) Integer size) {
		PageInfo pageInfo = null;
		if (current != null || size != null) {
			pageInfo = new PageInfo(SqlUtil.getCurrentPage(current), SqlUtil.getPageSize(size));
		}
		return disasterEventService.list(keyword, status, pageInfo);
	}

	@GetMapping("/{eventId}")
	@ApiOperation("查询事件详情（含基线与评估进度概览）")
	public ExecuteResult<Map<String, Object>> detail(@PathVariable("eventId") String eventId) {
		return disasterEventService.getDetail(eventId);
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
