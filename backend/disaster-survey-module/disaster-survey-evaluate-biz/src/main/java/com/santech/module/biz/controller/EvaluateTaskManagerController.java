package com.santech.module.biz.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.EvaluateHistoryQueryDto;
import com.santech.module.biz.entity.dto.EvaluateHistorySaveParamsDto;
import com.santech.module.biz.entity.dto.EvaluateHistoryUpdateParamsDto;
import com.santech.module.biz.service.EvaluateTaskService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: EvaluateTaskManagerController.java
 * @Description:灾害评估运行任务相关功能接口
 * @version V1.0
 */
@RestController
@RequestMapping(value = "/web-disaster-evaluate/web/disaster/evaluate/task")
@Api(tags = "Evaluate-灾害评估运行任务管理相关功能接口")
public class EvaluateTaskManagerController {

    private static final  Logger logger = LoggerFactory.getLogger(EvaluateTaskManagerController.class);

    @Resource
    private EvaluateTaskService evaluateTaskService;

    /**
  	 * @Title exceptionHandler
  	 * @Description (用来捕获EvaluateTaskManagerController接口程序中未能预见的异常信息，保证前端响应结果的一致性，避免无响应)
  	 * @param e(Exception,所有异常的父类异常)
  	 * @return (接口执行情况)
  	 */
  	@ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
      	logger.error("EvaluateTaskManagerController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
  	/**
	  * @Title queryEvaluateHistoryInfosWithPage 
	  * @Description (此为根据筛选条件进行批量分页查询灾害评估运行任务信息接口。该方法可以通过传入分页参数信息进行分页查询。
	  * 查询的记录默认按记录的创建时间降序返回。)
	  * @param query (EvaluateHistoryQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的灾害评估运行任务灾害评估运行任务信息)
	  */
	@PostMapping("/queryWithPage")
	@ApiOperation(value = "根据筛选条件进行批量分页查询灾害评估运行任务信息接口", notes = "{字段说明【\n" +
   		"】")
	public ExecuteResult<List<FxEvaluateHistoryModel>> queryEvaluateHistoryInfosWithPage(@RequestBody EvaluateHistoryQueryDto query) {
		
		return evaluateTaskService.queryEvaluateHistoryInfosWithPage(query);
	}
	
	/**
     * @Title queryEvaluateHistoryDetailInfoById 
     * @Description (根据灾害评估运行任务id获取记录及结果详情信息接口)
     * @param id (灾害评估运行任务ID)
     * @return (接口的执行情况)
     */
	@GetMapping("/queryDetail")
	@ApiOperation(value = "根据灾害评估运行任务id获取详情信息接口", notes = "{字段说明【\n" +
			"id(必填,灾害评估运行任务ID)  \n"+
  		"】")
	public ExecuteResult<FxEvaluateHistoryModel> queryEvaluateHistoryDetailInfoById(@RequestParam("id") String id){
		
		return evaluateTaskService.queryEvaluateHistoryDetailInfoById(id);
	}
	
	/**
	  * @Title saveModelOperateHistoryInfo 
	  * @Description (此为灾害评估运行任务新建灾害评估运行任务信息保存接口，用于处理初次运行的业务逻辑。)
	  * @param model (EvaluateHistorySaveParamsDto,灾害评估运行任务要保存的参数信息)
	  * @return (接口的执行情况)
	  */
	@LogSystem(target = "新建灾害评估运行任务", type = Constants.LOG_TYPE_ADD)
	@PostMapping("/build")
	@ApiOperation(value = "新建灾害评估运行任务接口", notes = "{字段说明【\n" +
   		"】")
	public ExecuteResult<String> buildEvaluateTaskInfo(@RequestBody EvaluateHistorySaveParamsDto model) {
		
		return evaluateTaskService.buildEvaluateTaskInfo(model);
	}
	
	/**
	  * @Title deleteEvaluateTaskInfo 
	  * @Description (删除或终止灾害评估运行任务接口，当运行中或运行失败后，用户发现运行灾害评估运行任务有误或者不想要该灾害评估运行任务时，
	  * 调用此处逻辑对该灾害评估运行任务以及该灾害评估运行任务产生的任务文件进行删除)
	  * @param id (灾害评估运行任务灾害评估运行任务ID)
	  * @return (接口的执行情况)
	  */
	@LogSystem(target = "灾害评估运行任务删除", type = Constants.LOG_TYPE_DELETE)
	@GetMapping("/delete")
	@ApiOperation(value = "删除或终止灾害评估运行任务接口", notes = "{字段说明【\n" +
			"id(必填,灾害评估运行任务ID)  \n"+
   		"】")
	public ExecuteResult<String> deleteEvaluateTaskInfo(@RequestParam("id") String id) {
		
		return evaluateTaskService.deleteEvaluateTaskInfo(id);
	}
	
	/**
     * @Title queryEvaluateTaskProgressInfo 
     * @Description (根据灾害评估运行任务ID查询灾害评估运行任务的执行进度信息接口)
     * @param id (灾害评估运行任务ID)
     * @return (接口的执行情况)
     */
	@GetMapping("/progress/query")
	@ApiOperation(value = "根据任务id查询灾害评估运行任务的执行进度信息接口", notes = "{字段说明【\n" +
			"taskId(必填,灾害评估运行任务ID)  \n"+
  		"】")
	public ExecuteResult<Map<String,Object>> queryEvaluateTaskProgressInfo(@RequestParam("taskId") String taskId){
		
		return evaluateTaskService.queryEvaluateTaskProgressInfo(taskId);
	}

	@LogSystem(target = "更新灾害评估运行任务", type = Constants.LOG_TYPE_UPDATE)
	@PostMapping("/update")
	@ApiOperation(value = "更新灾害评估运行任务结果/状态接口")
	public ExecuteResult<String> updateEvaluateTaskInfo(@RequestBody EvaluateHistoryUpdateParamsDto dto) {
		return evaluateTaskService.updateEvaluateTaskInfo(dto);
	}

}
