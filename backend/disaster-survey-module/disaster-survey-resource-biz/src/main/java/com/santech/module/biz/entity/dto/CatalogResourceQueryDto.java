package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: CatalogResourceQueryDto
 * @Description: 数据目录资源查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据目录资源查询参数信息")
public class CatalogResourceQueryDto {
	//检索字段
	@ApiModelProperty(value = "目录名称,模糊匹配")
	private String like_name;
	
	@ApiModelProperty(value = "所属项目,全匹配")
	private String eq_project ;
	
}
