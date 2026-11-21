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
 * 系统启动时检测并创建前端业务台账相关表（达梦）。
 */
@Component
@Order(50)
public class BizSchemaInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(BizSchemaInitializer.class);

	@Resource
	private BizFrontendDataDao bizFrontendDataDao;

	@Override
	public void run(ApplicationArguments args) {
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

		ensureTable("T_BIZ_REPORT_ARCHIVE",
				"CREATE TABLE T_BIZ_REPORT_ARCHIVE ("
						+ "ID VARCHAR(64) NOT NULL,"
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
		ensureIndex("IDX_BIZ_RPT_NO", "CREATE INDEX IDX_BIZ_RPT_NO ON T_BIZ_REPORT_ARCHIVE(REPORT_NO)");

		ensureTable("T_BIZ_HAZARD_DOSSIER",
				"CREATE TABLE T_BIZ_HAZARD_DOSSIER ("
						+ "ID VARCHAR(64) NOT NULL,"
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
		ensureIndex("IDX_BIZ_DOSSIER_KEY", "CREATE INDEX IDX_BIZ_DOSSIER_KEY ON T_BIZ_HAZARD_DOSSIER(BIZ_KEY)");
		ensureIndex("IDX_BIZ_DOSSIER_ARCH", "CREATE INDEX IDX_BIZ_DOSSIER_ARCH ON T_BIZ_HAZARD_DOSSIER(ARCHIVE_STATUS)");

		ensureTable("T_BIZ_SURVEY_BASELINE",
				"CREATE TABLE T_BIZ_SURVEY_BASELINE ("
						+ "ID VARCHAR(64) NOT NULL,"
						+ "SCENE_ID VARCHAR(128) NOT NULL,"
						+ "DISTRICT VARCHAR(128),"
						+ "JSON_DATA CLOB,"
						+ "REMARK VARCHAR(512),"
						+ "CJR VARCHAR(64),"
						+ "CJSJ TIMESTAMP,"
						+ "ZHGXR VARCHAR(64),"
						+ "ZHGXSJ TIMESTAMP,"
						+ "CONSTRAINT PK_T_BIZ_SURVEY_BASELINE PRIMARY KEY (ID)"
						+ ")");
		ensureIndex("IDX_BIZ_BASELINE_SCENE", "CREATE INDEX IDX_BIZ_BASELINE_SCENE ON T_BIZ_SURVEY_BASELINE(SCENE_ID)");

		// 调查/评估任务历史表补充场景与轻量模式列（表通常已存在）
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

		logger.info("BizSchemaInitializer 完成：前端业务台账表检查/创建结束");
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
