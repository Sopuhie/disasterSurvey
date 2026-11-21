package com.santech.core.framework.entity.model;

import java.io.Serializable;
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
@Table(name = "T_JC_ACTION")
@ApiModel(description = "系统基础功能菜单表")
public class JcActionModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -7883654313536351776L;

	@PrimaryKey
    @Column(name = "ID")
	@ApiModelProperty(value = "ID，A打头")
    private String id;

    @Column(name = "NAME")
	@ApiModelProperty(value = "功能名称")
    private String name;

    @Column(name = "ALIAS")
	@ApiModelProperty(value = "功能别名")
    private String alias;
    
    @Column(name = "NODETYPE")
	@ApiModelProperty(value = "节点类型  folder，meun，action,leftmenu,outmodule")
    private String nodetype;

    @Column(name = "EXECUTETOOL")
	@ApiModelProperty(value = "执行函数")
    private String executetool;

    @Column(name = "ACTIONGROUP")
	@ApiModelProperty(value = "功能分组，当节点类型为action时，需对节点进行分组，如一张图应用基础工具组maptools")
    private String actiongroup;
    
    @Column(name = "IMAGE")
	@ApiModelProperty(value = "显示字体图标")
    private String image;

    @Column(name = "ROUTERPATH")
    @ApiModelProperty(value = "功能菜单对应页面的路由地址")
    private String routerpath;
  
    @Column(name = "SORTINDEX")
	@ApiModelProperty(value = "排序索引")
    private Integer sortindex;
    
    @Column(name = "VISIBLE")
   	@ApiModelProperty(value = "是否可见,1可见，0不可见")
    private String visible;
    
    @Column(name = "CONFIGURE")
	@ApiModelProperty(value = "配置项")
    private String configure;
    
    @Column(name = "PARENTID")
	@ApiModelProperty(value = "父节点id")
    private String parentId;
    
	@ApiModelProperty(value = "父节点（或所属上级）名称")
    private String parentName;
   
    @Column(name = "SYSTEM")
	@ApiModelProperty(value = "所属子系统")
    private String system;
    
    @Column(name = "DESCRIPTION")
	@ApiModelProperty(value = "说明")
    private String description;

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

    @ApiModelProperty(value = "子功能信息")
 	private List<JcActionModel> children;
}
