package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: RolePermissionsQueryDto
 * @Description: 角色查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "角色查询参数信息")
public class RolePermissionsQueryDto {
	//检索字段
	@ApiModelProperty(value = "角色id,多个以英文逗号分割")
	private String roles;
	
	@ApiModelProperty(value = "管理员标识,0非管理员，1管理员")
	private String adminFlag;
	
	
}
