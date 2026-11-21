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
@Table(name = "T_JC_ZD")
@ApiModel(description = "字典分类表")
public class JcDictionaryTypeModel implements Serializable{
	
	private static final long serialVersionUID = -9035919558267152948L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty(value = "字典ID,DIC+时间戳")
	private String id ;
	
    @ApiModelProperty(value = "字典类型编码")
    @Column(name = "ZDLXBM")
    private String zdlxbm;

    @ApiModelProperty(value = "字典类型名称")
    @Column(name = "ZDLXMC")
    private String zdlxmc;

    @ApiModelProperty(value = "字典类型排序")
    @Column(name = "ZDLXSX")
    private Integer zdlxsx;

    @ApiModelProperty(value = "是否启用或删除标识，0启用,1不启用或已删除")
    @Column(name = "SFQY")
    private String sfqy;
    
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
