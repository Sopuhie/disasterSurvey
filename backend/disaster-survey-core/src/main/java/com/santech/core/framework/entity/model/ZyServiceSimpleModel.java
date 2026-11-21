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
@Table(name = "T_ZY_SERVICE")
@ApiModel(description = "资源信息表,不含服务截图信息")
public class ZyServiceSimpleModel  implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = -7453547921843082472L;

	@PrimaryKey
    @Column(name = "ID")
    @ApiModelProperty(value = "服务ID")
    private String id;

    @Column(name = "SERVICENAME")
    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    @Column(name = "SERVICEPLATFORM")
    @ApiModelProperty(value = "服务发布平台,如Esri平台，具体见字典值,保存后不能修改")
    private String servicePlatform;
    
    @Column(name = "SERVICETYPE")
    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @Column(name = "SERVICEURL")
    @ApiModelProperty(value = "服务地址")
    private String serviceUrl;


    @Column(name = "EXTENTMETA")
    @ApiModelProperty(value = "范围元数据")
    private String extentMeta;
    

    @Column(name = "SORTINDEX")
    @ApiModelProperty(value = "排序索引")
    private Integer sortIndex;


    @Column(name = "RELAY")
    @ApiModelProperty(value = "转发地址，预留")
    private String relay;

    @Column(name = "STATUS")
    @ApiModelProperty(value = "服务状态， 0-未启动 1-已启动 2-能调通 3-不能调通，预留")
    private String status;

    @Column(name = "DETECT")
    @ApiModelProperty(value = "检查服务状态，0-不检测  1-检测，预留")
    private String detect;
    
    @Column(name = "CONFIDENTIAL")
    @ApiModelProperty(value = "服务数据秘密性，1公开非涉密，2涉密")
    private String confidential;
    
    @Column(name = "UNIT")
    @ApiModelProperty(value = "发布机构")
    private String unit;
    
    @ApiModelProperty(value = "关联子集文本集的专题id")
    @Column(name = "ATTACHMENTID")
    private String attachmentid;
    
    @ApiModelProperty(value = "关联的规划成果ID，用来进行成果与服务关联查询")
    @Column(name = "ATTACHMENTCGID")
    private String attachmentcgid;
    
    @ApiModelProperty(value = "关联资源规则id")
    @Column(name = "ZYGZID")
    private String zygzid;


    @ApiModelProperty(value = "服务描述")
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "CJR")
    @ApiModelProperty(value = "创建人")
    private String cjr ;
    
    @Column(name = "CJSJ")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date cjsj ;
    
    @Column(name = "ZHGXR")
    @ApiModelProperty(value = "更新人")
    private String zhgxr ;
    
    @Column(name = "ZHGXSJ")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date zhgxsj ;

   /* @ApiModelProperty(value = "子图层信息")
    private List<ZyLayerModel> layers;*/
    
    
	/*
	 * public JSONObject getExtentMeta() { JSONObject extentMetajson =null;
	 * if(extentMeta!=null) { if(!PGobject.class.isInstance(extentMeta)) {
	 * //页面传递用于保存或更新的信息 extentMetajson=
	 * JSONObject.parseObject(JSON.toJSONString(extentMeta)); }else { //从数据库中查询的数据
	 * extentMetajson= JSONObject.parseObject(extentMeta.toString()); } } return
	 * extentMetajson; }
	 */

}
