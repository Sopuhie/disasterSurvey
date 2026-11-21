package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@Table(name = "T_QX_ROLE")
@ApiModel(description= "角色信息表")
public class QxRoleModel  implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
    @Column(name = "ROLEID")
    @ApiModelProperty(value = "角色ID,以R开头")
    private String roleId;

    @Column(name = "PARENTID")
    @ApiModelProperty(value = "上级角色ID")
    private String parentId;

    @Column(name = "ROLENAME")
    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @Column(name = "DISPLAYNAME")
    @ApiModelProperty(value = "显示名称")
    private String displayName;

    @Column(name = "DESCRIPTION")
    @ApiModelProperty(value = "角色描述")
    private String description;

    @Column(name = "SINDEX")
    @ApiModelProperty(value = "排序")
    private Integer sindex;

    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;
    
    @Column(name = "ZHGXR")
    @ApiModelProperty(value = "更新人")
    private String zhgxr ;
    
    @Column(name = "ZHGXSJ")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date zhgxsj ;
}
