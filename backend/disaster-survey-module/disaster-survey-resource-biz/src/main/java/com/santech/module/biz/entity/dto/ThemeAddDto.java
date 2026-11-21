package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "专题信息新增数据传输对象")
public class ThemeAddDto implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "专题名称,必填")
    private String themeName;

    @ApiModelProperty(value = "排序索引，非必填，不传默认库中对应记录排序的最大值")
    private Integer sortIndex;

    @ApiModelProperty(value = "上级ID，非必填，不传默认为根节点")
    private String parentId;

    @ApiModelProperty(value = "节点类型，folder目录 theme专题 complex复合专题,必填")
    private String type;

    @ApiModelProperty(value = "是否启用时间轴，0-不启用，1-启用，预留，非必填")
    private String  timeLine;
    
    @ApiModelProperty(value = "创建人,必填")
    private String cjr ;
    

}
