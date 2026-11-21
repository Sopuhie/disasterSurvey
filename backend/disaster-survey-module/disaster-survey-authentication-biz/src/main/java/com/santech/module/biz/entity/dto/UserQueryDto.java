package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: UserQueryDto
 * @Description: 用户查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "用户查询参数信息")
public class UserQueryDto {
	//检索字段
	@ApiModelProperty(value = "用户id")
	private String eq_userid;
	@ApiModelProperty(value = "用户名,全匹配")
	private String eq_username;
	@ApiModelProperty(value = "用户名,模糊匹配")
	private String like_username;
	@ApiModelProperty(value = "昵称或真实名称")
	private String eq_name;
	@ApiModelProperty(value = "昵称或真实名称,模糊匹配")
	private String like_name;
	@ApiModelProperty(value = "性别")
	private Integer eq_gender;
	@ApiModelProperty(value = "用户状态,0有效，1无效")
	private Integer eq_status;
	@ApiModelProperty(value = "电话")
	private String eq_tel;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
