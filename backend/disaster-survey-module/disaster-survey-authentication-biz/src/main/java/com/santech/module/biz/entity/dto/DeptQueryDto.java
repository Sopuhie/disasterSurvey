package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: DeptQueryDto
 * @Description: 部门查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "部门查询参数信息")
public class DeptQueryDto {
	//检索字段
	@ApiModelProperty(value = "部门id")
	private String eq_id;
	@ApiModelProperty(value = "部门或机构名称")
	private String like_orgname;
	@ApiModelProperty(value = "部门或机构代码")
	private String eq_orgcode;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
}
