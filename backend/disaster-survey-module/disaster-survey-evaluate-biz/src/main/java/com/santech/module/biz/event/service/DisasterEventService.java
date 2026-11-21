package com.santech.module.biz.event.service;

import java.util.List;
import java.util.Map;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.event.entity.dto.DisasterEventCreateDto;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * 灾害事件管理
 */
public interface DisasterEventService {

	ExecuteResult<Map<String, Object>> create(DisasterEventCreateDto dto, String operator);

	ExecuteResult<List<Map<String, Object>>> list(String keyword, String status, PageInfo pageInfo);

	ExecuteResult<Map<String, Object>> getDetail(String eventId);
}
