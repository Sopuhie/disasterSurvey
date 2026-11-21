package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "角色信息新增数据传输对象")
public class RoleAddDto  implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "上级角色ID,必填")
    private String parentId;

    @ApiModelProperty(value = "角色名称,必填")
    private String roleName;

    @ApiModelProperty(value = "显示名称,非必填")
    private String displayName;

    @ApiModelProperty(value = "角色描述,非必填")
    private String description;

    @ApiModelProperty(value = "排序,非必填，不传默认库中对应记录排序的最大值")
    private Integer sindex;

    @ApiModelProperty(value = "创建人,必填")
    private String cjr ;
    
}
