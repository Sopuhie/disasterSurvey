package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "对外展示的资源目录更新数据传输对象")
public class CatalogUpdateDto implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "ID,以CR打头,必填")
    private String id;

    @ApiModelProperty(value = "上级ID,非必填")
    private String parentId;

    @ApiModelProperty(value = "名称,非必填")
    private String name;

    @ApiModelProperty(value = "节点类型，folder目录 theme专题,非必填")
    private String type;

    @ApiModelProperty(value = "专题ID,非必填")
    private String themeId;
    
    @ApiModelProperty(value = "排序，非必填")
    private Integer sortIndex;

    @ApiModelProperty(value = "更新人,必填")
    private String zhgxr ;

}
