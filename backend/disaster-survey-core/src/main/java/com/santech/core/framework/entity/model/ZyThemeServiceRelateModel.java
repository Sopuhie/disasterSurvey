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
@Table(name = "T_ZY_THEMESERVICERELATE")
@ApiModel(description= "专题资源关联表")
public class ZyThemeServiceRelateModel implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -361723231073069865L;
	
    @PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "ID,以TSR打头")
    private String id;
    
    @Column(name = "THEMEID")
    @ApiModelProperty(value = "专题ID")
    private String themeid;

    @Column(name = "SERVICEID")
    @ApiModelProperty(value = "服务ID")
    private String serviceid;
    
   /* @Column(name = "LAYERID")
    @ApiModelProperty(value = "图层ID")
    private String layerid;*/
   
    @Column(name = "SORTINDEX")
    @ApiModelProperty(value = "排序索引")
    private Integer sortindex;
    
    @Column(name = "SERVICENAME")
    @ApiModelProperty(value = "服务名称")
    private String servicename;
    
   /* @Column(name = "LAYERNAME")
    @ApiModelProperty(value = "图层名称")
    private String layername;*/
    
    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;



}
