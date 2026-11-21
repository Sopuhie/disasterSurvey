package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: RolePermissionsManagerModel
 * @Description: 单个角色权限信息，运维使用
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "单个角色权限信息，运维使用")
public class RolePermissionsManagerVo {
	
	@ApiModelProperty(value = "菜单功能权限信息")
	private List<String> actionPermission;
	
	@ApiModelProperty(value = "运维路由权限信息")
	private List<String> routerPermission;
	
	@ApiModelProperty(value = "资源目录权限信息")
	private List<String> catalogPermission;
	
	@ApiModelProperty(value = "关联用户信息")
	private List<String> userPermission;
	
	
}
