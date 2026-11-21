package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.BizFrontendDataQueryDto;
import com.santech.module.biz.entity.dto.BizFrontendDataSaveDto;

public interface BizFrontendDataService {

	ExecuteResult<List<Map<String, Object>>> list(String dataType, String keyword, String extraType);

	ExecuteResult<List<Map<String, Object>>> queryWithPage(BizFrontendDataQueryDto query);

	ExecuteResult<Map<String, Object>> getById(String id);

	ExecuteResult<Map<String, Object>> save(BizFrontendDataSaveDto dto, String operator);

	ExecuteResult<Map<String, Object>> update(BizFrontendDataSaveDto dto, String operator);

	ExecuteResult<Map<String, Object>> batchSave(String dataType, java.util.List<java.util.Map<String, Object>> list, String operator);

	ExecuteResult<String> delete(String id);

	ExecuteResult<Map<String, Object>> archiveReport(Map<String, Object> payload, String operator);

	ExecuteResult<List<Map<String, Object>>> listArchives();

	ExecuteResult<Map<String, Object>> getArchiveById(String id);
}
