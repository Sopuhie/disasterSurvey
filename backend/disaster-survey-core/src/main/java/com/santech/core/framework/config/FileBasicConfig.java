package com.santech.core.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @Title: CommonConfig.java 
 * @Description：服务器文件配置
 * @Version： V1.0
 * @Modify：
 */
@Component
@Data
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "file")
public class FileBasicConfig {
    //linux根路径
    private String rootForLiunx;
    //windows根路径
    private String rootForWin;
    //微服务日志文件绝对路径，多个以英文逗号分割
    private String logpath;
    
    private String localCachePathLin;
    private String localCachePathWin;

    //执行评价的公共程序包路径
  	private String modelJarPath;
    //岸段范围shp编码格式
    private String encode;
}
