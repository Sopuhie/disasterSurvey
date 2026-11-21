package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "专题出图日志新增参数信息")
public class MappingLogAddDto  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4267001447700304076L;

	@ApiModelProperty(value = "用户ID")
    private String userId;

	@ApiModelProperty(value = "用户名称")
    private String userName;

	@ApiModelProperty(value = "用户部门")
    private String department;

	@ApiModelProperty(value = "出图主题或图片名称")
    private String topic;
    
	@ApiModelProperty(value = "出图内容描述，比如使用什么图层进行出图")
    private String content;

	@ApiModelProperty(value = "状态，S出图成功，F出图失败")
    private String status;

	@ApiModelProperty(value = "状态信息，如出图成功，因什么原因出图失败")
    private String statusMsg;

}
