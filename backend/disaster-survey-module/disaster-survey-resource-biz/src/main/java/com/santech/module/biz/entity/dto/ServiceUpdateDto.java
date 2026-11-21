package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "资源信息更新数据传输对象")
public class ServiceUpdateDto  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "服务ID，以SR打头,必填")
    private String id;

    @ApiModelProperty(value = "服务名称,非必填")
    private String serviceName;
    
    @ApiModelProperty(value = "服务发布平台,如Esri平台，具体见功能配置,保存后不能修改,非必填")
    private String servicePlatform;
    
    @ApiModelProperty(value = "服务类型,如切片图、矢量图，具体见字典值,保存后不能修改,非必填")
    private String serviceType;

    @ApiModelProperty(value = "服务地址,非必填")
    private String serviceUrl;

    @ApiModelProperty(value = "范围元数据,非必填")
    private Object extentMeta;
    
    @ApiModelProperty(value = "排序索引,非必填")
    private Integer sortIndex;

    @ApiModelProperty(value = "转发地址，预留,非必填")
    private String relay;
    
    @ApiModelProperty(value = "服务状态， 0-不能调通 1-能调通,非必填")
    private String status;

    @ApiModelProperty(value = "检查服务状态，0-不检测  1-检测，预留,非必填")
    private String detect;
    
    @ApiModelProperty(value = "服务数据秘密性，1公开非涉密，2涉密")
    private String confidential;
    
    @ApiModelProperty(value = "服务资源截图,非必填")
    private String screenshot;

    @ApiModelProperty(value = "发布机构,非必填")
    private String unit;
    
    @ApiModelProperty(value = "关联子集文本集的专题id,非必填")
    private String attachmentid;
    
    @ApiModelProperty(value = "关联的规划成果ID，用来进行成果与服务关联查询,非必填")
    private String attachmentcgid;
    
    @ApiModelProperty(value = "关联资源规则id,非必填")
    private String zygzid;

    @ApiModelProperty(value = "服务描述,非必填")
    private String description;
    
    @ApiModelProperty(value = "更新人,非必填")
    private String zhgxr ;
    

}
