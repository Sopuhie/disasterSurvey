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
@Table(name = "T_ZY_CATALOG_COLLECT")
@ApiModel(description= "资源目录收藏功能表")
public class ZyCatalogCollectModel implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "ID,以CC打头")
    private String id;
    
    @Column(name = "CATALOGID")
    @ApiModelProperty(value = "收藏的目录ID")
    private String catalogId;
    
    @Column(name = "CATALOGNAME")
    @ApiModelProperty(value = "收藏的目录名称")
    private String catalogName;

    @Column(name = "THEMEID")
    @ApiModelProperty(value = "专题ID")
    private String themeId;
    
    @Column(name = "SORTINDEX")
    @ApiModelProperty(value = "排序索引")
    private Integer sortIndex;
        
    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人或收藏人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建或收藏时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;


}
