package com.santech.module.biz.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "Shp文件属性字段及数据信息结果对象")
public class ShpTableFieldsVo {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "字段名称")
    private String name;
	
	@ApiModelProperty(value = "字段别名")
    private String aliasName;
	
	@ApiModelProperty(value = "字段类型")
    private String type;
	
	@ApiModelProperty(value = "字段描述")
    private String fieldDescrption;
	

}
