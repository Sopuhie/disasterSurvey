package com.santech.core.framework.entity.view;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件分片上传参数model
 * @Author L
 * @Date 2023/1/14
 **/
@Data
@ApiModel(value = "文件分片上传参数", description = "文件分片上传参数")
public class ChunkUploadParamsModel {

    
    
    /**
     * 文件标识MD5
     */
	@ApiModelProperty(value = "文件标识MD5，前端生成")
    private String fileMd5;
    /**
     * 文件名
     */
	@ApiModelProperty(value = "文件名")
    private String filename;
	
	/**
     * 文件块编号，从1开始
     */
	@ApiModelProperty(value = "文件块编号，从1开始,或是文件的第几个分片")
    private Integer chunkNumber;
	/**
     * 总块数
     */
	@ApiModelProperty(value = "总块数")
    private Integer totalChunks;
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
     * 块内容
     */
	@ApiModelProperty(value = "具体文件块")
    private MultipartFile upfile;
}