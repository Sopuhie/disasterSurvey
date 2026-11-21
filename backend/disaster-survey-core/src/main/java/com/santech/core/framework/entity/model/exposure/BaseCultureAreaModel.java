package com.santech.core.framework.entity.model.exposure;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@Table(name = "T_BASE_CULTUREAREA")
@ApiModel(description = "海水养殖区基础信息表")
public class BaseCultureAreaModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(name ="ID")
	@ApiModelProperty(value ="主键ID")
	private  String  id;
	@Column(name ="CODE")
	@ApiModelProperty(value ="养殖区编码")
	private  String  code;
	@Column(name ="NAME")
	@ApiModelProperty(value ="养殖区名称")
	private  String  name;
	@Column(name ="LONGITUDE")
	@ApiModelProperty(value ="养殖区中心点经度")
	private  String  longitude;
	@Column(name ="LATITUDE")
	@ApiModelProperty(value ="养殖区中心点纬度")
	private  String  latitude;
	@Column(name ="TYPE")
	@ApiModelProperty(value ="养殖方式，多个以英文逗号分割，如HDPE浮筏渔排、贝类底播养殖、HDPE深水网箱、近岸池塘养殖等")
	private  String  type;
	@Column(name ="OUTPUT")
	@ApiModelProperty(value ="年产值，单位万元")
	private  BigDecimal  output;
	@Column(name ="AREA")
	@ApiModelProperty(value ="养殖区面积，单位hm²")
	private  BigDecimal  area;
	@Column(name ="DEPTH")
	@ApiModelProperty(value ="养殖水深")
	private  BigDecimal  depth;
	@Column(name ="ADDRESS")
	@ApiModelProperty(value ="养殖区地址")
	private  String  address;
	@Column(name ="DANGERGRADE")
	@ApiModelProperty(value ="养殖区当前隐患或危险等级，如一级隐患、二级隐患、三级隐患、无隐患")
	private  String  dangerGrade;
	@Column(name ="BUILDTIME")
	@ApiModelProperty(value ="建成年月，如2024-01")
	private  String  buildTime;
	@Column(name ="REGIONNAME")
	@ApiModelProperty(value ="所属行政区名称")
	private  String  regionName;
	@Column(name ="REGIONCODE")
	@ApiModelProperty(value ="所属行政区编码")
	private  String  regionCode;
	@Column(name ="SORTINDEX")
	@ApiModelProperty(value ="排序")
	private  Integer  sortIndex;
	@Column(name ="REMARK")
	@ApiModelProperty(value ="备注")
	private  String  remark;
	
	
	@Column(name ="CJR")
    @ApiModelProperty(value ="创建人")
    private  String  cjr;
    @Column(name ="CJSJ")
    @ApiModelProperty(value ="创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private  Date  cjsj;
    @Column(name ="ZHGXR")
    @ApiModelProperty(value ="最后更新人")
    private  String  zhgxr;
    @Column(name ="ZHGXSJ")
    @ApiModelProperty(value ="最后更新时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private  Date  zhgxsj;




}
