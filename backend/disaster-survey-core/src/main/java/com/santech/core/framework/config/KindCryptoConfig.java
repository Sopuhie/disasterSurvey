package com.santech.core.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @Title: KindCryptoConfig.java 
 * @Description：KindCrypto加解密配置
 * @Version： V1.0
 * @Modify：
 */
@Component
@Data
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "kind.crypto")
public class KindCryptoConfig {
	  
    //是否启用加解密
    private boolean enable;
    //加解密应用名称
    private String appname;

    
}
