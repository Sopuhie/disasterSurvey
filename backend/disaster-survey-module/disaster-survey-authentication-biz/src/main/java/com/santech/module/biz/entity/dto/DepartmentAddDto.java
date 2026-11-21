package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "机构部门信息新增数据传输对象")
public class DepartmentAddDto  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "父级机构ID,非必填，不传默认-1根部门")
	private String pid;

	@ApiModelProperty(value = "机构或部门名称,必填")
	private String orgName;

	@ApiModelProperty(value = "机构或部门代码,必填")
	private String orgCode;

	@ApiModelProperty(value = "机构部门标识：jg 机构；bm 部门，必填")
	private String orgType;

	@ApiModelProperty(value = "机构部门描述，非必填")
	private String orgDesc;

	@ApiModelProperty(value = "机构状态：running 正常；cancel 注销，非必填，不填默认running")
	private String status;
	
	@ApiModelProperty(value = "排序索引，非必填，不传默认库中对应记录排序的最大值")
    private Integer sortIndex;

	@ApiModelProperty(value = "行政区代码，非必填")
	private String xzqdm;

	@ApiModelProperty(value = "行政区名称，非必填")
	private String xzqmc;

	@ApiModelProperty(value = "备注，非必填")
	private String remark;

    @ApiModelProperty(value = "创建人，必填")
    private String cjr ;
    
    
}
