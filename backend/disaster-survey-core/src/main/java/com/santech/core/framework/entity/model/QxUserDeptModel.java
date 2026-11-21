package com.santech.core.framework.entity.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;


@Data
@Table(name = "T_QX_USERDEPARTMENT")
@ApiModel(description = "组织机构用户关联信息")
public class QxUserDeptModel  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty(value = "标识码")
	private String id;

	@Column(name = "USERID")
	@ApiModelProperty(value = "用户ID")
	private String userId;

	@Column(name = "DEPARTMENTID")
	@ApiModelProperty(value = "部门ID")
	private String departmentId;
	
	@Column(name = "USER_ORDER")
	@ApiModelProperty(value = "用户显示顺序")
	private Integer userOrder;
	
}
