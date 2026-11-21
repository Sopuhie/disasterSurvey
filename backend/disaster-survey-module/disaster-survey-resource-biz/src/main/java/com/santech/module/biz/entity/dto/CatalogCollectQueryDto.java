package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: CatalogCollectQueryDto
 * @Description: 数据目录资源收藏查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据目录收藏查询参数信息")
public class CatalogCollectQueryDto {
	//检索字段
	@ApiModelProperty(value = "目录名称,模糊匹配,非必填")
	private String like_catalogname;
	
	@ApiModelProperty(value = "当前登录人用户名,必填")
	private String loginUser;
	
	//分页字段-预留字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
}
