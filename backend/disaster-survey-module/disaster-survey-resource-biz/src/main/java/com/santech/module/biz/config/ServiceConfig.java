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
public class ServiceConfig {
    
    @Value("${map.symbol.file.rootpath:null}")
    private String fileRoot;//地图符号资源存放路径
    
    @Value("${service.proxy.forwarding.url:null}")
    private String proxyForwardingUrl;//地图服务代理地址
    
    @Value("${service.proxy.forwarding.enable:disable}")
    private String enable;//地图服务访问是否启用代理模式
}
