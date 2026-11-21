package com.santech.core.framework.entity.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;


@Data
@Table(name = "T_QX_ROLECATALOG")
@ApiModel(description= "角色专题目录关联表")
public class QxRoleCatalogModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6620507732751104943L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "ID")
    private String id;

    @Column(name = "ROLEID")
    @ApiModelProperty(value = "角色ID")
    private String roleId;

    @Column(name = "CATALOGID")
    @ApiModelProperty(value = "目录ID")
    private String catalogId;

}
