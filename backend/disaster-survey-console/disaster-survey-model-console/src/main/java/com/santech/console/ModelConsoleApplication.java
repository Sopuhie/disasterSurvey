package com.santech.console;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 此模块用来运行工具包，如质检服务，TODO
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(value = {"santech","com.santech"},excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE))
public class ModelConsoleApplication implements CommandLineRunner {
	private static ConfigurableApplicationContext context;
	
//    @Autowired
//    QCTaskExeService qcTaskExeService;
    
    @Override
    public void run(String... args) throws Exception {
    	//qcTaskExeService.exeQcTask();
    }

    public static void main(String[] args) {
    	try {
    		// 执行初始化任务...
    		context = SpringApplication.run(ModelConsoleApplication.class, args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			if(context!=null) {
				context.close(); // 不论异常与否都要主动关闭应用上下文
			}
		}
    }

}
