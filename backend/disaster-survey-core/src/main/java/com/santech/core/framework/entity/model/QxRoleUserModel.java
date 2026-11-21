package com.santech.core.framework.entity.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;



@Data
@Table(name = "T_QX_ROLEUSER")
@ApiModel(description= "用户角色关联信息表")
public class QxRoleUserModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -1311192194429201665L;


	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "关联ID,RU打头")
    private String id;


    @Column(name = "ROLEID")
    @ApiModelProperty(value = "角色id")
    private String roleId;


    @Column(name = "USERID")
    @ApiModelProperty(value = "用户id")
    private String userId;
}
