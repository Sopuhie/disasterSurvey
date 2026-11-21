package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@Table(name = "T_ZY_CATALOG")
@ApiModel(description= "对外展示的资源目录表")
public class ZyCatalogModel implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2730441887860402424L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "ID,以CR打头")
    private String id;

    @Column(name = "PARENTID")
    @ApiModelProperty(value = "上级ID")
    private String parentId;

    @Column(name = "NAME")
    @ApiModelProperty(value = "名称")
    private String name;

    @Column(name = "TYPE")
    @ApiModelProperty(value = "节点类型")
    private String type;

    @Column(name = "THEMEID")
    @ApiModelProperty(value = "专题ID")
    private String themeId;
    
    @ApiModelProperty(value = "专题名称")
    private String themeName;

    @Column(name = "SORTINDEX")
    @ApiModelProperty(value = "排序")
    private Integer sortIndex;

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


}
