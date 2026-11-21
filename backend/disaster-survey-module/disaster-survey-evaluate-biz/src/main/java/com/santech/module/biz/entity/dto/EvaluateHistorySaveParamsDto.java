package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 灾害评估运行任务保存参数信息
 **/
@Data
@ApiModel(description= "灾害评估运行参数信息")
public class EvaluateHistorySaveParamsDto {
	
	@ApiModelProperty(value = "评价模型任务名称，非必填,不填给予默认值")
	private String name;

	@ApiModelProperty(value ="灾害类型，1风暴潮、2海风、3海浪，必填")
	private  String  disasterType;
	
	@ApiModelProperty(value ="风暴潮上报数据，JSON类型，风暴潮时必填")
	private  String  reportData;
	
	@ApiModelProperty(value ="叠置目标对象类型，Pop人口密度、Xzq乡镇区划、Dike海堤、Fishery渔港、CultureArea海水养殖区、TouristArea滨海旅游区等，非必填")
	private  String  targetEntity;
	
	@ApiModelProperty(value ="结构损伤评估因子评分，满分100，非必填")
	private  Integer  factorJgss;
	
	@ApiModelProperty(value ="应急响应评估因子评分，满分100，非必填")
	private  Integer  factorYjsy;
	
	@ApiModelProperty(value ="备灾准备评估因子评分，满分100，非必填")
	private  Integer  factorBzzb;
	
	@ApiModelProperty(value ="核查或恢复建议，非必填")
	private  String  recommend;
	
	@ApiModelProperty(value = "当前登录或创建人，必填")
    private String cjr;

	@ApiModelProperty(value = "轻量模式：前端步骤 UI 直达完成，不依赖周期微服务")
	private Boolean liteMode;

	@ApiModelProperty(value = "分析结果 JSON（liteMode 时写入 RESULTDATA）")
	private String resultData;

	@ApiModelProperty(value = "场景ID（前端）")
	private String sceneId;
	
}
