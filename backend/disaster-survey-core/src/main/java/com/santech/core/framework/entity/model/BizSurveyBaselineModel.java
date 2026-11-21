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
 * 调查基线包：供灾害评估消费的结构化调查成果。
 * 以 event_id 关联灾害事件主线；保留 sceneId/jsonData 兼容旧链路。
 */
@Data
@Table(name = "T_BIZ_SURVEY_BASELINE")
@ApiModel(description = "调查基线包表")
public class BizSurveyBaselineModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty("基线ID (baseline_id)")
	private String id;

	@Column(name = "EVENT_ID")
	@ApiModelProperty("关联灾害事件ID")
	private String eventId;

	@Column(name = "VERSION_NO")
	@ApiModelProperty("基线版本号")
	private Integer versionNo;

	@Column(name = "STATUS")
	@ApiModelProperty("基线状态 DRAFT / CONFIRMED / ARCHIVED")
	private String status;

	@Column(name = "RS_IMAGE_ID")
	@ApiModelProperty("使用的灾后遥感影像资源ID")
	private String rsImageId;

	@Column(name = "AFFECTED_AREA_GEOJSON")
	@ApiModelProperty("Step2 识别出的受灾范围矢量面")
	private String affectedAreaGeojson;

	@Column(name = "AFFECTED_POPULATION")
	@ApiModelProperty("Step3 受灾人口预估")
	private BigDecimal affectedPopulation;

	@Column(name = "TOWN_AREA_STATS_JSON")
	@ApiModelProperty("Step3 乡镇受灾面积预估列表 JSON")
	private String townAreaStatsJson;

	@Column(name = "EXPOSURE_OVERLAY_JSON")
	@ApiModelProperty("Step3 四类受灾承灾体叠置清单 JSON")
	private String exposureOverlayJson;

	@Column(name = "COMPARE_CHECK_JSON")
	@ApiModelProperty("Step4 遥感解译 vs 速报对比核查结果 JSON")
	private String compareCheckJson;

	@Column(name = "CONFIRMED_TIME")
	@ApiModelProperty("确认锁定时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date confirmedTime;

	@Column(name = "CONFIRMED_BY")
	@ApiModelProperty("确认人")
	private String confirmedBy;

	@Column(name = "SCENE_ID")
	@ApiModelProperty("遥感场景ID（兼容旧链路）")
	private String sceneId;

	@Column(name = "DISTRICT")
	@ApiModelProperty("区县名称")
	private String district;

	@Column(name = "JSON_DATA")
	@ApiModelProperty("完整基线包 JSON（兼容旧链路）")
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
