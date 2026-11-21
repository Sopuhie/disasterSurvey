package com.santech.module.biz.entity.vo.txt;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "字段数据对象")
public class FieldVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字段名称")
    private String name;
    
    @ApiModelProperty(value = "字段别名")
    private String alias;

    @ApiModelProperty(value = "字段类型")
    private String type;
    

}
