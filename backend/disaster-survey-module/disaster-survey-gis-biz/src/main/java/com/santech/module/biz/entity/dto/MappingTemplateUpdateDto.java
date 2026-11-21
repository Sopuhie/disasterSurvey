package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import org.postgresql.util.PGobject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "专题制图模板更新数据传输对象")
public class MappingTemplateUpdateDto  implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "专题制图模板ID，以TMT打头，必填")
    private String id;

    @ApiModelProperty(value = "专题制图模板名称，非必填")
    private String name;
    
    @ApiModelProperty(value = "专题制图模板内容，非必填")
    private String content;
    
    @ApiModelProperty(value = "模板分享类型，0不共享，1共享，非必填")
    private String isShare;

    @ApiModelProperty(value = "更新人，必填")
    private String zhgxr ;

}
