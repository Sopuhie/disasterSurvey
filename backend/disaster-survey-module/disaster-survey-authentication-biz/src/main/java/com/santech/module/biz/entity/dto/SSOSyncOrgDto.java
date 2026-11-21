package com.santech.module.biz.entity.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "IAM数字福建平台推送的机构信息")
public class SSOSyncOrgDto {
	
	@ApiModelProperty(value = "目标应用名称")
    private String targetAppName;
    @ApiModelProperty(value = "同步数据类型：ORG：机构 USER：用户")
    private String dataType;
    @ApiModelProperty(value = "同步操作类型：INSERT：新增, UPDATE：更新, DELETE：删除, INSERT_UPDATE：手动新增或删除" )
    private String operationType;
    @ApiModelProperty(value = "推送数据时间 yyyy-MM-dd HH:mm:ss")
    private String pushTime;
    @ApiModelProperty(value = "同步数据机构对象")
    private SSOSyncOrgDataDto data;

	
	
}
