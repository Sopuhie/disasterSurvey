package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: ResourceStatisticsResultModel
 * @Description: 资源统计结果数据信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "资源统计结果数据信息")
public class ResourceStatisticsResultVo {
	
	@ApiModelProperty(value = "统计目标对象名称，字典值")
	private String nodeId;
	
	@ApiModelProperty(value = "父级节点ID")
	private String pid;
	
	@ApiModelProperty(value = "统计目标对象名称，字典值")
	private String nodeName;
	
	@ApiModelProperty(value = "统计目标对象编码，字典编码")
	private String nodeCode;
	
	@ApiModelProperty(value = "节点类型,root-根目录，folder-子目录,leaf-代表类型（末层节点）")
	private String nodeType;
	
	@ApiModelProperty(value = "统计结果或数量")
	private Integer statisticResult;
	
	@ApiModelProperty(value = "子统计结果或数量")
	private List<ResourceStatisticsResultVo> children;
	
}
