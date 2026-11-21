package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Data
@ApiModel("前端业务台账查询参数")
public class BizFrontendDataQueryDto {
	@ApiModelProperty("数据类型，如 seawall/assessSar")
	private String dataType;
	@ApiModelProperty("关键字（名称/业务键）")
	private String keyword;
	@ApiModelProperty("子类型，如 wind/wave/storm")
	private String extraType;
	@ApiModelProperty("分页")
	private PageInfo pageInfo;
}
