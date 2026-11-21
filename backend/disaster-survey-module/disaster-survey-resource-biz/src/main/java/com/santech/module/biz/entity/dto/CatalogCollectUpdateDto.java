package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "资源目录收藏更新参数-预留")
public class CatalogCollectUpdateDto implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID,必填")
    private String id;
    
    @ApiModelProperty(value = "排序索引，非必填")
    private Integer sortIndex;

    @ApiModelProperty(value = "当前登录人用户名,必填")
    private String loginUser;
}
