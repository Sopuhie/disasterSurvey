package com.santech.module.biz.entity.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "为文件资源新添加文件的参数传输对象")
public class FileDetailsUpdateDto implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "文件资源ID，以FR打头，必填")
    private String id;
    
    @ApiModelProperty(value = "上传人，必填")
    private String cjr ;
    
    @ApiModelProperty(value = "新上传的附件资源的具体附件存储信息，必填")    
    private List<FileDetailAddDto> details;


}
