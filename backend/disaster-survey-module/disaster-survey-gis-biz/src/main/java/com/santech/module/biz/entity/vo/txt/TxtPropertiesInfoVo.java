package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "数据服务发布结果数据对象")
public class TxtPropertiesInfoVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "格式版本号")
    private String version;
    
    @ApiModelProperty(value = "数据产生单位")
    private String unit;
    
    @ApiModelProperty(value = "数据产生日期")
    private String date;

    @ApiModelProperty(value = "坐标系")
    private String geoCoordSystem;
    
    @ApiModelProperty(value = "几度分带")
    private int degrees;
    
    @ApiModelProperty(value = "投影类型")
    private String projection;
    
    @ApiModelProperty(value = "计量单位")
    private String linearUnit;
    
    @ApiModelProperty(value = "带号")
    private int degreeNumber;
    
    @ApiModelProperty(value = "精度")
    private double Precision;
    
    @ApiModelProperty(value = "转换参数")
    private String TransformFactor;
    
    
    
    

}
