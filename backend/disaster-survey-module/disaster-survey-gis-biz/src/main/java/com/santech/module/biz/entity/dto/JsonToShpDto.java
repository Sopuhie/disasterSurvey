package com.santech.module.biz.entity.dto;

import java.util.List;
import java.util.Map;

import com.santech.module.biz.entity.vo.ShpTableFieldsVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "将featuresjson以shp导出接口参数定义")
public class JsonToShpDto {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "图层名称,必填")
    private String fileName;
	
	@ApiModelProperty(value = "要素数据中的几何图形字段的key，如geom,必填")
    private String geometryKey;
	
	@ApiModelProperty(value = "几何图形类型，如点：Point,必填")
    private String geoType;
	
	@ApiModelProperty(value = "空间参考，如点：EPSG:4527,必填")
    private String crs;
	
	@ApiModelProperty(value = "文件编码格式，如：UTF-8,非必填，不传默认UTF-8")
    private String encode;
	
	@ApiModelProperty(value = "属性字段，不含几何图形,如[ {name: \"name\",type: \"string\"}],必填")
    private List<ShpTableFieldsVo> attrFields;
	
	@ApiModelProperty(value = "要素数据,图形数据采用wkt格式，如点数据[{name:\"地方\",geom:\"POINT(109.588 34.645)\"}],必填")
    private List<Map<String, Object>> features;
	
	@ApiModelProperty(value = "当前登录人,必填")
	private String loginUser;

}
