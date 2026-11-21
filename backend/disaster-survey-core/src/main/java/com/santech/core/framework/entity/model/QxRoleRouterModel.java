package com.santech.core.framework.entity.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@Table(name = "T_QX_ROLEROUTER")
@ApiModel(description= "角色运维路由关系表")
public class QxRoleRouterModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -4484161019285842088L;


	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "ID,以RT打头")
    private String id;


    @Column(name = "ROLEID")
    @ApiModelProperty(value = "角色ID")
    private String roleId;


    @Column(name = "ROUTERID")
    @ApiModelProperty(value = "路由id")
    private String routerId;
}
