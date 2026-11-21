package com.santech.core.framework.entity.model;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.annotations.Column;
import santech.common.dbcomponent.entity.annotations.PrimaryKey;
import santech.common.dbcomponent.entity.annotations.Table;

@Data
@ColumnWidth(15)
@Table(name = "T_JC_LOG")
@ApiModel(description = "系统日志表")
public class JcLogModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4267001447700304076L;

	@ExcelIgnore
    @PrimaryKey
    @Column(name = "ID")
	@ApiModelProperty(value = "ID，LOG打头")
    private String id;

    @ExcelProperty("用户ID")
    @Column(name = "USERID")
	@ApiModelProperty(value = "用户ID")
    private String userId;

    @ExcelProperty("用户名称")
    @Column(name = "USERNAME")
	@ApiModelProperty(value = "用户名称")
    private String userName;

    @ExcelProperty("操作对象")
    @Column(name = "TARGET")
	@ApiModelProperty(value = "操作描述或对象")
    private String target;

    @ExcelProperty("操作类型")
    @Column(name = "TYPE")
	@ApiModelProperty(value = "操作类型，登录、新增、删除、修改等")
    private String type;

    @ExcelProperty("状态")
    @Column(name = "STATUS")
	@ApiModelProperty(value = "状态")
    private String status;

    @ExcelProperty("状态信息")
    @Column(name = "STATUSMSG")
	@ApiModelProperty(value = "状态信息")
    private String statusMsg;

    @ColumnWidth(30)
    @ExcelProperty("操作时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "CREATETIME")
	@ApiModelProperty(value = "操作时间")
    private Date createTime;
    
    @ExcelProperty("IP地址")
    @Column(name = "IPADDRESS")
	@ApiModelProperty(value = "用户IP地址")
    private String ipAddress;
    
    @Column(name = "SIGN")
   	@ApiModelProperty(value = "签名-数据完整性验证使用")
    private String sign;
       
    @Column(name = "PUBKEYCERT")
   	@ApiModelProperty(value = "签名证书-验签时使用")
    private String pubKeyCert;

}
