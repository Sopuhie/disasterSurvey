package com.santech.core.framework.entity.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;


@Data
@Table(name = "T_QX_ROLEACTION")
@ApiModel(description= "角色功能权限表")
public class QxRoleActionModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4399563697775169723L;


	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "ID")
    private String id;


    @Column(name = "ROLEID")
    @ApiModelProperty(value = "角色ID")
    private String roleId;

    @Column(name = "ACTIONID")
    @ApiModelProperty(value = "功能ID")
    private String actionId;
}
