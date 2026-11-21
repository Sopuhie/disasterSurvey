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
 */
@Data
@Table(name = "T_BIZ_REPORT_ARCHIVE")
@ApiModel(description = "评估报告归档表")
public class BizReportArchiveModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	private String id;

	@Column(name = "REPORT_NO")
	private String reportNo;

	@Column(name = "TITLE")
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
