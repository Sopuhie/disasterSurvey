package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "shp文件内容查询参数信息")
public class ShpFileContentQueryDto implements Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "单个上传的成果文件真实名称，必填")
    private String fileName;
	
	@ApiModelProperty(value ="文件路径,具体.shp的相对文件路径也就图层路径，必填")
	private String  filePath;
	
	@ApiModelProperty(value ="FID属性或shp属性记录ID，根据id查询时必填")	
	private String fid;
	
	@ApiModelProperty(value ="shp文件编码格式，非必填，不传默认UTF-8")
	private String encode;
	
	

}