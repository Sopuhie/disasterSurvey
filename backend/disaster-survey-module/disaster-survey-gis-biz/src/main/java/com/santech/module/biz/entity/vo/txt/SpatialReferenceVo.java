package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "空间参考数据对象")
public class SpatialReferenceVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "空间参考ID")
    private int wkid;

    @ApiModelProperty(value = "伪空间参考ID")
    private  int latestWkid;
    

}
