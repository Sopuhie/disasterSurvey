package com.santech.module.biz.entity.vo;

import java.io.Serializable;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "叠加上传的空间图形文件解析结果数据对象")
public class OverLayDataVo  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "上传的Txt文件解析结果")
    private Map<String,Object> txtData;
    
    @ApiModelProperty(value = "上传的Txt文件解析结果")
    private Map<Object,Object> shpData;
    
    @ApiModelProperty(value = "上传的Cad文件解析结果")
    private Map<String,Object> cadData;


}
