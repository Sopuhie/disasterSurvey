package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Txt地块坐标数据对象")
public class TxtSegmentVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "地块信息")
    private String blockInfo;
    
    @ApiModelProperty(value = "坐标串")
    private List<TxtCoordPointVo> Points;
    
}
