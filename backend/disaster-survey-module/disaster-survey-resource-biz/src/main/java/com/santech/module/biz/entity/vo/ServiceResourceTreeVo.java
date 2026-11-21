package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ServiceResourceTreeModel
 * @Description: 数据服务资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据服务资源树信息")
public class ServiceResourceTreeVo {
	//检索字段
	@ApiModelProperty(value = "节点ID")
	private String nodeId;
	@ApiModelProperty(value = "节点名称")
	private String nodeName;
	@ApiModelProperty(value = "节点类型,folder-代表平台,leaf-代表类型,service-代表数据服务,layer-代表服务的图层信息")
	private String nodeType;
	@ApiModelProperty(value = "节点对应的资源数据量")
	private int serviceCount ;
	@ApiModelProperty(value = "当节点对应图层类型的时候，此字段有值，对应所属服务id")
	private String serviceId;
	@ApiModelProperty(value = "当节点对应图层类型的时候，此字段有值，对应所属服务名称")
	private String serviceName;
	@ApiModelProperty(value = "子树信息")
	private List<ServiceResourceTreeVo> children;
	
	
	
	
}
