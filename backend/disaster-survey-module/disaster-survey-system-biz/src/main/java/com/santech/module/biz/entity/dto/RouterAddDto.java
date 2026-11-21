package com.santech.module.biz.entity.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "运维功能菜单路由新增数据传输对象")
public class RouterAddDto  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3762654068648841121L;

	@ApiModelProperty(value = "标题，必填")
    private String title;

	@ApiModelProperty(value = "图标，非必填")
    private String icon;

	@ApiModelProperty(value = "路径，必填")
    private String path;

	@ApiModelProperty(value = "路由名称，必填")
    private String name;
    
	@ApiModelProperty(value = "是否缓存(0为false缓存，1为true不缓存)，非必填")
    private BigDecimal noCache;

	@ApiModelProperty(value = "组件，非必填，必填")
    private String component;

	@ApiModelProperty(value = "重定向，非必填")
    private String redirect;

	@ApiModelProperty(value = "父id，非必填，不传默认为-1根节点")
    private String parentId;

	@ApiModelProperty(value = "是否隐藏(0为false,1为true)，必填，默认0")
    private Integer hidden;

	@ApiModelProperty(value = "是否总是显示(0为false,1为true)，非必填, 默认0")
    private Integer alwaysShow;

	@ApiModelProperty(value = "排序，非必填，不传默认库中对应记录排序的最大值")
    private Integer sortIndex;

	@ApiModelProperty(value = "嵌入页面地址，非必填")
    private String iframe;
    
    @ApiModelProperty(value = "创建人，必填")
    private String cjr ;
    
}
