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
@Table(name = "T_ZY_FILERESOURCE")
@ApiModel(description= "普通文件类资源表")
public class ZyFileModel implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2730441887860402424L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "文件资源ID，以FR打头")
    private String id;

    @Column(name = "NAME")
    @ApiModelProperty(value = "文件资源名称")
    private String name;

    @Column(name = "TYPE")
    @ApiModelProperty(value = "文件资源类型或分类")
    private String type;
    
    @Column(name = "LEVEL")
    @ApiModelProperty(value = "文件等级，如1机密级、2秘密级等,具体见字典")
    private String level;

    @Column(name = "STATUS")
    @ApiModelProperty(value = "文件资源状态，0开放，1不开放")
    private String status;
    
    @Column(name = "DESCRIPTION")
    @ApiModelProperty(value = "文件资源描述")
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
    
    private List<ZyFileDetailModel> details;


}
