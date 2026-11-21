package com.santech.module.biz.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "监测数据查询结果信息")
public class EvaluateProgressVo {
	
	@ApiModelProperty(value = "监测数据")
    private String progressName;
	
	@ApiModelProperty(value = "监测数据包含的站点数据")
	private String progressDescription;
	
	@ApiModelProperty(value = "监测数据对应的字段定义")
	private String recordTime;
	
}
