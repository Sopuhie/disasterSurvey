package com.santech.module.biz.entity.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "数据专题绑定详细参数信息")
public class BindServiceLayerVo {
	
	/*@ApiModelProperty(value = "服务资源树节点id,对应服务的id或图层的id")
    private String id;*/
	
	/*@ApiModelProperty(value = "服务资源树节点名称,对应图层的名称")
    private String layerName;*/
	
	@ApiModelProperty(value = "图层节点时对应图层所属服务id，服务节点时，就是对应的节点id")
    private String serviceId;
	
	@ApiModelProperty(value = "图层节点时对应图层所属服务名称，服务节点时，就是对应的节点名称")
    private String serviceName;
	
	/*@ApiModelProperty(value = "对应图层序号")
    private Integer layerIndex;*/
	
    //private List<BindServiceLayerModel> list;

}
