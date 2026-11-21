package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 灾情调查分析任务更新参数信息
 **/
@Data
@ApiModel(description= "灾情调查分析任务更新参数信息")
public class DisasterSurveyTaskUpdateParamsDto {
	
	@ApiModelProperty(value = "评价任务ID，必填")
	private String id;

	@ApiModelProperty(value ="运行总体状态，1任务创建，2运行中，3运行完成，4运行失败，非必填")
	private  String  status;
	
	@ApiModelProperty(value ="失败原因，非必填")
	private  String  faildReason;
	
	@ApiModelProperty(value ="结果文件路径（如裁切统计生成的新shp结果文件），非必填")
	private  String  resultPath;
	
	@ApiModelProperty(value ="分析结果统计数据，json的字符串，非必填")
	private  String  resultData;
	
	@ApiModelProperty(value = "更新人，必填")
    private String zhgxr;
	
}
