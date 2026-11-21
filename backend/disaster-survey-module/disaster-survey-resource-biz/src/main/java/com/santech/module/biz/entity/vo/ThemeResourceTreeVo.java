package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ThemeResourceTreeModel
 * @Description: 数据专题资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据专题资源树信息")
public class ThemeResourceTreeVo {
	//检索字段
	@ApiModelProperty(value = "节点ID")
	private String nodeId;
	@ApiModelProperty(value = "节点名称")
	private String nodeName;
	@ApiModelProperty(value = "节点类型,folder-代表平台,theme-代表类型,service-代表数据服务,layer-代表服务的图层信息")
	private String nodeType;
	@ApiModelProperty(value = "父级节点ID")
	private String pid;
	@ApiModelProperty(value = "子树信息")
	private List<ThemeResourceTreeVo> children;
	
	
	
	
}
