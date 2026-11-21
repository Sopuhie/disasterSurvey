package com.santech.core.framework.entity.view;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 上传后的文件信息model
 **/
@Data
@ApiModel(description= "上传后的文件信息")
public class UploadFileVo {
	
	@ApiModelProperty(value = "单个上传的成果文件真实名称")
    private String fileName;
	
	@ApiModelProperty(value = "文件格式类型（扩展名）")
    private String fileType;
	
	@ApiModelProperty(value = "文件存放的相对路径")
    private String filePath;
	
	@ApiModelProperty(value = "文件大小")
    private String fileSize;
	
}
