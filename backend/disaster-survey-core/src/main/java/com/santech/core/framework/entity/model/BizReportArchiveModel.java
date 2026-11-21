package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

/**
 * 灾害评估报告归档台账。
 * 补充 event_id / assessment_id / disaster_type 等主线关联字段；TITLE 即 report_title。
 */
@Data
@Table(name = "T_BIZ_REPORT_ARCHIVE")
@ApiModel(description = "评估报告归档表")
public class BizReportArchiveModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	private String id;

	@Column(name = "EVENT_ID")
	@ApiModelProperty("关联灾害事件ID")
	private String eventId;

	@Column(name = "ASSESSMENT_ID")
	@ApiModelProperty("关联评估记录ID")
	private String assessmentId;

	@Column(name = "DISASTER_TYPE")
	@ApiModelProperty("灾种：STORM_SURGE / SEA_WIND / SEA_WAVE")
	private String disasterType;

	@Column(name = "REPORT_FILE_PATH")
	@ApiModelProperty("报告文件存储路径")
	private String reportFilePath;

	@Column(name = "REPORT_FORMAT")
	@ApiModelProperty("报告格式：DOCX / PDF")
	private String reportFormat;

	@Column(name = "SUMMARY_JSON")
	@ApiModelProperty("报告核心结论摘要 JSON")
	private String summaryJson;

	@Column(name = "REPORT_NO")
	private String reportNo;

	@Column(name = "TITLE")
	@ApiModelProperty("报告标题 (report_title)")
	private String title;

	@Column(name = "TYPE_ID")
	private String typeId;

	@Column(name = "TYPE_LABEL")
	private String typeLabel;

	@Column(name = "DISTRICT")
	private String district;

	@Column(name = "SCENE_NAME")
	private String sceneName;

	@Column(name = "GRADE")
	private String grade;

	@Column(name = "TOTAL_SCORE")
	private BigDecimal totalScore;

	@Column(name = "ARCHIVED_AT")
	private String archivedAt;

	@Column(name = "ARCHIVED_BY")
	private String archivedBy;

	@Column(name = "REMARK")
	private String remark;

	@Column(name = "SNAPSHOT")
	@ApiModelProperty("完整报告快照 JSON")
	private String snapshot;

	@Column(name = "CJR")
	private String cjr;

	@Column(name = "CJSJ")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date cjsj;

	@Column(name = "ZHGXR")
	private String zhgxr;

	@Column(name = "ZHGXSJ")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date zhgxsj;
}
