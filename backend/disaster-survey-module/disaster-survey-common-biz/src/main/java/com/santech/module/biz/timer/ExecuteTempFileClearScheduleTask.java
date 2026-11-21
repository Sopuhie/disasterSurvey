package com.santech.module.biz.timer;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.service.FileHandlerService;

/**
 * @Title: ExecuteTempFileClearScheduleTask.java 
 * @Description：清理服务器（应用或文件服务器）临时文件定时任务处理类 
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
@Component
public class ExecuteTempFileClearScheduleTask {
	public static final Logger logger = LoggerFactory.getLogger(ExecuteTempFileClearScheduleTask.class);
	@Resource
	private FileBasicConfig fileBasicConfig;
	@Resource
	private FileHandlerService fileHandlerService;
	
    
    @Scheduled(cron = "0 0 0 1 * ?")//每月对日志文件做清空
    public void clearLogFile(){
    	String logpath = fileBasicConfig.getLogpath();
        //定时删除历史日志文件数据
    	logger.info("【灾后调查评估系统】开始删除历史日志文件数据，日志目录【{}】,开始时间【{}】",logpath,System.currentTimeMillis());
    	if(logpath!=null) {
    		String[] logpaths = logpath.split(",");
    		for(int i=0;i<logpaths.length;i++) {
    			String path = logpaths[i];
    			File file = new File(path);
    	        try {
    	        	deleteLogFile(file);
    	        } catch (Exception e) {
    	        	logger.error("【灾后调查评估系统】删除历史日志文件数据出现异常，删除日志目录【{}】,异常详情",path,e);
    	        }
    		}
    	}
        logger.info("【灾后调查评估系统】删除历史日志文件数据完成，日志目录【{}】,完成时间【{}】",logpath,System.currentTimeMillis());
    }
    //递归删除指定文件夹下的文件
    private void deleteLogFile(File file) throws IOException {
    	if(file.exists()&&file.isDirectory()) {
    		File[] files = file.listFiles();
            //如果包含文件进行删除操作
            if (files!=null&&files.length>0){
                for (int i = 0; i <files.length; i++) {
                    if (files[i].isFile()){
                        //删除目标日志子文件（除了当天的正在输出的日志文件）
                    	String filename = files[i].getName();
                    	if(filename!=null&&!"error.log".equals(filename)&&filename.endsWith(".log")) {
                    		files[i].delete();
                    	}
                    }else if (files[i].isDirectory()){
                        //通过递归的方法找到子目录的文件
                    	deleteLogFile(files[i]);
                    }
                }
            }
    	}
    }
}
