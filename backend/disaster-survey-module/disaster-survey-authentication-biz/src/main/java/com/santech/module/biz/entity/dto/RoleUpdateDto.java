package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "角色信息更新数据传输对象")
public class RoleUpdateDto  implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色ID,以R开头,必填")
    private String roleId;

    @ApiModelProperty(value = "上级角色ID,非必填")
    private String parentId;

    @ApiModelProperty(value = "角色名称,非必填")
    private String roleName;

    @ApiModelProperty(value = "显示名称,非必填")
    private String displayName;

    @ApiModelProperty(value = "角色描述,非必填")
    private String description;

    @ApiModelProperty(value = "排序,非必填")
    private Integer sindex;
    
    @ApiModelProperty(value = "更新人,必填")
    private String zhgxr ;
    
}
