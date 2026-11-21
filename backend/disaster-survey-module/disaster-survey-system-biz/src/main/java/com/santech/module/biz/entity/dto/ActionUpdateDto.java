package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "系统基础功能更新数据传输对象")
public class ActionUpdateDto  implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -5996862378581085897L;

	@ApiModelProperty(value = "ID，A打头，必填")
    private String id;
	
	@ApiModelProperty(value = "功能名称，非必填")
    private String name;

	@ApiModelProperty(value = "功能别名，非必填")
    private String alias;
    
	@ApiModelProperty(value = "节点类型  ，非必填，folder，meun，action,leftmenu,outmodule")
    private String nodetype;

	@ApiModelProperty(value = "执行函数，非必填")
    private String executetool;

	@ApiModelProperty(value = "功能分组，非必填，当节点类型为action时，需对节点进行分组，如一张图应用基础工具组maptools")
    private String actiongroup;
    
	@ApiModelProperty(value = "显示字体图标，非必填")
    private String image;

    @ApiModelProperty(value = "功能菜单对应页面的路由地址，非必填")
    private String routerpath;
  
	@ApiModelProperty(value = "排序索引，非必填")
    private Integer sortindex;
    
   	@ApiModelProperty(value = "是否可见,1可见，0不可见，非必填")
    private String visible;
    
	@ApiModelProperty(value = "配置项，非必填")
    private String configure;
    
	@ApiModelProperty(value = "父节点id，非必填")
    private String parentId;
    
	@ApiModelProperty(value = "所属子系统，非必填")
    private String system;
    
	@ApiModelProperty(value = "说明，非必填")
    private String description;

	@ApiModelProperty(value = "更新人，必填")
	private String zhgxr ;
    
}
