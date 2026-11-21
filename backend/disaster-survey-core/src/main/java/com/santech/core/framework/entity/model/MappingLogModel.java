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
@Table(name = "T_MAPPING_LOG")
@ApiModel(description = "专题出图日志表")
public class MappingLogModel  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4267001447700304076L;

	@ExcelIgnore
    @PrimaryKey
    @Column(name = "ID")
	@ApiModelProperty(value = "主键ID，ML打头")
    private String id;

    @ExcelProperty("用户ID")
    @Column(name = "USERID")
	@ApiModelProperty(value = "用户ID")
    private String userId;

    @ExcelProperty("用户名称")
    @Column(name = "USERNAME")
	@ApiModelProperty(value = "用户名称")
    private String userName;

    @ExcelProperty("用户部门")
    @Column(name = "DEPARTMENT")
	@ApiModelProperty(value = "用户部门")
    private String department;

    @ExcelProperty("出图主题")
    @Column(name = "TOPIC")
	@ApiModelProperty(value = "出图主题或图片名称")
    private String topic;
    
    @ExcelProperty("出图内容")
    @Column(name = "CONTENT")
	@ApiModelProperty(value = "出图内容描述，比如使用什么图层进行出图")
    private String content;

    @ExcelProperty("状态")
    @Column(name = "STATUS")
	@ApiModelProperty(value = "状态，S出图成功，F出图失败")
    private String status;

    @ExcelProperty("状态信息")
    @Column(name = "STATUSMSG")
	@ApiModelProperty(value = "状态信息，如出图成功，因什么原因出图失败")
    private String statusMsg;

    @ExcelProperty("IP地址")
    @Column(name = "IPADDRESS")
	@ApiModelProperty(value = "用户IP地址")
    private String ipAddress;

    @ColumnWidth(30)
    @ExcelProperty("操作时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name = "CREATETIME")
	@ApiModelProperty(value = "操作时间")
    private Date createTime;
}
