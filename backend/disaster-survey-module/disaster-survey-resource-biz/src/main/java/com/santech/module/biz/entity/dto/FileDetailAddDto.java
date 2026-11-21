package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "文件资源明细新增数据传输对象")
public class FileDetailAddDto implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "文件名称,必填")
    private String fileName;
    
    @ApiModelProperty(value = "文件存储相对路径,必填")
    private String filePath;

    @ApiModelProperty(value = "文件类型（扩展名）,必填")
    private String fileType;
    
    @ApiModelProperty(value = "文件大小,必填")
    private Long fileSize;
    

}
