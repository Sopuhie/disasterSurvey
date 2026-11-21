package com.santech.module.biz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @Title: AuthenticationConfig.java 
 * @Description：权限认证相关配置参数
 * @Version： V1.0
 * @Modify：
 */
@Data
@Component
public class AuthenticationConfig {
    
    @Value("${authentication.rasPrivateKey:null}")
    private String rsaPrivateKey;//本地登录密码解密私钥
    
    @Value("${authentication.user.defaultPassword:aaaa_0000}")
    private String defaultPassword;//默认密码
    
    @Value("${authentication.user.xzqdm:null}")
    private String appCity;
}
