package com.santech.core.framework.entity.view;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "字典类型查询参数")
public class DictionaryTypeQueryDto {
	
	@ApiModelProperty(value = "字典类型编码,模糊")
	private String like_zdlxbm;

	@ApiModelProperty(value = "字典类型名称,模糊")
	private String like_zdlxmc;
	
	@ApiModelProperty(value = "是否启用或删除标识，0启用,1不启用或已删除")
	private String eq_sfqy;
	
	//分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
    
}
