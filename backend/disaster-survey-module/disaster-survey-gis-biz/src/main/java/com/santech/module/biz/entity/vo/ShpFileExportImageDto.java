package com.santech.module.biz.entity.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "shp导出为图片接口参数信息")
public class ShpFileExportImageDto implements Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "图层或shp名称，必传")
    private String layerName;
	
	@ApiModelProperty(value ="shp文件路径，必传")
	private  String  filePath;
	
	@ApiModelProperty(value ="导出图片类型，如png，jpg，tiff，必传")
	private  String  imageType;
	
	@ApiModelProperty(value ="导出图片默认样式编码，固定的随机值，若图层名找不到对应的配色样式配置，则通过此值进行随机配色样式导出图片，必传")
	private String symbol;
	
	@ApiModelProperty(value ="导出图片宽度，不传默认1024")
	private Integer width;
	@ApiModelProperty(value ="导出图片高度，不传根据默认宽度和shp长宽比获取")
	private Integer height;
	@ApiModelProperty(value ="shp左下角和右上角的坐标串，如：15.75550452737981,36.40795749749838,117.60052976349418,38.010623638947465，不传根据四至获取")
	private String bbox;
	@ApiModelProperty(value ="类似格式：EPSG:4326,或WKT格式坐标系字符串，非必传，不传默认EPSG:4326")
	private String bboxSr;

}