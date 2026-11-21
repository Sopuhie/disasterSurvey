package com.santech.module.biz.entity.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "系统配置更新数据传输对象")
public class SystemConfigUpdateDto  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5394593143966223487L;

	@ApiModelProperty(value = "ID，SC打头，必填")
    private String id;
	
	@ApiModelProperty(value = "配置名称，非必填")
    private String name;

	@ApiModelProperty(value = "配置别名，非必填")
    private String aliasName;

	@ApiModelProperty(value = "系统标题，非必填")
    private String title;

	@ApiModelProperty(value = "系统图标，非必填")
    private String logo;
    
	@ApiModelProperty(value = "系统风格，非必填，如simple简约，traditional 传统")
    private String style;
 
	@ApiModelProperty(value = "地图底图专题，非必填")
    private String mapTheme;

	@ApiModelProperty(value = "影像底图专题，非必填")
    private String imageTheme;

	@ApiModelProperty(value = "三维底图专题，非必填")
    private String map3dTheme;

	@ApiModelProperty(value = "空间参考标识，非必填，EPSG，读取字典，如EPSG:4527")
    private String code;

	@ApiModelProperty(value = "空间参考，非必填")
    private String projection;

	@ApiModelProperty(value = "最大级别，非必填")
    private Integer maxZoom;

	@ApiModelProperty(value = "最小级别，非必填")
    private Integer minZoom;

	@ApiModelProperty(value = "初始级别，非必填")
    private Integer zoom;

	@ApiModelProperty(value = "范围，非必填")
    private String extent;

	@ApiModelProperty(value = "中心点，非必填")
    private String center;

	@ApiModelProperty(value = "单位，非必填")
    private String unit;

	@ApiModelProperty(value = "级别，非必填")
    private String resolutions;

	@ApiModelProperty(value = "原点，非必填")
    private String origin;

	@ApiModelProperty(value = "排序，非必填")
    private Integer sortindex;

    @ApiModelProperty(value = "更新人，必填")
    private String zhgxr ;
    

}
