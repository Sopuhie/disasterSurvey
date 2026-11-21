package com.santech.module.biz.entity.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "运维功能菜单路由更新数据传输对象")
public class RouterUpdateDto  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4268280248940070502L;

	@ApiModelProperty(value = "ID，RT打头，必填")
    private String routerId;

	@ApiModelProperty(value = "标题，非必填")
    private String title;

	@ApiModelProperty(value = "图标，非必填")
    private String icon;

	@ApiModelProperty(value = "路径，非必填")
    private String path;

	@ApiModelProperty(value = "路由名称，非必填")
    private String name;
    
	@ApiModelProperty(value = "是否缓存(0为false缓存，1为true不缓存)，非必填")
    private BigDecimal noCache;

	@ApiModelProperty(value = "组件，非必填")
    private String component;

	@ApiModelProperty(value = "重定向，非必填")
    private String redirect;

	@ApiModelProperty(value = "父id，非必填")
    private String parentId;

	@ApiModelProperty(value = "是否隐藏(0为false,1为true)，默认0，非必填")
    private Integer hidden;

	@ApiModelProperty(value = "是否总是显示(0为false,1为true), 默认0，非必填")
    private Integer alwaysShow;

	@ApiModelProperty(value = "排序，非必填")
    private Integer sortIndex;

	@ApiModelProperty(value = "嵌入页面地址，非必填")
    private String iframe;
    
    @ApiModelProperty(value = "更新人，必填")
    private String zhgxr ;
    
    
}
