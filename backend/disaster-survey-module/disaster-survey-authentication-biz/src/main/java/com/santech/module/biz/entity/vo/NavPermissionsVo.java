package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: NavPermissionsModel
 * @Description: 角色菜单功能权限信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "角色菜单功能权限信息")
public class NavPermissionsVo {
	//检索字段
	@ApiModelProperty(value = "功能ID")
	private String id;
	
	@ApiModelProperty(value = "功能名称")
	private String name;
	
	@ApiModelProperty(value = "功能别名")
	private String title;
	
	@ApiModelProperty(value = "功能图标")
	private String icon;
	
	@ApiModelProperty(value = "功能页面路由地址")
	private String path;
	
	@ApiModelProperty(value = "父级功能id")
	private String parentId;
	
	@ApiModelProperty(value = "父级功能名称")
	private String type;
	
	@ApiModelProperty(value = "功能执行函数")
	private String executetool;
	
	@ApiModelProperty(value = "节点类型  folder目录，meun菜单，action按钮")
	private String nodetype;
	
	@ApiModelProperty(value = "功能分组，当节点类型为action时，此字段有值，一张图应用工具组maptools、成果审查功能组checktools")
	private String actiongroup;
	
	@ApiModelProperty(value = "功能排序")
	private Integer sortindex;
	
	@ApiModelProperty(value = "功能配置，目前暂只对action类型的节点且为审查功能组的功能进行配置的返回")
	private String configure;
	
	@ApiModelProperty(value = "功能名称")
	private List<NavPermissionsVo> childrens;
	
}
