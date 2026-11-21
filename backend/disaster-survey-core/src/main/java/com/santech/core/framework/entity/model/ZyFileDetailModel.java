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
@Table(name = "T_ZY_FILEDETAIL")
@ApiModel(description= "文件资源明细表")
public class ZyFileDetailModel implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2730441887860402424L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "文件id，以F打头")
    private String id;

    @Column(name = "FRID")
    @ApiModelProperty(value = "文件资源表ID")
    private String frid;

    @Column(name = "FILENAME")
    @ApiModelProperty(value = "文件名称")
    private String fileName;
    
    @Column(name = "FILEPATH")
    @ApiModelProperty(value = "文件存储相对路径")
    private String filePath;

    @Column(name = "FILETYPE")
    @ApiModelProperty(value = "文件类型（扩展名）")
    private String fileType;
    
    @Column(name = "FILESIZE")
    @ApiModelProperty(value = "文件大小")
    private Long fileSize;
    
    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;
    

}
