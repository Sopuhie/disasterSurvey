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

/**
 * 前端调查/评估业务台账通用存储（JSON 文档）。
 * 对应前端 surveyBase 各 dataType。
 */
@Data
@Table(name = "T_BIZ_FRONTEND_DATA")
@ApiModel(description = "前端业务台账通用表")
public class BizFrontendDataModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty("主键")
	private String id;

	@Column(name = "DATA_TYPE")
	@ApiModelProperty("数据类型，如 seawall/rsScene/assessSar")
	private String dataType;

	@Column(name = "BIZ_KEY")
	@ApiModelProperty("业务键（通常等于前端 id）")
	private String bizKey;

	@Column(name = "NAME")
	@ApiModelProperty("名称（便于检索）")
	private String name;

	@Column(name = "EXTRA_TYPE")
	@ApiModelProperty("子类型，如 wind/wave/storm")
	private String extraType;

	@Column(name = "JSON_DATA")
	@ApiModelProperty("完整业务 JSON")
	private String jsonData;

	@Column(name = "SORT_INDEX")
	@ApiModelProperty("排序")
	private Integer sortIndex;

	@Column(name = "REMARK")
	@ApiModelProperty("备注")
	private String remark;

	@Column(name = "CJR")
	@ApiModelProperty("创建人")
	private String cjr;

	@Column(name = "CJSJ")
	@ApiModelProperty("创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date cjsj;

	@Column(name = "ZHGXR")
	@ApiModelProperty("最后更新人")
	private String zhgxr;

	@Column(name = "ZHGXSJ")
	@ApiModelProperty("最后更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date zhgxsj;
}
