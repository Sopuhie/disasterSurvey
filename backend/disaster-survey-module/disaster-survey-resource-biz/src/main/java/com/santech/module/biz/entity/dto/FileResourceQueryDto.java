package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: FileResourceQueryDto
 * @Description: 文件资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "文件资源分页查询参数信息")
public class FileResourceQueryDto {
	
	//检索字段
	@ApiModelProperty(value = "文件资源名称,模糊匹配")
	private String like_name;
	
	@ApiModelProperty(value = "文件资源分类,全匹配")
	private String eq_type;
	
	@ApiModelProperty(value = "文件资源秘密等级,全匹配")
	private String eq_level;
	
	@ApiModelProperty(value = "文件资源状态,全匹配")
	private String eq_status ;
	
	@ApiModelProperty(value = "所属项目,全匹配")
	private String eq_project ;
	
	@ApiModelProperty(value = "管理员标识")
	private String adminFlag;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
