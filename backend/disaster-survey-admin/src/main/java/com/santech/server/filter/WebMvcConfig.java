package com.santech.server.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.santech.core.framework.config.FileBasicConfig;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer   {
	@Resource
    private FileBasicConfig fileBasicConfig;
	@Bean
    public SysInterceptor loginInterceptor() {
        return new SysInterceptor();
    }
    /**
     * 配置读取文件的虚拟地址
     */
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String names= System.getProperty("os.name");
   	 	if(names.toLowerCase().startsWith("win")){
   		 //Windows——由于使用了文件服务器，故暂时没有配置win的环境路径
   	 		registry.addResourceHandler("/openfile/**").addResourceLocations("file:"+fileBasicConfig.getRootForWin());
        }else{
       	 //Linux
            registry.addResourceHandler("/openfile/**").addResourceLocations("file:"+fileBasicConfig.getRootForLiunx());
        }
    	 registry.addResourceHandler("swagger-ui.html")
         .addResourceLocations("classpath:/META-INF/resources/");
    	 registry.addResourceHandler("/webjars/**")
         .addResourceLocations("classpath:/META-INF/resources/webjars/");
    	 registry.addResourceHandler("doc.html")
         .addResourceLocations("classpath:/META-INF/resources/");
    }

	/**
	 * 允许跨域请求
	 * 
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
		.allowedOriginPatterns("*")
		.allowedHeaders("*")
		.allowCredentials(true)
		.allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
		.maxAge(3600);
	}
	
	
	/**
     * 配置不需要拦截的请求
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**", "/doc.html/**")
                .excludePathPatterns("/web-disaster-common/web/disaster/common/checkcode/generate")
                .excludePathPatterns("/web-disaster-authentication/web/disaster/authentication/user/verify/login")
                .excludePathPatterns("/web-disaster-authentication/web/disaster/authentication/user/verify/refreshToken")
                .excludePathPatterns("/web-disaster-authentication/web/disaster/authentication/user/verify/check/login")
                .excludePathPatterns("/web-disaster-resource/web/disaster/resource/service/proxy/forwarding/**")
                .excludePathPatterns("/web-disaster-resource/web/disaster/resource/style/getxml/**")
        		.excludePathPatterns("/web-disaster-common/web/disaster/common/openfile/**");
    }
    

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(stringHttpMessageConverter());
        converters.add(new ByteArrayHttpMessageConverter());
    }
    
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }
}