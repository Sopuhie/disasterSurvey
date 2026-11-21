package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;


@Data
@Table(name = "T_JC_SYSTEMCONFIG")
@ApiModel(description = "系统配置实体")
public class JcSystemConfigModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -912778268278703227L;

	@PrimaryKey
    @Column(name = "ID")
	@ApiModelProperty(value = "ID，SC打头")
    private String id;

    @Column(name = "NAME")
	@ApiModelProperty(value = "配置名称")
    private String name;

    @Column(name = "ALIASNAME")
	@ApiModelProperty(value = "配置别名")
    private String aliasName;

    @Column(name = "TITLE")
	@ApiModelProperty(value = "系统标题")
    private String title;

    @Column(name = "LOGO")
	@ApiModelProperty(value = "系统图标")
    private String logo;
    
    @Column(name = "STYLE")
	@ApiModelProperty(value = "系统风格，如simple简约，traditional 传统")
    private String style;
 
    @Column(name = "MAPTHEME")
	@ApiModelProperty(value = "地图底图专题")
    private String mapTheme;

    @Column(name = "IMAGETHEME")
	@ApiModelProperty(value = "影像底图专题")
    private String imageTheme;

    @Column(name = "MAP3DTHEME")
	@ApiModelProperty(value = "三维底图专题")
    private String map3dTheme;

    @Column(name = "CODE")
	@ApiModelProperty(value = "空间参考标识，EPSG，读取字典，如EPSG:4527")
    private String code;

    @Column(name = "PROJECTION")
	@ApiModelProperty(value = "空间参考")
    private String projection;

    @Column(name = "MAXZOOM")
	@ApiModelProperty(value = "最大级别")
    private Integer maxZoom;

    @Column(name = "MINZOOM")
	@ApiModelProperty(value = "最小级别")
    private Integer minZoom;

    @Column(name = "ZOOM")
	@ApiModelProperty(value = "初始级别")
    private Integer zoom;

    @Column(name = "EXTENT")
	@ApiModelProperty(value = "范围")
    private String extent;

    @Column(name = "CENTER")
	@ApiModelProperty(value = "中心点")
    private String center;

    @Column(name = "UNIT")
	@ApiModelProperty(value = "单位")
    private String unit;

    @Column(name = "RESOLUTIONS")
	@ApiModelProperty(value = "级别")
    private String resolutions;

    @Column(name = "ORIGIN")
	@ApiModelProperty(value = "原点")
    private String origin;

    @Column(name = "SORTINDEX")
	@ApiModelProperty(value = "排序")
    private Integer sortindex;

    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;
    
    @Column(name = "ZHGXR")
    @ApiModelProperty(value = "更新人")
    private String zhgxr ;
    
    @Column(name = "ZHGXSJ")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date zhgxsj ;
    
    @Column(name = "SIGN")
	@ApiModelProperty(value = "签名-数据完整性验证使用，CODE 岸段编码，NAME 岸段名称，REGIONCODE 所属行政区编码，GRADE 岸段等级STATUS 岸段状态")
    private String sign;
    
    @Column(name = "PUBKEYCERT")
	@ApiModelProperty(value = "签名证书-验签时使用")
    private String pubKeyCert;

}
