package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ServiceStyleResourceQueryDto
 * @Description: 服务样式资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "服务样式资源分页查询参数信息")
public class ServiceStyleResourceQueryDto {
	
	//检索字段
	@ApiModelProperty(value = "样式资源名称,模糊匹配")
	private String like_name;
	
	@ApiModelProperty(value = "样式资源分类,全匹配") 
	private String eq_type;
	
	@ApiModelProperty(value = "样式资源状态,全匹配")
	private String eq_status ;
	
	@ApiModelProperty(value = "所属项目,全匹配")
	private String eq_project ;
	
	@ApiModelProperty(value = "管理员标识,0非管理员，1管理员")
	private String adminFlag;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
