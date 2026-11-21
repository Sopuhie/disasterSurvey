package com.santech.module.biz.event.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建灾害事件请求
 */
@Data
@ApiModel(description = "创建灾害事件请求")
public class DisasterEventCreateDto {

	@ApiModelProperty("事件编号（可选，空则自动生成）")
	private String eventCode;

	@ApiModelProperty(value = "灾害事件名称", required = true)
	private String eventName;

	@ApiModelProperty("涉及灾种，逗号分隔或数组语义字符串，取自 DisasterTypeEnum")
	private String disasterTypes;

	@ApiModelProperty("沿海区县行政区划编码")
	private String regionCode;

	@ApiModelProperty("行政区划名称")
	private String regionName;

	@ApiModelProperty("灾害发生时间，yyyy-MM-dd HH:mm:ss")
	private String occurTime;

	@ApiModelProperty("事件状态，默认 ACTIVE")
	private String status;
}
