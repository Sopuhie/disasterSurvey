package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "专题信息更新数据传输对象")
public class ThemeUpdateDto implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "专题ID,以TR打头,必填")
    private String id;

    @ApiModelProperty(value = "专题名称,非必填")
    private String themeName;

    @ApiModelProperty(value = "排序索引，非必填")
    private Integer sortIndex;

    @ApiModelProperty(value = "上级ID，非必填")
    private String parentId;

    @ApiModelProperty(value = "节点类型，folder目录 theme专题 complex复合专题,非必填")
    private String type;

    @ApiModelProperty(value = "是否启用时间轴，0-不启用，1-启用，预留，非必填")
    private String timeLine;
    
    @ApiModelProperty(value = "更新人,必填")
    private String zhgxr ;

}
