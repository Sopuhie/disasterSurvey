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
@Table(name = "T_MAPPING_TEMPLATE")
@ApiModel(description = "专题制图模板表")
public class MappingTemplateModel  implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -1385273315391042157L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "专题制图模板ID，以MT打头")
    private String id;

    @Column(name = "NAME")
    @ApiModelProperty(value = "专题制图模板名称")
    private String name;
    
    @Column(name = "CONTENT")
    @ApiModelProperty(value = "专题制图模板内容")
    private String content;
    
    @Column(name = "ISSHARE")
    @ApiModelProperty(value = "模板分享类型，0不共享，1共享")
    private String isShare;

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
