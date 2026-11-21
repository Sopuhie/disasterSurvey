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
@Table(name = "T_ZY_THEME")
@ApiModel(description= "专题信息表")
public class ZyThemeModel implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -539115305699651932L;

    @PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "专题ID,以TR打头")
    private String id;

    @Column(name = "THEMENAME")
    @ApiModelProperty(value = "专题名称")
    private String themeName;

    @Column(name = "SORTINDEX")
    @ApiModelProperty(value = "排序索引")
    private Integer sortIndex;

    @Column(name = "PARENTID")
    @ApiModelProperty(value = "上级ID")
    private String parentId;

    @Column(name = "TYPE")
    @ApiModelProperty(value = "节点类型，folder目录 theme专题 complex复合专题")
    private String type;

    @Column(name = "TIMELINE")
    @ApiModelProperty(value = "是否启用时间轴，0-不启用，1-启用")
    private String  timeLine;
    
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
