package com.santech.server.filter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {
	
	private final OpenApiExtensionResolver openApiExtensionResolver;

    @Autowired
    public SwaggerConfig(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
    	 List<Parameter> pars = new ArrayList<>();
         ParameterBuilder Par = new ParameterBuilder();
         Par.name("access_token").description("access_token").modelRef(new ModelRef("string")).parameterType("header").required(false).defaultValue("").build();
         pars.add(Par.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("1.0.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.santech.module.biz.controller"))
                .paths(PathSelectors.any())
                .build()
                .extensions(openApiExtensionResolver.buildExtensions("1.0.0"))
                .globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("灾后调查评估系统")//页面标题
                .description("灾后调查评估系统API")//描述
                .version("1.0")//版本号
                .build();
    }

}
