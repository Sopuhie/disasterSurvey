package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户信息更新数据传输对象")
public class UserUpdateDto implements Serializable {

	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID,U打头,必填")
    private	String	userId;

    @ApiModelProperty(value = "用户名,非必填")
    private	String	username;

    @ApiModelProperty(value = "用户昵称,非必填")
    private	String	name;

    @ApiModelProperty(value = "性别,非必填")
    private	Integer	gender;
    
    @ApiModelProperty(value = "状态，0在职，1离职或删除或无效,非必填")
    private	Integer	status;

    @ApiModelProperty(value = "邮箱,非必填")
    private	String	email;

    @ApiModelProperty(value = "电话,非必填")
    private	String	tel;
    
    @ApiModelProperty(value = "用户头像,非必填")
    private	String	photo;

    @ApiModelProperty(value = "所属行政区编码,非必填")
    private	String	districtcode;

    @ApiModelProperty(value = "用户其他描述,非必填")
    private	String	description;
    
    @ApiModelProperty(value = "更新人,必填")
    private String zhgxr ;
    
    @ApiModelProperty(value = "关联部门ID,非必填")    
    private	String	departmentId;
    

}
