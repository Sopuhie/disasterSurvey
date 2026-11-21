package com.santech.core.framework.entity.model;

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
@Table(name = "T_QX_REGION")
@ApiModel(description = "行政区数据-从地图服务初始化的行政区信息存储表")
public class QxRegionModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 329280424590849810L;

	@PrimaryKey
    @Column(name = "ID")
	@ApiModelProperty(value = "ID，RG打头")
    private String id;

    @Column(name = "CODE")
	@ApiModelProperty(value = "行政区代码")
    private String code;
    
    @Column(name = "PARENTCODE")
	@ApiModelProperty(value = "父级行政区划代码")
    private String parentCode;

    @Column(name = "NAME")
	@ApiModelProperty(value = "行政区划名称")
    private String name;
 
    @Column(name = "ALIAS")
	@ApiModelProperty(value = "别名")
    private String alias;

    @Column(name = "RANGE")
	@ApiModelProperty(value = "范围坐标")
    private String range;
    
    @Column(name = "SORTINDEX")
	@ApiModelProperty(value = "排序")
    private BigDecimal sortIndex;

    @Column(name = "REGIONLEVEL")
	@ApiModelProperty(value = "级别、市级、区级、镇级、村级")
    private String regionLevel;

    @Column(name = "REMARK")
	@ApiModelProperty(value = "说明")
    private String remark;
    
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
