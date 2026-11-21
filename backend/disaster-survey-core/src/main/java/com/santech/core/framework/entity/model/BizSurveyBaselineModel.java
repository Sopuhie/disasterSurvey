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
 * 调查基线包：供灾害评估消费的结构化调查成果
 */
@Data
@Table(name = "T_BIZ_SURVEY_BASELINE")
@ApiModel(description = "调查基线包表")
public class BizSurveyBaselineModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty("主键")
	private String id;

	@Column(name = "SCENE_ID")
	@ApiModelProperty("遥感场景ID")
	private String sceneId;

	@Column(name = "DISTRICT")
	@ApiModelProperty("区县名称")
	private String district;

	@Column(name = "JSON_DATA")
	@ApiModelProperty("完整基线包 JSON")
	private String jsonData;

	@Column(name = "REMARK")
	private String remark;

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
