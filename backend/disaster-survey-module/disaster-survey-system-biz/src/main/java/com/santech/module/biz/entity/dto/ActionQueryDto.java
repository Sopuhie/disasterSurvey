package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ActionQueryDto
 * @Description: 功能分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "功能分页查询参数信息")
public class ActionQueryDto {
	//检索字段
	@ApiModelProperty(value = "功能名称,模糊匹配")
	private String like_name;
	@ApiModelProperty(value = "功能别名,模糊匹配")
	private String like_alias;
	@ApiModelProperty(value = "节点类型  folder，meun，action,leftmenu,outmodule,全匹配")
	private String eq_nodetype;
	@ApiModelProperty(value = "是否可见")
	private String eq_visible;
	@ApiModelProperty(value = "所属子系统")
	private String eq_system;
	@ApiModelProperty(value = "管理员标识")
	private String adminFlag ;
	@ApiModelProperty(value = "当前登录用户ID")
	private String loginUserId;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
