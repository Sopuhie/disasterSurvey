package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "资源目录收藏新增参数")
public class CatalogCollectAddDto implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "收藏的目录ID，必填")
    private String catalogId;
    
    @ApiModelProperty(value = "收藏的目录名称，必填")
    private String catalogName;

    @ApiModelProperty(value = "专题ID，必填")
    private String themeId;
    
    @ApiModelProperty(value = "排序索引，非必填")
    private Integer sortIndex;
        
    @ApiModelProperty(value = "创建人或收藏人，必填")
    private String cjr ;
    

}
