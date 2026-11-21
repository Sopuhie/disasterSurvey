package com.santech.module.biz.assessment.service;

import java.util.List;
import java.util.Map;

import com.santech.framework.global.ExecuteResult;

/**
 * 评估记录查询与报告归档
 */
public interface AssessmentReportService {

	ExecuteResult<List<Map<String, Object>>> listRecords(String eventId, String disasterType);

	ExecuteResult<Map<String, Object>> generateReport(String eventId, Map<String, Object> body, String operator);

	ExecuteResult<List<Map<String, Object>>> listReports(String disasterType, String keyword);

	ExecuteResult<Map<String, Object>> getReport(String reportId);
}
