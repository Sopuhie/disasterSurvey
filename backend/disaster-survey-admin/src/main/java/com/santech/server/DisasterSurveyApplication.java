package com.santech.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 项目的启动类
 */
@ComponentScan({"santech","com.santech","com.github.xiaoymin.knife4j.spring","com.linewell","springfox.documentation"})
@EnableTransactionManagement
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DisasterSurveyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisasterSurveyApplication.class, args);
    }

}
