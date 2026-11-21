package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "txt内容信息对象")
public class TxtContentInfoVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "属性描述信息")
    private TxtPropertiesInfoVo properties;
    
    @ApiModelProperty(value = "地块坐标信息")
    private List<TxtSegmentVo> segs;
    

}
