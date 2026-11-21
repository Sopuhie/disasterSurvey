package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@Table(name = "T_QX_USER")
@ApiModel(description = "用户信息表")
public class QxUserSimpleModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
    @Column(name = "USERID")
    @ApiModelProperty(value = "用户ID,U打头")
    private	String	userId;

    @Column(name = "USERNAME")
    @ApiModelProperty(value = "用户名")
    private	String	username;

    @Column(name= "NAME")
    @ApiModelProperty(value = "用户昵称")
    private	String	name;

    @Column(name= "GENDER")
    @ApiModelProperty(value = "性别")
    private	Integer	gender;

    @Column(name= "EMAIL")
    @ApiModelProperty(value = "邮箱")
    private	String	email;

    @Column(name= "TEL")
    @ApiModelProperty(value = "电话")
    private	String	tel;
    
    @Column(name= "DISTRICTCODE")
    @ApiModelProperty(value = "所属行政区编码")
    private	String	districtcode;

    @Column(name= "STATUS")
    @ApiModelProperty(value = "状态，0在职，1离职或删除或无效")
    private	Integer	status;

    @Column(name= "FIRST_LOGIN_FLAG")
    @ApiModelProperty(value = "是否第一次登陆，0是，1不是")
    private	Integer	firstLoginFlag;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;
    
    @ApiModelProperty(value = "用户具有的角色信息")
    private List<QxRoleModel> roles;

}
