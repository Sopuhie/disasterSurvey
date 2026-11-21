package com.santech.module.biz.entity.vo;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "专题下图层、关联资源信息视图")
public class ThemeWithServiceLayersVo {
	@ApiModelProperty(value = "专题ID")
    private String themeId;
	@ApiModelProperty(value = "专题名称")
    private String themeName;
	@ApiModelProperty(value = "relateId")
    private String relateId;
	@ApiModelProperty(value = "序号")
    private Integer sortIndex;
	@ApiModelProperty(value = "关联服务ID")
    private String serviceId;
	@ApiModelProperty(value = "关联服务名称")
    private String serviceName;
	@ApiModelProperty(value = "关联服务类型")
    private String serviceType;
	@ApiModelProperty(value = "关联服务地址")
    private String serviceUrl;
	@ApiModelProperty(value = "关联服务涉密性，1公开非涉密，2涉密")
    private String serviceConfidential;
	@ApiModelProperty(value = "关联服务说明或备注")
    private String serviceRemark;
	@ApiModelProperty(value = "是否有图层或附件")
    private Boolean isHaveLayer;
	@ApiModelProperty(value = "元数据")
    private String metaData;
	@ApiModelProperty(value = "专题服务关联的附件表格信息，预留")
    private JSONObject glzy;
}
