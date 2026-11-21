package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "对外展示的资源目录新增数据传输对象")
public class CatalogAddDto implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2730441887860402424L;

    @ApiModelProperty(value = "上级ID,非必填，不传默认为-1根节点")
    private String parentId;

    @ApiModelProperty(value = "名称,必填")
    private String name;

    @ApiModelProperty(value = "节点类型，folder目录 theme专题,必填")
    private String type;

    @ApiModelProperty(value = "专题ID,节点类型为theme时必填")
    private String themeId;
    
    @ApiModelProperty(value = "排序，非必填，不传默认库中对应记录排序的最大值")
    private Integer sortIndex;

    @ApiModelProperty(value = "创建人,必填")
    private String cjr ;
    
}
