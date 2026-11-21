package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Txt坐标串对象")
public class TxtCoordPointVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "点号")
    private String pointNumber;
    
    
    @ApiModelProperty(value = "圈号")
    private int RingNumber;
    
    
    @ApiModelProperty(value = "X 坐标")
    private double x;
    
    @ApiModelProperty(value = "Y 坐标")
    private double y;
    
}
