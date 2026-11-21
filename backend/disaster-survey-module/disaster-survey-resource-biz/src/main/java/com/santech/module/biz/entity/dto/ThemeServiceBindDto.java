package com.santech.module.biz.entity.dto;

import java.util.List;

import com.santech.module.biz.entity.vo.BindServiceLayerVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "数据专题绑定相关参数信息")
public class ThemeServiceBindDto {
	
	@ApiModelProperty(value = "目标专题ID")
    private String themeId;
	
	@ApiModelProperty(value = "专题绑定的服务信息")
    private List<BindServiceLayerVo> services;
	
	@ApiModelProperty(value = "当前登录人")
    private String loginUser;

}
