package com.santech.core.framework.entity.view;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件分片合并传参数model
 * @Author L
 * @Date 2023/1/14
 **/
@Data
@ApiModel(value = "文件分片合并传参数", description = "文件分片合并传参数")
public class ChunkMergeParamsModel {
	
    /**
     * 文件标识MD5
     */
	@ApiModelProperty(value = "文件标识MD5，前端生成")
    private String fileMd5;
	/**
     * 总块数
     */
	@ApiModelProperty(value = "总块数")
    private Integer totalChunks;
	
}