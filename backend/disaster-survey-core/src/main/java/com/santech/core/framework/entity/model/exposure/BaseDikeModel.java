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
@Table(name = "T_BASE_DIKE")
@ApiModel(description = "海堤或堤防基础信息表")
public class BaseDikeModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(name ="ID")
	@ApiModelProperty(value ="主键ID")
	private  String  id;
	@Column(name ="CODE")
	@ApiModelProperty(value ="堤防编码")
	private  String  code;
	@Column(name ="NAME")
	@ApiModelProperty(value ="堤防名称")
	private  String  name;
	@Column(name ="GRADE")
	@ApiModelProperty(value ="堤防级别，1级至5级,1级重现期为100年，5级为10-20年，6为其他无防护岸段或无海堤或非标准海堤或未合拢海堤或重现期低于10年")
	private  String  grade;
	@Column(name ="SFYRKJJCZT")
	@ApiModelProperty(value ="后方是否有人口、经济、承灾体分布，1是，0否")
	private  String  sfyrkjjczt;
	@Column(name ="LENGTH")
	@ApiModelProperty(value ="堤防长度")
	private  BigDecimal  length;
	@Column(name ="TYPE")
	@ApiModelProperty(value ="堤防类型，如斜坡式、混合式、陡墙式等")
	private  String  type;
	@Column(name ="BUILDTIME")
	@ApiModelProperty(value ="堤防建成年月，如2024-01")
	private  String  buildTime;
	@Column(name ="HTD")
	@ApiModelProperty(value ="堤顶设计高程，单位米")
	private  BigDecimal  htd;
	@Column(name ="HFD")
	@ApiModelProperty(value ="堤前设计高程，单位米")
	private  BigDecimal  hfd;
	@Column(name ="HLD")
	@ApiModelProperty(value ="防渗土体顶高程，单位米")
	private  BigDecimal  hld;
	@Column(name ="HWD")
	@ApiModelProperty(value ="设计高潮（水）位，单位米")
	private  BigDecimal  hwd;
	@Column(name ="SFWYATCSQ")
	@ApiModelProperty(value ="是否位于岸滩冲刷区,是，否")
	private  String  sfwyatcsq;
	@Column(name ="DANGERGRADE")
	@ApiModelProperty(value ="堤防隐患或危险等级，如一级隐患、二级隐患、三级隐患、无隐患")
	private  String  dangerGrade;
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
