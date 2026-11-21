package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "空间几何数据对象")
public class GeometryVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "范围数据")
    private List<List<List<Double>>> rings;

    @ApiModelProperty(value = "空间参考信息")
    private  SpatialReferenceVo spatialReference;
    

}
