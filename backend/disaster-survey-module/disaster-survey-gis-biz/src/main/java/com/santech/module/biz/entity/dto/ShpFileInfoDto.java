package com.santech.module.biz.entity.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;

@Data
@ApiModel(description= "专题文件信息")
public class ShpFileInfoDto implements Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "单个上传的成果文件真实名称")
    private String fileName;
	
	@ApiModelProperty(value ="文件类型，1文件夹，2文件")
	private  String  fileType;
	
	@ApiModelProperty(value ="文件扩展名，图层为layer")
	private  String  fileExt;
	
	@ApiModelProperty(value ="文件路径")
	private  String  filePath;
	
	@ApiModelProperty(value ="文件大小")
	private  String  fileSize;
	
	//如下发布动态服务的时候会产生如下字段
	@ApiModelProperty(value = "子文件信息，用于存储矢量数据shp的图层信息")
	private List<ShpFileInfoDto> layers;
	

}