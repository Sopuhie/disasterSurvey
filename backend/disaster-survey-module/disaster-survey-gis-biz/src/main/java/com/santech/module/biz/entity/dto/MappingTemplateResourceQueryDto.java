package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: MappingTemplateResourceQueryDto
 * @Description: 专题制图模板资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "专题制图模板资源分页查询参数信息")
public class MappingTemplateResourceQueryDto {
	//检索字段
	@ApiModelProperty(value = "模板名称,模糊匹配")
	private String like_name;
	@ApiModelProperty(value = "分享类型,全匹配")
	private String eq_isshare;
	@ApiModelProperty(value = "当前登录人用户名，默认必传字段,全匹配")
	private String loginUser ;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
