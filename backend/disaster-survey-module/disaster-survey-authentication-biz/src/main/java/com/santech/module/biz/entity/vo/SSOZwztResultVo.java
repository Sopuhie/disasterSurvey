package com.santech.module.biz.entity.vo;


import com.linewell.sso.entity.SsoToken;
import com.linewell.sso.entity.SsoUser;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 单点登录后的结果信息
 **/
@Data
@ApiModel(description= "单点登录后的结果信息")
public class SSOZwztResultVo {
	
	@ApiModelProperty(value = "token信息")
    private SsoToken ssoToken;
	
	@ApiModelProperty(value = "用户信息")
    private SsoUser ssoUser;
	
	
}
