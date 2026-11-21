package com.santech.core.framework.entity.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RegionQueryDto {

    @ApiModelProperty(value = "行政区代码")
    private String eq_code;

    @ApiModelProperty(value = "行政区名称")
    private String like_name;
    
    @ApiModelProperty(value = "是否返回范围数据，0不返回，1返回")    
    private String hasRange;
    

    @ApiModelProperty(value = "单页显示数量")
    private int size;
    @ApiModelProperty(value = "目标页")
    private int current;


}
