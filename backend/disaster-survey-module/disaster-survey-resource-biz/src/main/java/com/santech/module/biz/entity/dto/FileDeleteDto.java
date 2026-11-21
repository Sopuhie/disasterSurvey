package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "文件删除参数信息")
public class FileDeleteDto implements Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "文件资源ID")
    private String fileResourceId ;
	
	@ApiModelProperty(value = "文件资源关联的子文件ID,多个以英文逗号分割")
	private String fileDetailIds ;
	
	@ApiModelProperty(value = "当前登录人")
    private String loginUser;
    
    

}