package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

/**
 * 灾害评估记录表：取代 T_BIZ_FRONTEND_DATA 的评估数据存储。
 */
@Data
@Table(name = "T_BIZ_ASSESSMENT_RECORD")
@ApiModel(description = "灾害评估记录表")
public class BizAssessmentRecordModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty("评估记录ID (assessment_id)")
	private String id;

	@Column(name = "EVENT_ID")
	@ApiModelProperty("关联灾害事件ID")
	private String eventId;

	@Column(name = "BASELINE_ID")
	@ApiModelProperty("关联已确认的调查基线ID")
	private String baselineId;

	@Column(name = "DISASTER_TYPE")
	@ApiModelProperty("灾种：STORM_SURGE / SEA_WIND / SEA_WAVE")
	private String disasterType;

	@Column(name = "SUB_TYPE")
	@ApiModelProperty("评估子项，取自 AssessmentSubTypeEnum")
	private String subType;

	@Column(name = "REGION_CODE")
	@ApiModelProperty("评估区县编码")
	private String regionCode;

	@Column(name = "INPUT_PARAMS_JSON")
	@ApiModelProperty("评估输入参数 JSON")
	private String inputParamsJson;

	@Column(name = "RESULT_DATA_JSON")
	@ApiModelProperty("结构化计算结果 JSON")
	private String resultDataJson;

	@Column(name = "COMPUTE_MODE")
	@ApiModelProperty("计算模式：SYNC / ASYNC")
	private String computeMode;

	@Column(name = "STATUS")
	@ApiModelProperty("任务状态，取自 ComputeTaskStatusEnum")
	private String status;

	@Column(name = "CJR")
	@ApiModelProperty("创建人")
	private String cjr;

	@Column(name = "CJSJ")
	@ApiModelProperty("创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date cjsj;

	@Column(name = "ZHGXR")
	@ApiModelProperty("最后更新人")
	private String zhgxr;

	@Column(name = "ZHGXSJ")
	@ApiModelProperty("最后更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date zhgxsj;
}
