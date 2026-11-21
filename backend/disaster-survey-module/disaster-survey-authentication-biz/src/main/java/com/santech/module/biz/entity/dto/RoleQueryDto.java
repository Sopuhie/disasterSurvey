package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: RoleQueryDto
 * @Description: 角色查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "角色查询参数信息")
public class RoleQueryDto {
	//检索字段
	@ApiModelProperty(value = "当前登录用户所具有的角色id,多个以英文逗号分割，当为管理员时，该字段必填")
	private String roleids;
	@ApiModelProperty(value = "角色名称,全匹配")
	private String eq_rolename;
	@ApiModelProperty(value = "角色名称,模糊匹配")
	private String like_rolename;
	
	@ApiModelProperty(value = "管理员标识")
	private String adminFlag ;
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
