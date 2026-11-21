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
@Table(name = "T_QX_DEPARTMENT")
@ApiModel(description = "机构部门信息基础表")
public class QxDepartmentModel  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty(value = "ID,DP打头")
	private String id;

	@Column(name = "PID")
	@ApiModelProperty(value = "父级机构ID")
	private String pid;

	@Column(name = "ORGNAME")
	@ApiModelProperty(value = "机构或部门名称")
	private String orgName;

	@Column(name = "ORGCODE")
	@ApiModelProperty(value = "机构或部门代码")
	private String orgCode;

	@Column(name = "ORGTYPE")
	@ApiModelProperty(value = "机构部门标识：jg 机构；bm 部门")
	private String orgType;
	

	@Column(name = "ORGDESC")
	@ApiModelProperty(value = "机构部门描述")
	private String orgDesc;
	

	@Column(name = "STATUS")
	@ApiModelProperty(value = "机构状态：running 正常；cancel 注销")
	private String status;
	
    @Column(name = "SORTINDEX")
	@ApiModelProperty(value = "排序索引")
    private Integer sortIndex;

	@Column(name = "XZQDM")
	@ApiModelProperty(value = "行政区代码")
	private String xzqdm;
	

	@Column(name = "XZQMC")
	@ApiModelProperty(value = "行政区名称")
	private String xzqmc;
	

	@Column(name = "REMARK")
	@ApiModelProperty(value = "备注")
	private String remark;
	

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
    
    @ApiModelProperty(value = "子部门信息")
	private List<QxDepartmentModel> children;
}
