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
 * 灾害事件主表：贯穿灾情调查与灾害评估的顶层主线。
 */
@Data
@Table(name = "T_BIZ_DISASTER_EVENT")
@ApiModel(description = "灾害事件主表")
public class BizDisasterEventModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty("事件唯一ID (event_id)")
	private String id;

	@Column(name = "EVENT_CODE")
	@ApiModelProperty("事件编号")
	private String eventCode;

	@Column(name = "EVENT_NAME")
	@ApiModelProperty("灾害事件名称")
	private String eventName;

	@Column(name = "DISASTER_TYPES")
	@ApiModelProperty("涉及灾种（逗号分隔，取自 DisasterTypeEnum）")
	private String disasterTypes;

	@Column(name = "REGION_CODE")
	@ApiModelProperty("沿海区县行政区划编码")
	private String regionCode;

	@Column(name = "REGION_NAME")
	@ApiModelProperty("行政区划名称")
	private String regionName;

	@Column(name = "OCCUR_TIME")
	@ApiModelProperty("灾害发生时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date occurTime;

	@Column(name = "STATUS")
	@ApiModelProperty("事件状态 ACTIVE / CLOSED")
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
