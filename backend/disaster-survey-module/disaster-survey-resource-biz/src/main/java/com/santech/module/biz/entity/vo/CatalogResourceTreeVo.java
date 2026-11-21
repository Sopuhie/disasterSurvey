package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: CatalogResourceTreeModel
 * @Description: 数据目录资源分页查询参数信息
 * @Version： V1.0
 * @Modify:
 */
@Data
@ApiModel(description= "数据目录资源树信息")
public class CatalogResourceTreeVo {
	//检索字段
	@ApiModelProperty(value = "节点ID，目录id")
	private String nodeId;
	@ApiModelProperty(value = "节点名称，目录名称")
	private String nodeName;
	@ApiModelProperty(value = "节点类型,system-根目录，folder-代表子目录,theme-代表专题目录")
	private String nodeType;
	@ApiModelProperty(value = "父级节点ID")
	private String pid;
	@ApiModelProperty(value = "所绑定的专题ID")
	private String themeId;
	@ApiModelProperty(value = "排序")
    private Integer sortIndex;
	@ApiModelProperty(value = "子树信息")
	private List<CatalogResourceTreeVo> children;
	
	
	
	
}
