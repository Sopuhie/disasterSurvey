package com.santech.core.framework.aop;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.santech.core.framework.entity.model.JcLogModel;
import com.santech.core.framework.service.LogService;
import com.santech.framework.utils.component.thread.ThreadCommand;
import com.santech.framework.utils.component.thread.ThreadPoolUtil;

/**
 * 异步任务
 */
@Component
public class AsyncTask {
	private static final Logger logger = LoggerFactory.getLogger(AsyncTask.class);
	
	
	 @Resource
	 private LogService logService;

	 
	 /**
	  * @Title saveSystemLog 
	  * @Description (系统日志记录方法)
	  * @param userId (用户ID)
	  * @param username (用户名)
	  * @param target (操作对象)
	  * @param type (操作类型，如增删改查登录)
	  * @param status (操作状态)
	  * @return (接口的执行情况或验证结果)
	  */
	 public void saveSystemLog(JcLogModel logModel ) {
		 ThreadCommand th = new ThreadCommand() {
			@Override
			public void execute() throws Exception {
				try {
					logService.saveLogeInfo(logModel);
				} catch (Exception e) {
					logger.info("字典管理微服务-系统日志记录异常，详情：",e);
				}
			}
		};
		ThreadPoolUtil.startTask(th);
	 }


}
