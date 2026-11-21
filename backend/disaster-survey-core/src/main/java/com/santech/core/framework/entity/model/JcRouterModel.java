package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@Table(name = "T_JC_ROUTER")
@ApiModel(description = "运维功能菜单路由表")
public class JcRouterModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 6846371555288203752L;

	@PrimaryKey
    @Column(name = "ROUTERID")
	@ApiModelProperty(value = "ID，RT打头")
    private String routerId;

    @Column(name = "TITLE")
	@ApiModelProperty(value = "标题")
    private String title;

    @Column(name = "ICON")
	@ApiModelProperty(value = "图标")
    private String icon;

    @Column(name = "path")
	@ApiModelProperty(value = "路径")
    private String path;

    @Column(name = "NAME")
	@ApiModelProperty(value = "路由名称")
    private String name;
    
    @Column(name = "NOCACHE")
	@ApiModelProperty(value = "是否缓存(0为false缓存，1为true不缓存)")
    private BigDecimal noCache;

    @Column(name = "COMPONENT")
	@ApiModelProperty(value = "组件")
    private String component;

    @Column(name = "REDIRECT")
	@ApiModelProperty(value = "重定向")
    private String redirect;

    @Column(name = "PARENTID")
	@ApiModelProperty(value = "父id")
    private String parentId;

    @Column(name = "HIDDEN")
	@ApiModelProperty(value = "是否隐藏(0为false,1为true)，默认false")
    private Integer hidden;

    @Column(name = "ALWAYSSHOW")
	@ApiModelProperty(value = "是否总是显示(0为false,1为true), 默认false")
    private Integer alwaysShow;

    @Column(name = "SORTINDEX")
	@ApiModelProperty(value = "排序")
    private Integer sortIndex;

    @Column(name = "IFRAME")
	@ApiModelProperty(value = "嵌入页面地址")
    private String iframe;
    
    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;
    
    @Column(name = "ZHGXR")
    @ApiModelProperty(value = "更新人")
    private String zhgxr ;
    
    @Column(name = "ZHGXSJ")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date zhgxsj ;
    
    @ApiModelProperty(value = "子路由信息")
	private List<JcRouterModel> children;
    
}
