package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "系统配置新增数据传输对象")
public class SystemConfigAddDto  implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -1705015021308516552L;

	@ApiModelProperty(value = "配置名称，必填")
    private String name;

	@ApiModelProperty(value = "配置别名，必填")
    private String aliasName;

	@ApiModelProperty(value = "系统标题，必填")
    private String title;

	@ApiModelProperty(value = "系统图标，必填")
    private String logo;
    
	@ApiModelProperty(value = "系统风格，必填，如simple简约，traditional 传统")
    private String style;
 
	@ApiModelProperty(value = "地图底图专题，必填")
    private String mapTheme;

	@ApiModelProperty(value = "影像底图专题，非必填")
    private String imageTheme;

	@ApiModelProperty(value = "三维底图专题，非必填")
    private String map3dTheme;

	@ApiModelProperty(value = "空间参考标识，必填，EPSG，读取字典，如EPSG:4527")
    private String code;

	@ApiModelProperty(value = "空间参考，非必填")
    private String projection;

	@ApiModelProperty(value = "最大级别，必填")
    private Integer maxZoom;

	@ApiModelProperty(value = "最小级别，必填")
    private Integer minZoom;

	@ApiModelProperty(value = "初始级别，必填")
    private Integer zoom;

	@ApiModelProperty(value = "范围，非必填")
    private String extent;

	@ApiModelProperty(value = "中心点，必填")
    private String center;

	@ApiModelProperty(value = "单位，必填")
    private String unit;

	@ApiModelProperty(value = "级别，非必填")
    private String resolutions;

	@ApiModelProperty(value = "原点，非必填")
    private String origin;

	@ApiModelProperty(value = "排序，非必填，不传默认库中对应记录排序的最大值")
    private Integer sortindex;

    @ApiModelProperty(value = "创建人，必填")
    private String cjr ;
    

}
