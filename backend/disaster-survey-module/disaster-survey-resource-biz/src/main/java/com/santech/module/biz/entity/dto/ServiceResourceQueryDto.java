package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ServiceResourceQueryDto
 * @Description: 数据服务资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据服务资源分页查询参数信息")
public class ServiceResourceQueryDto {
	//检索字段
	@ApiModelProperty(value = "服务名称,模糊匹配")
	private String like_serviceName;
	@ApiModelProperty(value = "服务发布平台类型,全匹配")
	private String eq_servicePlatform;
	@ApiModelProperty(value = "服务类型,全匹配")
	private String eq_serviceType;
	@ApiModelProperty(value = "发布机构,模糊匹配")
	private String like_unit ;
	@ApiModelProperty(value = "所属项目,全匹配")
	private String eq_project ;
	
	 //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
	
}
