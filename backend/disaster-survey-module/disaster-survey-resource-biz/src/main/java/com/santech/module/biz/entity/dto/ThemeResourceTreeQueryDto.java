package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ThemeResourceTreeQueryDto
 * @Description: 数据专题资源查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据专题资源树查询参数信息")
public class ThemeResourceTreeQueryDto {
	//检索字段
	@ApiModelProperty(value = "专题树节点名称,全匹配")
	private String themename;
	
	@ApiModelProperty(value = "所属项目,预留，全匹配")
	private String project ;
	
}
