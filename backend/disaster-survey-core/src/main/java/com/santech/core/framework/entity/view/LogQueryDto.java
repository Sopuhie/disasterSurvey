package com.santech.core.framework.entity.view;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "LogQueryModel",description = "日志查询参数")
public class LogQueryDto {
	
	@ApiModelProperty(value = "用户id,模糊")
	private String like_userId;

	@ApiModelProperty(value = "用户名,模糊")
	private String like_username;
	
	@ApiModelProperty(value = "操作内容，模糊")
	private String like_target;
		
	@ApiModelProperty(value = "操作类型，模糊")
	private String like_type;
	
	@ApiModelProperty(value = "操作状态，模糊")
	private String like_status;
	
	@ApiModelProperty(value = "开始时间")
    private String startTime;
	
	@ApiModelProperty(value = "截止时间")
    private String endTime;
	
	//分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	@ApiModelProperty(value = "目标页")
    private int current;
	
    
}
