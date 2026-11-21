package com.santech.core.framework.entity.view;

import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 上传后的文件信息model
 * @Author L
 * @Date 2021/3/14
 **/
@Data
@ApiModel(value = "文件块", description = "文件块")
public class ChunkTaskModel {

	@ApiModelProperty(value = "主键ID")
	private String id;
    /**
     * 文件块编号，从1开始
     */
	@ApiModelProperty(value = "文件块编号，从1开始")
    private Integer chunkNumber;
    /**
     * 每块大小
     */
	@ApiModelProperty(value = "每块大小")
    private Long chunkSize;
    /**
     * 当前分块大小
     */
	@ApiModelProperty(value = "当前分块大小")
    private Long currentChunkSize;
    /**
     * 总大小
     */
	@ApiModelProperty(value = "总大小")
    private Long totalSize;
    /**
     * 文件标识MD5
     */
	@ApiModelProperty(value = "文件标识MD5")
    private String fileMd5;
    /**
     * 文件名
     */
	@ApiModelProperty(value = "文件名")
    private String filename;
    /**
     * 相对路径
     */
	@ApiModelProperty(value = "分块文件存放的相对文件夹路径")
    private String relativePath;
    /**
     * 总块数
     */
	@ApiModelProperty(value = "总块数")
    private Integer totalChunks;
	
	@ApiModelProperty(value = "创建时间")
    private Date createTime;
	
	
	@ApiModelProperty(value = "最后更新时间")
    private Date updateTime;

}