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
 * 调查对象档案（现场资料 / 附件 / 核对 / 归档）。
 * 补充 event_id / baseline_id / exposure_type 主线关联字段。
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

	@Column(name = "EVENT_ID")
	@ApiModelProperty("关联灾害事件ID")
	private String eventId;

	@Column(name = "BASELINE_ID")
	@ApiModelProperty("关联调查基线ID")
	private String baselineId;

	@Column(name = "EXPOSURE_TYPE")
	@ApiModelProperty("承灾体类型，取自 ExposureTypeEnum")
	private String exposureType;

	@Column(name = "EXPOSURE_ID")
	@ApiModelProperty("承灾体业务ID")
	private String exposureId;

	@Column(name = "RS_CLIP_IMAGE_URL")
	@ApiModelProperty("导出的承灾体遥感影像路径")
	private String rsClipImageUrl;

	@Column(name = "FIELD_ATTACHMENTS_JSON")
	@ApiModelProperty("现场核查照片/附件 JSON")
	private String fieldAttachmentsJson;

	@Column(name = "BIZ_KEY")
	@ApiModelProperty("业务键：hazardType::hazardName（兼容旧链路）")
	private String bizKey;

	@Column(name = "HAZARD_TYPE")
	@ApiModelProperty("承灾体类型旧编码 seawall/fishing/mariculture/tourist（兼容）")
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
