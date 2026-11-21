package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 灾情调查分析任务保存参数信息
 **/
@Data
@ApiModel(description= "灾情调查分析参数信息")
public class DisasterSurveyTaskSaveParamsDto {
	
	@ApiModelProperty(value = "评价模型任务名称，非必填，不填会给默认名字")
	private String name;

	@ApiModelProperty(value ="灾情数据SHP文件路径（如受灾空间数据）；liteMode=true 时可空")
	private  String  dataPath;
	
	@ApiModelProperty(value ="叠置目标对象类型，Pop人口密度、Xzq乡镇区划、Dike海堤、Fishery渔港、CultureArea海水养殖区、TouristArea滨海旅游区等，必填")
	private  String  targetEntity;
	
	@ApiModelProperty(value = "当前登录或创建人，必填")
    private String cjr;

	@ApiModelProperty(value = "轻量模式：前端步骤 UI 直达完成，不依赖 SHP 引擎与周期微服务")
	private Boolean liteMode;

	@ApiModelProperty(value = "遥感场景ID（前端）")
	private String sceneId;

	@ApiModelProperty(value = "分析结果 JSON（liteMode 时写入 RESULTDATA）")
	private String resultData;
	
}
