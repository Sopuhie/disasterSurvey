package com.santech.module.biz.service;

import com.santech.framework.global.ExecuteResult;

/**
 * 旧版任务执行接口（兼容保留）。
 * 新代码请使用 {@link com.santech.module.biz.engine.ExternalModelExecutor}。
 */
public interface EvaluateTaskExeService {
	
	/**
	  * @Title exeDisasterSurveyTask 
	  * @Description (此为灾情调查分析任务执行)
	  * @param taskId (任务ID)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> exeDisasterSurveyTask(String taskId);

	
	/**
	  * @Title exeEvaluateTask 
	  * @Description (此为灾害评估分析任务执行)
	  * @param taskId (任务ID)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> exeEvaluateTask(String taskId);
	
	
	
	
}
