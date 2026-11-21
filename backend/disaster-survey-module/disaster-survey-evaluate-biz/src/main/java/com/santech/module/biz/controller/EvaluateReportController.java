package com.santech.module.biz.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.service.EvaluateReportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Evaluate-评价报告相关功能接口" )
@RestController
@RequestMapping("/web-disaster-evaluate/web/disaster/evaluate/report")
public class EvaluateReportController {
	private static final  Logger logger = LoggerFactory.getLogger(EvaluateReportController.class);
    
	@Resource
    private EvaluateReportService evaluateReportService;

    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获EvaluateReportController接口程序中未能预见的异常信息，保证前端相应结果的一致性)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
   @ExceptionHandler(Exception.class)
   public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("EvaluateReportController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
   }
   
     /**
	  * @Title generateDangerRepairReport 
	  * @Description (此为灾害评估报告导出接口)
	  * @param evaluateId (评估任务id,参数)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "灾害评估报告导出", type = Constants.LOG_TYPE_EXPORT)
     @GetMapping("/export")
     @ApiOperation(value = "灾害评估报告导出接口",notes = "{字段说明【\n" +
             "evaluateId(评价id，必传)\n" +
             "】")
     public ExecuteResult<String> generateDisasterEvaluateReport(HttpServletRequest request,@RequestParam(value = "evaluateId") String evaluateId,
    		 @RequestParam(value = "exposureType",required = false) String exposureType){
     	
         return evaluateReportService.generateDisasterEvaluateReport(evaluateId,exposureType);
     }

}
