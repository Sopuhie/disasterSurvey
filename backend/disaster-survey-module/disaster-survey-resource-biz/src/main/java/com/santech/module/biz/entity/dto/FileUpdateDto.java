package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "普通文件类资源更新数据传输对象")
public class FileUpdateDto implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "文件资源ID，以FR打头，必填")
    private String id;

    @ApiModelProperty(value = "文件资源名称，非必填")
    private String name;

    @ApiModelProperty(value = "文件资源类型或分类，非必填")
    private String type;
    
    @ApiModelProperty(value = "文件等级，如1机密级、2秘密级等,具体见字典，非必填")
    private String level;

    @ApiModelProperty(value = "文件资源状态，0开放，1不开放，非必填")
    private String status;
    
    @ApiModelProperty(value = "文件资源描述，非必填")
    private String description;

    @ApiModelProperty(value = "所属项目，非必填，预留")
    private String project;
    
    @ApiModelProperty(value = "更新人，必填")
    private String zhgxr ;
    
/*    @ApiModelProperty(value = "附件资源的具体附件存储信息，必填")    
    private List<ZyFileDetailModel> details;*/


}
