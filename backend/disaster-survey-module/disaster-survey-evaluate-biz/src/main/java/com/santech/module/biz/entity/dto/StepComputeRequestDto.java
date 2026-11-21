package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 步骤 UI 服务端计算请求（识别 / 叠置 / 损失）
 */
@Data
@ApiModel(description = "步骤计算请求")
public class StepComputeRequestDto {

	@ApiModelProperty("场景ID")
	private String sceneId;

	@ApiModelProperty("场景名称")
	private String sceneName;

	@ApiModelProperty("场景日期")
	private String sceneDate;

	@ApiModelProperty("区域提示")
	private String regionHint;

	@ApiModelProperty("淹没比例，默认 0.45")
	private Double inundationRatio;

	@ApiModelProperty("区县名称")
	private String districtName;

	@ApiModelProperty("地市")
	private String city;

	@ApiModelProperty("区县统计面积 km²")
	private Double statArea;

	@ApiModelProperty("人口密度")
	private Double density;

	@ApiModelProperty("叠置目标：Pop/Xzq/Dike/Fishery/CultureArea/TouristArea 或 population/district/seawall/...")
	private String targetEntity;

	@ApiModelProperty("承灾体名称过滤（可选）")
	private String hazardName;

	@ApiModelProperty("灾害类型 1风暴潮 2海风 3海浪")
	private String disasterType;

	@ApiModelProperty("创建人")
	private String cjr;

	@ApiModelProperty("是否落库为 lite 完成任务，默认 true")
	private Boolean persist;
}
