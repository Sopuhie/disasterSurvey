package com.santech.module.biz.entity.dto;


import com.alibaba.fastjson.JSONArray;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description= "IAM数字福建平台推送的用户数据信息")
public class SSOSyncUserDataDto {
	
    
    @ApiModelProperty(value = "用户 id")
    private String id;
    @ApiModelProperty(value = "用户账号（唯一，采用 SM2 加密）")
    private String userName;
    @ApiModelProperty(value = "用户姓名 （采用 SM2 加密）")
    private String realName;
    @ApiModelProperty(value = "性别（1：男；0：女）")
    private String gender;
    @ApiModelProperty(value = "用户类型 0 系统用户，1 自然人，2 法人")
    private Integer userType;
    @ApiModelProperty(value = "手机号码（采用 SM2 加密）")
    private String phone;
    @ApiModelProperty(value = "籍贯")
    private String nativePlace;
    @ApiModelProperty(value = "状态，0：启用 1：禁用")
    private Integer status;
    @ApiModelProperty(value = "身份证（采用 SM2 加密）")
    private String idCard;
    @ApiModelProperty(value = "民族")
    private String nation;
    @ApiModelProperty(value = "出生年月 yyyy-MM-dd")
    private String dateBirth;
    @ApiModelProperty(value = "用户状态：0:在编, 1:离职,2:退休")
    private Integer userStatus;
    @ApiModelProperty(value = "用户排序号，用户排序规则：先按 rankSort 排序 再按 sortNum 排序")
    private Integer sortNum;
    @ApiModelProperty(value = "用户职级排序，用户排序规则：先按 rankSort 排 序再按 sortNum 排序")
    private Integer rankSort;
    @ApiModelProperty(value = "所属机构对象，operationType 字段值为 INSERT , UPDAT, INSERT_UPDATE 时，该字段为必填项")
    private JSONArray belongOrg;

  //还有其他信息，后面根据需要再加

	
	
}
