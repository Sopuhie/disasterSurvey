package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ResourceStatisticsParamsModel
 * @Description: 资源统计参数信息，适用统计字段有对应字典类型的情况，否则统计结果为空
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "资源统计参数信息")
public class ResourceStatisticsParamsDto {
	
	@ApiModelProperty(value = "资源类型，service-数据服务资源，file-文件资源，api-接口资源")
	private String resourceType;
	
	@ApiModelProperty(value = "统计字段名称-真实数据库表字段")
	private String groupColumn;
	
	@ApiModelProperty(value = "字典类型，统计字段对应的字典类型")
	private String dictType;
	
	@ApiModelProperty(value = "功能名称，针对数据服务资源按类型统计时有值")
	private String actionName;
	
	@ApiModelProperty(value = "管理员标识,0非管理员，1管理员")
	private String adminFlag;
	
}
