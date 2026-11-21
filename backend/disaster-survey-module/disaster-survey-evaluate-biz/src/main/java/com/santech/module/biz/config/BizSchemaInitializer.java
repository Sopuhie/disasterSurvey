package com.santech.module.biz.config;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.santech.core.framework.dao.BizFrontendDataDao;

/**
 * 系统启动时检测并创建/补齐业务台账相关表（达梦）。
 * 阶段一：灾害事件主线、调查基线结构化、评估记录、档案/报告规范字段。
 */
@Component
@Order(50)
public class BizSchemaInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(BizSchemaInitializer.class);

	@Resource
	private BizFrontendDataDao bizFrontendDataDao;

	@Override
	public void run(ApplicationArguments args) {
		ensureDisasterEventTable();
		ensureSurveyBaselineTable();
		ensureHazardDossierTable();
		ensureAssessmentRecordTable();
		ensureReportArchiveTable();
		ensureFrontendDataTable();
		ensureLegacyHistoryColumns();

		logger.info("BizSchemaInitializer 完成：业务台账表检查/创建/补齐字段结束");
	}

	/** 3.1 灾害事件主表 */
	private void ensureDisasterEventTable() {
		ensureTable("T_BIZ_DISASTER_EVENT",
				"CREATE TABLE T_BIZ_DISASTER_EVENT ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "EVENT_CODE VARCHAR(128),"
						+ "EVENT_NAME VARCHAR(256),"
						+ "DISASTER_TYPES VARCHAR(256),"
						+ "REGION_CODE VARCHAR(64),"
						+ "REGION_NAME VARCHAR(128),"
						+ "OCCUR_TIME TIMESTAMP,"
						+ "STATUS VARCHAR(32),"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_DISASTER_EVENT PRIMARY KEY (ID)"
						+ ")");
		ensureIndex("IDX_BIZ_EVENT_CODE", "CREATE INDEX IDX_BIZ_EVENT_CODE ON T_BIZ_DISASTER_EVENT(EVENT_CODE)");
		ensureIndex("IDX_BIZ_EVENT_STATUS", "CREATE INDEX IDX_BIZ_EVENT_STATUS ON T_BIZ_DISASTER_EVENT(STATUS)");
		ensureIndex("IDX_BIZ_EVENT_REGION", "CREATE INDEX IDX_BIZ_EVENT_REGION ON T_BIZ_DISASTER_EVENT(REGION_CODE)");
	}

	/** 3.2 调查基线表：新建完整结构；已存在则平滑补齐字段 */
	private void ensureSurveyBaselineTable() {
		ensureTable("T_BIZ_SURVEY_BASELINE",
				"CREATE TABLE T_BIZ_SURVEY_BASELINE ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "EVENT_ID VARCHAR(64),"
						+ "VERSION_NO INT,"
						+ "STATUS VARCHAR(32),"
						+ "RS_IMAGE_ID VARCHAR(128),"
						+ "AFFECTED_AREA_GEOJSON CLOB,"
						+ "AFFECTED_POPULATION DECIMAL(18,2),"
						+ "TOWN_AREA_STATS_JSON CLOB,"
						+ "EXPOSURE_OVERLAY_JSON CLOB,"
						+ "COMPARE_CHECK_JSON CLOB,"
						+ "CONFIRMED_TIME TIMESTAMP,"
						+ "CONFIRMED_BY VARCHAR(64),"
						+ "SCENE_ID VARCHAR(128),"
						+ "DISTRICT VARCHAR(128),"
						+ "JSON_DATA CLOB,"
						+ "REMARK VARCHAR(512),"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_SURVEY_BASELINE PRIMARY KEY (ID)"
						+ ")");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "EVENT_ID",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD EVENT_ID VARCHAR(64)");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "VERSION_NO",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD VERSION_NO INT");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "STATUS",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD STATUS VARCHAR(32)");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "RS_IMAGE_ID",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD RS_IMAGE_ID VARCHAR(128)");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "AFFECTED_AREA_GEOJSON",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD AFFECTED_AREA_GEOJSON CLOB");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "AFFECTED_POPULATION",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD AFFECTED_POPULATION DECIMAL(18,2)");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "TOWN_AREA_STATS_JSON",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD TOWN_AREA_STATS_JSON CLOB");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "EXPOSURE_OVERLAY_JSON",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD EXPOSURE_OVERLAY_JSON CLOB");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "COMPARE_CHECK_JSON",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD COMPARE_CHECK_JSON CLOB");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "CONFIRMED_TIME",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD CONFIRMED_TIME TIMESTAMP");
		ensureColumn("T_BIZ_SURVEY_BASELINE", "CONFIRMED_BY",
				"ALTER TABLE T_BIZ_SURVEY_BASELINE ADD CONFIRMED_BY VARCHAR(64)");
		ensureIndex("IDX_BIZ_BASELINE_SCENE", "CREATE INDEX IDX_BIZ_BASELINE_SCENE ON T_BIZ_SURVEY_BASELINE(SCENE_ID)");
		ensureIndex("IDX_BIZ_BASELINE_EVENT", "CREATE INDEX IDX_BIZ_BASELINE_EVENT ON T_BIZ_SURVEY_BASELINE(EVENT_ID)");
		ensureIndex("IDX_BIZ_BASELINE_STATUS", "CREATE INDEX IDX_BIZ_BASELINE_STATUS ON T_BIZ_SURVEY_BASELINE(STATUS)");
	}

	/** 3.3 承灾体档案表：新建完整结构；已存在则平滑补齐字段 */
	private void ensureHazardDossierTable() {
		ensureTable("T_BIZ_HAZARD_DOSSIER",
				"CREATE TABLE T_BIZ_HAZARD_DOSSIER ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "EVENT_ID VARCHAR(64),"
						+ "BASELINE_ID VARCHAR(64),"
						+ "EXPOSURE_TYPE VARCHAR(64),"
						+ "EXPOSURE_ID VARCHAR(128),"
						+ "RS_CLIP_IMAGE_URL VARCHAR(1024),"
						+ "FIELD_ATTACHMENTS_JSON CLOB,"
						+ "BIZ_KEY VARCHAR(256) NOT NULL,"
						+ "HAZARD_TYPE VARCHAR(64),"
						+ "HAZARD_NAME VARCHAR(256),"
						+ "VERIFY_STATUS VARCHAR(64),"
						+ "ARCHIVE_STATUS VARCHAR(64),"
						+ "ARCHIVE_NO VARCHAR(128),"
						+ "JSON_DATA CLOB,"
						+ "REMARK VARCHAR(512),"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_HAZARD_DOSSIER PRIMARY KEY (ID)"
						+ ")");
		ensureColumn("T_BIZ_HAZARD_DOSSIER", "EVENT_ID",
				"ALTER TABLE T_BIZ_HAZARD_DOSSIER ADD EVENT_ID VARCHAR(64)");
		ensureColumn("T_BIZ_HAZARD_DOSSIER", "BASELINE_ID",
				"ALTER TABLE T_BIZ_HAZARD_DOSSIER ADD BASELINE_ID VARCHAR(64)");
		ensureColumn("T_BIZ_HAZARD_DOSSIER", "EXPOSURE_TYPE",
				"ALTER TABLE T_BIZ_HAZARD_DOSSIER ADD EXPOSURE_TYPE VARCHAR(64)");
		ensureColumn("T_BIZ_HAZARD_DOSSIER", "EXPOSURE_ID",
				"ALTER TABLE T_BIZ_HAZARD_DOSSIER ADD EXPOSURE_ID VARCHAR(128)");
		ensureColumn("T_BIZ_HAZARD_DOSSIER", "RS_CLIP_IMAGE_URL",
				"ALTER TABLE T_BIZ_HAZARD_DOSSIER ADD RS_CLIP_IMAGE_URL VARCHAR(1024)");
		ensureColumn("T_BIZ_HAZARD_DOSSIER", "FIELD_ATTACHMENTS_JSON",
				"ALTER TABLE T_BIZ_HAZARD_DOSSIER ADD FIELD_ATTACHMENTS_JSON CLOB");
		ensureIndex("IDX_BIZ_DOSSIER_KEY", "CREATE INDEX IDX_BIZ_DOSSIER_KEY ON T_BIZ_HAZARD_DOSSIER(BIZ_KEY)");
		ensureIndex("IDX_BIZ_DOSSIER_ARCH", "CREATE INDEX IDX_BIZ_DOSSIER_ARCH ON T_BIZ_HAZARD_DOSSIER(ARCHIVE_STATUS)");
		ensureIndex("IDX_BIZ_DOSSIER_EVENT", "CREATE INDEX IDX_BIZ_DOSSIER_EVENT ON T_BIZ_HAZARD_DOSSIER(EVENT_ID)");
		ensureIndex("IDX_BIZ_DOSSIER_EXPOSURE",
				"CREATE INDEX IDX_BIZ_DOSSIER_EXPOSURE ON T_BIZ_HAZARD_DOSSIER(EXPOSURE_TYPE)");
	}

	/** 3.4 灾害评估记录表（新增） */
	private void ensureAssessmentRecordTable() {
		ensureTable("T_BIZ_ASSESSMENT_RECORD",
				"CREATE TABLE T_BIZ_ASSESSMENT_RECORD ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "EVENT_ID VARCHAR(64) NOT NULL,"
						+ "BASELINE_ID VARCHAR(64),"
						+ "DISASTER_TYPE VARCHAR(64) NOT NULL,"
						+ "SUB_TYPE VARCHAR(64) NOT NULL,"
						+ "REGION_CODE VARCHAR(64),"
						+ "INPUT_PARAMS_JSON CLOB,"
						+ "RESULT_DATA_JSON CLOB,"
						+ "COMPUTE_MODE VARCHAR(32),"
						+ "STATUS VARCHAR(32),"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_ASSESSMENT_RECORD PRIMARY KEY (ID)"
						+ ")");
		ensureIndex("IDX_BIZ_ASSESS_EVENT", "CREATE INDEX IDX_BIZ_ASSESS_EVENT ON T_BIZ_ASSESSMENT_RECORD(EVENT_ID)");
		ensureIndex("IDX_BIZ_ASSESS_TYPE",
				"CREATE INDEX IDX_BIZ_ASSESS_TYPE ON T_BIZ_ASSESSMENT_RECORD(DISASTER_TYPE, SUB_TYPE)");
		ensureIndex("IDX_BIZ_ASSESS_STATUS", "CREATE INDEX IDX_BIZ_ASSESS_STATUS ON T_BIZ_ASSESSMENT_RECORD(STATUS)");
	}

	/** 3.5 评估报告归档表：新建完整结构；已存在则平滑补齐字段 */
	private void ensureReportArchiveTable() {
		ensureTable("T_BIZ_REPORT_ARCHIVE",
				"CREATE TABLE T_BIZ_REPORT_ARCHIVE ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "EVENT_ID VARCHAR(64),"
						+ "ASSESSMENT_ID VARCHAR(64),"
						+ "DISASTER_TYPE VARCHAR(64),"
						+ "REPORT_FILE_PATH VARCHAR(1024),"
						+ "REPORT_FORMAT VARCHAR(32),"
						+ "SUMMARY_JSON CLOB,"
						+ "REPORT_NO VARCHAR(128),"
						+ "TITLE VARCHAR(512),"
						+ "TYPE_ID VARCHAR(64),"
						+ "TYPE_LABEL VARCHAR(64),"
						+ "DISTRICT VARCHAR(128),"
						+ "SCENE_NAME VARCHAR(256),"
						+ "GRADE VARCHAR(64),"
						+ "TOTAL_SCORE DECIMAL(12,2),"
						+ "ARCHIVED_AT VARCHAR(64),"
						+ "ARCHIVED_BY VARCHAR(128),"
						+ "REMARK VARCHAR(512),"
						+ "SNAPSHOT CLOB,"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_REPORT_ARCHIVE PRIMARY KEY (ID)"
						+ ")");
		ensureColumn("T_BIZ_REPORT_ARCHIVE", "EVENT_ID",
				"ALTER TABLE T_BIZ_REPORT_ARCHIVE ADD EVENT_ID VARCHAR(64)");
		ensureColumn("T_BIZ_REPORT_ARCHIVE", "ASSESSMENT_ID",
				"ALTER TABLE T_BIZ_REPORT_ARCHIVE ADD ASSESSMENT_ID VARCHAR(64)");
		ensureColumn("T_BIZ_REPORT_ARCHIVE", "DISASTER_TYPE",
				"ALTER TABLE T_BIZ_REPORT_ARCHIVE ADD DISASTER_TYPE VARCHAR(64)");
		ensureColumn("T_BIZ_REPORT_ARCHIVE", "REPORT_FILE_PATH",
				"ALTER TABLE T_BIZ_REPORT_ARCHIVE ADD REPORT_FILE_PATH VARCHAR(1024)");
		ensureColumn("T_BIZ_REPORT_ARCHIVE", "REPORT_FORMAT",
				"ALTER TABLE T_BIZ_REPORT_ARCHIVE ADD REPORT_FORMAT VARCHAR(32)");
		ensureColumn("T_BIZ_REPORT_ARCHIVE", "SUMMARY_JSON",
				"ALTER TABLE T_BIZ_REPORT_ARCHIVE ADD SUMMARY_JSON CLOB");
		ensureIndex("IDX_BIZ_RPT_NO", "CREATE INDEX IDX_BIZ_RPT_NO ON T_BIZ_REPORT_ARCHIVE(REPORT_NO)");
		ensureIndex("IDX_BIZ_RPT_EVENT", "CREATE INDEX IDX_BIZ_RPT_EVENT ON T_BIZ_REPORT_ARCHIVE(EVENT_ID)");
		ensureIndex("IDX_BIZ_RPT_DISASTER", "CREATE INDEX IDX_BIZ_RPT_DISASTER ON T_BIZ_REPORT_ARCHIVE(DISASTER_TYPE)");
	}

	/** 万能表兼容保留 */
	private void ensureFrontendDataTable() {
		ensureTable("T_BIZ_FRONTEND_DATA",
				"CREATE TABLE T_BIZ_FRONTEND_DATA ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "DATA_TYPE VARCHAR(64) NOT NULL,"
						+ "BIZ_KEY VARCHAR(128),"
						+ "NAME VARCHAR(256),"
						+ "EXTRA_TYPE VARCHAR(64),"
						+ "JSON_DATA CLOB,"
						+ "SORT_INDEX INT,"
						+ "REMARK VARCHAR(512),"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_FRONTEND_DATA PRIMARY KEY (ID)"
						+ ")");
		ensureIndex("IDX_BIZ_FE_TYPE", "CREATE INDEX IDX_BIZ_FE_TYPE ON T_BIZ_FRONTEND_DATA(DATA_TYPE)");
		ensureIndex("IDX_BIZ_FE_BIZKEY", "CREATE INDEX IDX_BIZ_FE_BIZKEY ON T_BIZ_FRONTEND_DATA(BIZ_KEY)");
	}

	/** 调查/评估任务历史表补充场景与轻量模式列（表通常已存在） */
	private void ensureLegacyHistoryColumns() {
		ensureColumn("T_SURVEY_EVALUATE_HISTORY", "SCENEID",
				"ALTER TABLE T_SURVEY_EVALUATE_HISTORY ADD SCENEID VARCHAR(128)");
		ensureColumn("T_SURVEY_EVALUATE_HISTORY", "LITEMODE",
				"ALTER TABLE T_SURVEY_EVALUATE_HISTORY ADD LITEMODE VARCHAR(8)");
		ensureColumn("T_EVALUATE_HISTORY", "SCENEID",
				"ALTER TABLE T_EVALUATE_HISTORY ADD SCENEID VARCHAR(128)");
		ensureColumn("T_EVALUATE_HISTORY", "LITEMODE",
				"ALTER TABLE T_EVALUATE_HISTORY ADD LITEMODE VARCHAR(8)");
		ensureIndex("IDX_SURVEY_HIS_SCENE", "CREATE INDEX IDX_SURVEY_HIS_SCENE ON T_SURVEY_EVALUATE_HISTORY(SCENEID)");
		ensureIndex("IDX_EVAL_HIS_SCENE", "CREATE INDEX IDX_EVAL_HIS_SCENE ON T_EVALUATE_HISTORY(SCENEID)");
	}

	private void ensureColumn(String tableName, String columnName, String alterSql) {
		if (!bizFrontendDataDao.tableExists(tableName)) {
			logger.warn("表不存在，跳过加列：{}.{}", tableName, columnName);
			return;
		}
		if (bizFrontendDataDao.columnExists(tableName, columnName)) {
			logger.info("列已存在，跳过：{}.{}", tableName, columnName);
			return;
		}
		try {
			bizFrontendDataDao.executeDdl(alterSql);
			logger.info("已加列：{}.{}", tableName, columnName);
		} catch (Exception e) {
			logger.error("加列失败 {}.{}，原因：{}", tableName, columnName, e.getMessage(), e);
		}
	}

	private void ensureTable(String tableName, String ddl) {
		if (bizFrontendDataDao.tableExists(tableName)) {
			logger.info("表已存在，跳过创建：{}", tableName);
			return;
		}
		try {
			bizFrontendDataDao.executeDdl(ddl);
			logger.info("已创建表：{}", tableName);
		} catch (Exception e) {
			logger.error("创建表失败 {}，原因：{}", tableName, e.getMessage(), e);
		}
	}

	private void ensureIndex(String indexName, String ddl) {
		try {
			bizFrontendDataDao.executeDdl(ddl);
			logger.info("已确保索引：{}", indexName);
		} catch (Exception e) {
			// 索引已存在时达梦会报错，忽略
			logger.debug("索引 {} 可能已存在：{}", indexName, e.getMessage());
		}
	}
}
