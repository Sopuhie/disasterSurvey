package com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "部门用户树信息")
public class UserGroupByDeptVo {
	//检索字段
	@ApiModelProperty(value = "节点id,对应部门id或用户id")
	private String nodeId;
	@ApiModelProperty(value = "树节点编码名称，对应部门编码或用户名")
	private String nodeCode;
	@ApiModelProperty(value = "部门用户树昵称，对应部门名称或用户昵称")
	private String nodeName;
	@ApiModelProperty(value = "节点类型，dept部门节点,user用户节点")
	private String nodeType;
	@ApiModelProperty(value = "父级节点ID")
	private String pid;
	@ApiModelProperty(value = "子信息")
	private List<UserGroupByDeptVo> children;
	
}
