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
 * 调查对象档案（现场资料 / 附件 / 核对 / 归档）
 */
@Data
@Table(name = "T_BIZ_HAZARD_DOSSIER")
@ApiModel(description = "调查对象档案表")
public class BizHazardDossierModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(name = "ID")
	@ApiModelProperty("主键")
	private String id;

	@Column(name = "BIZ_KEY")
	@ApiModelProperty("业务键：hazardType::hazardName")
	private String bizKey;

	@Column(name = "HAZARD_TYPE")
	@ApiModelProperty("承灾体类型 seawall/fishing/mariculture/tourist")
	private String hazardType;

	@Column(name = "HAZARD_NAME")
	@ApiModelProperty("承灾体名称")
	private String hazardName;

	@Column(name = "VERIFY_STATUS")
	@ApiModelProperty("核对状态")
	private String verifyStatus;

	@Column(name = "ARCHIVE_STATUS")
	@ApiModelProperty("归档状态")
	private String archiveStatus;

	@Column(name = "ARCHIVE_NO")
	@ApiModelProperty("归档编号")
	private String archiveNo;

	@Column(name = "JSON_DATA")
	@ApiModelProperty("完整档案 JSON（含 materials/attachments/profile 等）")
	private String jsonData;

	@Column(name = "REMARK")
	private String remark;

	@Column(name = "CJR")
	private String cjr;

	@Column(name = "CJSJ")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date cjsj;

	@Column(name = "ZHGXR")
	private String zhgxr;

	@Column(name = "ZHGXSJ")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date zhgxsj;
}
