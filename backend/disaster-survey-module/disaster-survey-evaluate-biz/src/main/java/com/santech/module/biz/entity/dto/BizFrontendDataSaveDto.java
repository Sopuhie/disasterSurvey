package com.santech.module.biz.entity.dto;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("前端业务台账保存参数")
public class BizFrontendDataSaveDto {
	@ApiModelProperty(value = "数据类型", required = true)
	private String dataType;
	@ApiModelProperty("业务对象字段（与前端展示字段一致）")
	private Map<String, Object> data;
}
