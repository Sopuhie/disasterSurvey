package com.santech.module.biz.entity.vo;

import com.linewell.hsfjsso.entity.AccessTokenModel;
import com.linewell.hsfjsso.entity.PeopleDetailInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 单点登录后的结果信息
 **/
@Data
@ApiModel(description= "单点登录后的结果信息")
public class SSOHsfjResultVo {
	
	@ApiModelProperty(value = "token信息")
    private AccessTokenModel tokenInfo;
	
	@ApiModelProperty(value = "用户信息")
    private PeopleDetailInfo userInfo;
	
	
}
