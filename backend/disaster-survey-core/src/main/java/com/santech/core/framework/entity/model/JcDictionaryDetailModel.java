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
@Table(name = "T_JC_ZDX")
@ApiModel(description = "字典项明细表(T_JC_ZDX)")
public class JcDictionaryDetailModel implements Serializable{
	
	private static final long serialVersionUID = -7687785299843311087L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty(value = "字典ID,DIC+时间戳")
	private String id;
	
    @ApiModelProperty(value = "所属字典类型Id")
    @Column(name = "FLID")
    private String flid;

    @ApiModelProperty(value = "字典类型编码")
    @Column(name = "ZDLXBM")
    private String zdlxbm;

    @ApiModelProperty(value = "字典编码")
    @Column(name = "ZDBM")
    private String zdbm;

    @ApiModelProperty(value = "字典值或名称")
    @Column(name = "ZDZ")
    private String zdz;
    
    @ApiModelProperty(value = "字典排序")
    @Column(name = "ZDSX")
    private Integer zdsx;
    
    @ApiModelProperty(value = "备注")
    @Column(name = "BZ")
    private String bz;

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
