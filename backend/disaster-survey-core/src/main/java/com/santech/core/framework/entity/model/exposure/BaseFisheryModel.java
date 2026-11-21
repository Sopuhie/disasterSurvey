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
@Table(name = "T_BASE_FISHERY")
@ApiModel(description = "渔港基础信息表")
public class BaseFisheryModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
    @Column(name ="ID")
	@ApiModelProperty(value ="主键ID")
	private  String  id;
	@Column(name ="CODE")
	@ApiModelProperty(value ="渔港编码")
	private  String  code;
	@Column(name ="NAME")
	@ApiModelProperty(value ="渔港名称")
	private  String  name;
	@Column(name ="GRADE")
	@ApiModelProperty(value ="渔港级别，如0中心渔港，1一级渔港，2二级渔港，3三级渔港")
	private  String  grade;
	@Column(name ="LONGITUDE")
	@ApiModelProperty(value ="渔港经度")
	private  String  longitude;
	@Column(name ="LATITUDE")
	@ApiModelProperty(value ="渔港纬度")
	private  String  latitude;
	@Column(name ="ADDRESS")
	@ApiModelProperty(value ="渔港地址")
	private  String  address;
	@Column(name ="BREAKWATER")
	@ApiModelProperty(value ="防波堤类型，如斜坡式防波堤、直立式护岸")
	private  String  breakWater;
	@Column(name ="STANDARD")
	@ApiModelProperty(value ="防波堤设计防护标准，如防灾减灾能力50年一遇")
	private  String  standard;
	@Column(name ="SHIPTYPE")
	@ApiModelProperty(value ="设计船型，单位m")
	private  BigDecimal  shipType;
	@Column(name ="CAPACITY")
	@ApiModelProperty(value ="设计靠泊容量，单位只")
	private  Integer  capacity;
	@Column(name ="DANGERGRADE")
	@ApiModelProperty(value ="渔港当前隐患或危险等级，如一级隐患、二级隐患、三级隐患、无隐患")
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
