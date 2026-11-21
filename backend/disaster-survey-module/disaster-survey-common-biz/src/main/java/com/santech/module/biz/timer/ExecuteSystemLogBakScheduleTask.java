package com.santech.module.biz.timer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.santech.core.framework.service.LogService;
import com.santech.framework.global.ExecuteResult;

/**
 * @Title: ExecuteSystemLogBakScheduleTask.java 
 * @Description：系统日志备份定时任务处理类 
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
@Component
public class ExecuteSystemLogBakScheduleTask {
	public static final Logger logger = LoggerFactory.getLogger(ExecuteSystemLogBakScheduleTask.class);
	
	@Resource
	private LogService logService;
	
    @Scheduled(cron = "0 59 23 * * ?")//每天23点59分执行
    public void clearLogFile(){
        //定时备份系统日志文件数据
        try {
        	logger.info("系统日志备份任务，开始时间【{}】",System.currentTimeMillis());
        	ExecuteResult<String> redsult = logService.bakLogeInfo();
            logger.info("系统日志备份任务,完成时间【{}】及结果【{}】",System.currentTimeMillis(),redsult.getResultMsg());
        } catch (Exception e) {
        	logger.error("系统日志备份任务出现异常，异常详情：",e);
        }
    }
}
