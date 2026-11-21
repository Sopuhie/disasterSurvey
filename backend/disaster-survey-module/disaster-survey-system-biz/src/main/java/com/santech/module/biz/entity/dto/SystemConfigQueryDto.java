package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: SystemConfigQueryDto
 * @Description: 系统基础配置分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "系统基础配置分页查询参数信息")
public class SystemConfigQueryDto {
	//检索字段
	@ApiModelProperty(value = "配置名称,模糊匹配")
	private String like_name;
	@ApiModelProperty(value = "系统标题,模糊匹配")
	private String like_title;
	@ApiModelProperty(value = "管理员标识,预留")
	private String adminFlag ;
	@ApiModelProperty(value = "当前登录用户ID,预留")
	private String loginUserId;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
