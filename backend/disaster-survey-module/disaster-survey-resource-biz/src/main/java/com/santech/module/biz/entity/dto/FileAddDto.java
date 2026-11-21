package com.santech.module.biz.entity.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "普通文件类资源新增数据传输对象")
public class FileAddDto implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件资源名称，必填")
    private String name;

    @ApiModelProperty(value = "文件资源类型或分类，必填")
    private String type;
    
    @ApiModelProperty(value = "文件等级，如1机密级、2秘密级等,具体见字典，必填")
    private String level;

    @ApiModelProperty(value = "文件资源状态，0开放，1不开放，非必填，默认0")
    private String status;
    
    @ApiModelProperty(value = "文件资源描述，非必填")
    private String description;

    @ApiModelProperty(value = "所属项目，非必填，预留")
    private String project;
    
    @ApiModelProperty(value = "创建人，必填")
    private String cjr ;
    
    @ApiModelProperty(value = "附件资源的具体附件存储信息，必填")    
    private List<FileDetailAddDto> details;


}
