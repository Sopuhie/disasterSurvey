package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户密码更改参数")
public class UserPasswordChangeDto implements Serializable {

	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID,U打头")
    private	String	userId;

    @ApiModelProperty(value = "原密码")
    private	String	originPassword;

    @ApiModelProperty(value = "新密码")
    private	String	targetPassword;

    @ApiModelProperty(value = "当前登录人用户编码")
    private String loginUser ;

}
