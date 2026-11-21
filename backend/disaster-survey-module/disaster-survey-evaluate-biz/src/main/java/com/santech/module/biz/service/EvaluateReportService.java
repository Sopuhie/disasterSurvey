package com.santech.module.biz.service;

import com.santech.framework.global.ExecuteResult;

/**
 * @Title: EvaluateReportService.java
 * @Description：评价报告业务层接口类
 * @Version： V1.0
 * @Modify：   
 */
public interface EvaluateReportService {
	
	
	/**
	  * @Title generateDisasterEvaluateReport 
	  * @Description (此为生成灾害评估报告接口的业务层接口方法)
	  * @param evaluateId (评价id)
	  * @param exposureType (预留)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> generateDisasterEvaluateReport(String evaluateId,String exposureType);
	
	
    
}
