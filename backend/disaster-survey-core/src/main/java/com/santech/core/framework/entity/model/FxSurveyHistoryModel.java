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
@Table(name = "T_SURVEY_EVALUATE_HISTORY")
@ApiModel(description = "灾情调查分析运行历史记录表")
public class FxSurveyHistoryModel  implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name ="ID")
	@ApiModelProperty(value ="EH+时间戳，主键")
	private  String  id;
	@Column(name ="NAME")
	@ApiModelProperty(value ="分析任务名称")
	private  String  name;
	@Column(name ="DATAPATH")
	@ApiModelProperty(value ="灾情数据SHP文件路径（如受灾空间数据）")
	private  String  dataPath;
	@Column(name ="TARGETENTITY")
	@ApiModelProperty(value ="叠置目标对象类型，Pop人口密度、Xzq乡镇区划、Dike海堤、Fishery渔港、CultureArea海水养殖区、TouristArea滨海旅游区等")
	private  String  targetEntity;
	
	@Column(name ="RESULTPATH")
	@ApiModelProperty(value ="结果文件路径（如裁切统计生成的新shp结果文件）")
	private  String  resultPath;
	@Column(name ="RESULTDATA")
	@ApiModelProperty(value ="分析结果统计数据，json的字符串")
	private  String  resultData;

	@Column(name ="SCENEID")
	@ApiModelProperty(value ="前端遥感场景ID")
	private  String  sceneId;

	@Column(name ="LITEMODE")
	@ApiModelProperty(value ="轻量模式：1是 0否")
	private  String  liteMode;
	
	@Column(name ="STATUS")
	@ApiModelProperty(value ="运行总体状态，1任务创建，2运行中，3运行完成，4运行失败")
	private  String  status;
	@Column(name ="FAILDREASON")
	@ApiModelProperty(value ="失败原因")
	private  String  faildReason;
	
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
