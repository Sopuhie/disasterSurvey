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
@Table(name = "T_BASE_TOURISTAREA")
@ApiModel(description = "滨海旅游区基础信息表")
public class BaseTouristAreaModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@PrimaryKey
	@Column(name ="ID")
	@ApiModelProperty(value ="主键ID")
	private  String  id;
	@Column(name ="CODE")
	@ApiModelProperty(value ="滨海旅游区编码")
	private  String  code;
	@Column(name ="NAME")
	@ApiModelProperty(value ="滨海旅游区名称")
	private  String  name;
	@Column(name ="ADDRESS")
	@ApiModelProperty(value ="滨海旅游区地址")
	private  String  address;
	@Column(name ="COMPANY")
	@ApiModelProperty(value ="运营公司或管理单位")
	private  String  company;
	@Column(name ="CONTACTS")
	@ApiModelProperty(value ="联系人,加密存储")
	private  String  contacts;
	@Column(name ="TEL")
	@ApiModelProperty(value ="联系方式或电话,加密存储")
	private  String  tel;
	@Column(name ="SEANUM")
	@ApiModelProperty(value ="海域管理号")
	private  String  seaNum;
	@Column(name ="SEAAREA")
	@ApiModelProperty(value ="用海面积，单位hm2")
	private  BigDecimal  seaArea;
	@Column(name ="DIKELENGTH")
	@ApiModelProperty(value ="占用岸线长度，单位米")
	private  BigDecimal  dikeLength;
	@Column(name ="START_LONG")
	@ApiModelProperty(value ="起点经度，加密存储")
	private  String  startLong;
	@Column(name ="START_LAT")
	@ApiModelProperty(value ="起点纬度，加密存储")
	private  String  startLat;
	@Column(name ="END_LONG")
	@ApiModelProperty(value ="终点经度")
	private  String  endLong;
	@Column(name ="END_LAT")
	@ApiModelProperty(value ="终点纬度")
	private  String  endLat;
	@Column(name ="WJRJKLL")
	@ApiModelProperty(value ="旺季日均游客量,单位人")
	private  BigDecimal  wjrjkll;
	@Column(name ="JNNSSGS")
	@ApiModelProperty(value ="近年溺水事故数，单位件")
	private  BigDecimal  jnnssgs;
	@Column(name ="JNNWRS")
	@ApiModelProperty(value ="近年溺亡人数，单位人")
	private  BigDecimal  jnnwrs;
	@Column(name ="DANGERGRADE")
	@ApiModelProperty(value ="滨海旅游区当前裂流灾害隐患或危险等级，如高、中、低、无隐患")
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
	private  Integer sortIndex;
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
