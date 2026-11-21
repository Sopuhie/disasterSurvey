package com.santech.module.biz.entity.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 单点登录后的结果信息
 **/
@Data
@ApiModel(description= "IAM数字福建平台推送的机构数据信息")
public class SSOSyncOrgDataDto {
	
    
    @ApiModelProperty(value = "机构 id")
    private String  id;
    @ApiModelProperty(value = "机构名称")
    private String name;
    @ApiModelProperty(value = "简称")
    private String shortName;
    @ApiModelProperty(value = "统一社会信用代码")
    private String creditCode;
    @ApiModelProperty(value = "机构级别，1: 国家级 2: 省部级 3: 司厅局级 4: 县处级 5: 乡镇科级 6: 市级 7: 村级")
    private String standard;
    @ApiModelProperty(value = "机构类型，1:区域 2:部门 3:虚拟")
    private String nature;
    @ApiModelProperty(value = "状态（0：启用；1：禁用）")
    private Integer status;
    @ApiModelProperty(value = "排序号")
    private Integer sortNum;
    @ApiModelProperty(value = "部门编码")
    private String deptCode;
    @ApiModelProperty(value = "国家部门名称")
    private String nationalName;
    @ApiModelProperty(value = "国家部门编码")
    private String nationalCode;
    @ApiModelProperty(value = "区域编码")
    private String regionCode;
    @ApiModelProperty(value = "父节点机构，对象中包含 id，name，orgPath")
    private Object parentOrg;
    @ApiModelProperty(value = "部门类型：1:单位 2:处室 3:科室 4:股所 5:其 他")
    private String deptType;
    @ApiModelProperty(value = "机构全路径")
    private String orgPath;
    @ApiModelProperty(value = "节点类型：01:地域节点 02:分类节点 03:行政区\r\n"
    		+ "域节点 04:单位节点 05:内设节点 06:联络节点\r\n"
    		+ "07:虚拟节点 08:网格节点\r\n"
    		+ "")
    private String nodeType;



	
	
}
