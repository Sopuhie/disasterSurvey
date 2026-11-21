package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: UserLoginDto
 * @Description: 用户登录认证参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "用户登录认证参数信息")
public class UserLoginDto {
	//检索字段
	@ApiModelProperty(value = "用户名")
	private String username;
	@ApiModelProperty(value = "密码")
	private String password;
	@ApiModelProperty(value = "单点登录的认证token")
	private String token;
	
}
