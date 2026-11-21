package com.santech.module.biz.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 模型运行历史查询条件参数信息
 **/
@Data
@ApiModel(description= "模型运行历史查询条件参数信息")
public class EvaluateHistoryQueryDto {
	
	@ApiModelProperty(value = "ID,精确匹配")
    private String eq_id;
	
	@ApiModelProperty(value = "评价模型任务名称,模糊匹配")
    private String like_name;
	
	@ApiModelProperty(value = "评价范围或网格类型,精确匹配")
    private String eq_rangeType;
	
	@ApiModelProperty(value = "评价方法,精确匹配")
	private String eq_method;
	
	@ApiModelProperty(value = "运行总体状态，1任务创建，2运行中，3运行完成，4运行失败,精确匹配")
	private String eq_status;

	@ApiModelProperty(value = "灾害类型，精确匹配")
	private String eq_disasterType;

	@ApiModelProperty(value = "场景ID，精确匹配")
	private String eq_sceneId;

	@ApiModelProperty(value = "轻量模式 1/0，精确匹配")
	private String eq_liteMode;
	
	@ApiModelProperty(value ="是否收藏，1收藏，0未收藏")
	private  String  eq_sfsc;
	
	@ApiModelProperty(value = "任务来源，OCEAN海洋局，OTHER第三方,精确匹配,预留")
	private String eq_source;
	
	@ApiModelProperty(value = "任务记录查询起始时间")
    private String startTime;
	
	@ApiModelProperty(value = "任务记录查询截止时间")
    private String endTime;
	
	@ApiModelProperty(value = "当前登录人,精确匹配")
    private String loginUser;
	
    //分页字段
	@ApiModelProperty(value = "单页显示数量")
    private int size;
	
	@ApiModelProperty(value = "目标页")
    private int current;

	@ApiModelProperty(value = "登录用户是否为管理员标识，1是管理员，0非管理员")
	private String adminFlag;
}
