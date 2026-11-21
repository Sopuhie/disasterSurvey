package com.santech.module.biz.service;

import com.santech.framework.global.ExecuteResult;

/**
 * @Title: EvaluateTaskExeService.java 
 * @Description：任务执行相关接口类 
 * @Version： V1.0
 * @Modify：   
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
