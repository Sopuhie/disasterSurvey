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
@Table(name = "T_ZY_STYLERESOURCE")
@ApiModel(description= "数据服务样式文件资源表")
public class ZyServiceStyleModel implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 5255675828831682649L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "样式资源ID，以YS打头")
    private String id;

    @Column(name = "NAME")
    @ApiModelProperty(value = "样式资源名称")
    private String name;

    @Column(name = "TYPE")
    @ApiModelProperty(value = "样式资源类型或分类，如按具体发布平台分类")
    private String type;
    
    @Column(name = "CONTENT")
    @ApiModelProperty(value = "样式文件内容，为xml值")
    private String content;

    @Column(name = "STATUS")
    @ApiModelProperty(value = "样式资源状态，0可用，1不可用")
    private String status;
    
    @Column(name = "URL")
    @ApiModelProperty(value = "样式资源xml获取地址")
    private String url;
    
    @Column(name = "DESCRIPTION")
    @ApiModelProperty(value = "样式资源描述")
    private String description;

    @Column(name = "PROJECT")
    @ApiModelProperty(value = "所属项目")
    private String project;
    
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
