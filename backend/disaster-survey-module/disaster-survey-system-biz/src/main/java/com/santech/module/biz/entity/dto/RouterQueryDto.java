package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: RouterQueryDto
 * @Description: 运维路由菜单分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "运维路由菜单分页查询参数信息")
public class RouterQueryDto {
	//检索字段
	@ApiModelProperty(value = "路由名称名称,模糊匹配")
	private String like_name;
	@ApiModelProperty(value = "标题,模糊匹配")
	private String like_title;
	@ApiModelProperty(value = "是否隐藏(0为false,1为true)")
	private Integer eq_hidden;
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
