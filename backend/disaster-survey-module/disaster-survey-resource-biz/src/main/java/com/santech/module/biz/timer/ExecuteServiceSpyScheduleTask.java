package com.santech.module.biz.timer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.santech.module.biz.service.ServiceResourceService;

/**
 * @Title: ExecuteSyncScheduleTask.java 
 * @Description：数据服务状态巡检定时任务处理类 
 * @Version： V1.0
 * @Modify：   
 */
@Component
public class ExecuteServiceSpyScheduleTask {
	public static final Logger logger = LoggerFactory.getLogger(ExecuteServiceSpyScheduleTask.class);
	
	@Resource
    private ServiceResourceService serviceResourceService;
	
    /**
     * 
     * 定时拉取数据服务记录，进行访问测试
     * 
     */
    @Scheduled(cron = "0 10 0 * * ?")//巡检周期(每日0点10分时(cron = "0 10 0 * * ?")每秒(cron = "1 * * * * ?");每分钟(cron = "0 0/1 * * * ?");每小时(cron = "0 0 * * * ?"))
    public void executeDataSync() {
        
        try {
        	serviceResourceService.updateMapServiceStatusInfo();
		} catch (Exception e) {
			logger.error("数据服务状态巡检的定时任务出现异常，详情",e);
		}
    }
    
    
}
