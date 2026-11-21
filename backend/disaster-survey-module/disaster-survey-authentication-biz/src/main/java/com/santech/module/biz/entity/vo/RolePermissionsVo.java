package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: RolePermissionsModel
 * @Description: 角色权限信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "角色权限信息")
public class RolePermissionsVo {
	
	@ApiModelProperty(value = "菜单功能权限信息")
	private List<NavPermissionsVo> navList;
	
	
	@ApiModelProperty(value = "一张图应用资源目录权限信息")
	private List<CataLogPermissionsVo> catalogList;
	
	
}
