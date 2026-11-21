package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "数据服务样式文件资源新增数据传输对象")
public class ServiceStyleAddDto implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "样式资源名称，必填")
    private String name;

    @ApiModelProperty(value = "样式资源类型或分类，如按具体发布平台分类，必填")
    private String type;
    
    @ApiModelProperty(value = "样式资源状态，0可用，1不可用，非必填，不传默认0")
    private String status;
    
    @ApiModelProperty(value = "样式资源描述，非必填")
    private String description;

    @ApiModelProperty(value = "所属项目，非必填，预留")
    private String project;
    
    @ApiModelProperty(value = "创建人，必填")
    private String cjr ;
    
}
