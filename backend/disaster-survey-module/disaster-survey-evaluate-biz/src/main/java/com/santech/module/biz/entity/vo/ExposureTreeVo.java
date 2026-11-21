package com.santech.module.biz.entity.vo;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "承灾体树结构信息")
public class ExposureTreeVo implements Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "节点ID（承灾体或行政区ID）")
    private String nodeId ;
	@ApiModelProperty(value = "父级节点ID（承灾体父级ID），当为根节点时，为-1")
    private String pid;
	@ApiModelProperty(value = "节点编码（承灾体编码或行政区编码），必填")
	private String nodeCode;
	@ApiModelProperty(value = "节点名称（承灾体名称或行政区名称），必填")
	private String nodeName;
	@ApiModelProperty(value = "承灾体或节点类型，REGION行政区，EXPOSURE承灾体，必填")
	private String nodeType;
	@ApiModelProperty(value = "备注，非必填")
	private String remark;
	@ApiModelProperty(value = "节点顺序号（承灾体顺序号），用来进行目录排序")
	private Integer sortIndex;
	
	@ApiModelProperty(value = "子树信息")
	private List<ExposureTreeVo> children;
	

	

}