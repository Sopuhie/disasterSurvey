package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;
import java.util.Properties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "几何要素数据对象")
public class FeatureVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "要素属性信息")
    private Properties attributes;

    @ApiModelProperty(value = "要素空间几何信息")
    private  GeometryVo geometry;
    

}
