package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: RolePermissionsSaveModel
 * @Description: 角色权限保存参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "角色权限保存参数信息")
public class RolePermissionsSaveDto {
	
	@ApiModelProperty(value = "角色ID,必填")
	private String roleId;
	
	@ApiModelProperty(value = "角色绑定的用户id,多个以英文逗号分割")
	private String userIds;
	
	@ApiModelProperty(value = "角色绑定的功能id,多个以英文逗号分割")
	private String actionIds;
	
	@ApiModelProperty(value = "角色绑定的运维路由菜单id,多个以英文逗号分割")
	private String routerIds;
	
	@ApiModelProperty(value = "角色绑定的资源目录id,多个以英文逗号分割")
	private String catalogIds;
	
	@ApiModelProperty(value = "当前登录人用户名")
	private String loginUser;
	
	
}
