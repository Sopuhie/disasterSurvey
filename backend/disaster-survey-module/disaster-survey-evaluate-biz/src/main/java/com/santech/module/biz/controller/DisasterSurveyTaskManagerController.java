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
import com.santech.core.framework.entity.model.FxSurveyHistoryModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskQueryDto;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskSaveParamsDto;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskUpdateParamsDto;
import com.santech.module.biz.service.DisasterSurveyTaskService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: DisasterSurveyTaskManagerController.java
 * @Description:评价灾情调查分析任务相关功能接口
 * @version V1.0
 */
@RestController
@RequestMapping(value = "/web-disaster-evaluate/web/disaster/evaluate/survey/task")
@Api(tags = "Evaluate-灾情调查分析任务管理相关功能接口")
public class DisasterSurveyTaskManagerController {

    private static final  Logger logger = LoggerFactory.getLogger(DisasterSurveyTaskManagerController.class);

    @Resource
    private DisasterSurveyTaskService disasterSurveyTaskService;

    /**
  	 * @Title exceptionHandler
  	 * @Description (用来捕获DisasterSurveyTaskManagerController接口程序中未能预见的异常信息，保证前端响应结果的一致性，避免无响应)
  	 * @param e(Exception,所有异常的父类异常)
  	 * @return (接口执行情况)
  	 */
  	@ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
      	logger.error("DisasterSurveyTaskManagerController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
  	/**
	  * @Title queryDisasterSurveyTaskInfosWithPage 
	  * @Description (此为根据筛选条件进行批量分页查询灾情调查分析任务信息接口。该方法可以通过传入分页参数信息进行分页查询。
	  * 查询的记录默认按记录的创建时间降序返回。)
	  * @param query (DisasterSurveyTaskQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的灾情调查分析任务信息)
	  */
	@PostMapping("/queryWithPage")
	@ApiOperation(value = "根据筛选条件进行批量分页查询灾情调查分析任务信息接口", notes = "{字段说明【\n" +
   		"】")
	public ExecuteResult<List<FxSurveyHistoryModel>> queryDisasterSurveyTaskInfosWithPage(@RequestBody DisasterSurveyTaskQueryDto query) {
		
		return disasterSurveyTaskService.queryDisasterSurveyTaskInfosWithPage(query);
	}
	
	/**
     * @Title queryDisasterSurveyTaskDetailInfoById 
     * @Description (根据灾情调查分析任务id获取记录及结果详情信息接口)
     * @param id (灾情调查分析任务ID)
     * @return (接口的执行情况)
     */
	@GetMapping("/queryDetail")
	@ApiOperation(value = "根据灾情调查分析任务id获取任务详情信息接口", notes = "{字段说明【\n" +
			"id(必填,灾情调查分析任务ID)  \n"+
  		"】")
	public ExecuteResult<FxSurveyHistoryModel> queryDisasterSurveyTaskDetailInfoById(@RequestParam("id") String id){
		
		return disasterSurveyTaskService.queryDisasterSurveyTaskDetailInfoById(id);
	}
	
	/**
	  * @Title saveModelOperateHistoryInfo 
	  * @Description (此为新建灾情调查分析任务接口，用于处理初次运行的业务逻辑。)
	  * @param model (DisasterSurveyTaskSaveParamsDto,灾情调查分析任务要保存的参数信息)
	  * @return (接口的执行情况)
	  */
	@LogSystem(target = "新建灾情调查分析任务", type = Constants.LOG_TYPE_ADD)
	@PostMapping("/build")
	@ApiOperation(value = "新建灾情调查分析任务接口", notes = "{字段说明【\n" +
   		"】")
	public ExecuteResult<String> buildDisasterSurveyTaskInfo(@RequestBody DisasterSurveyTaskSaveParamsDto model) {
		
		return disasterSurveyTaskService.buildDisasterSurveyTaskInfo(model);
	}
	
	/**
	  * @Title deleteDisasterSurveyTaskInfo 
	  * @Description (删除或终止灾情调查分析任务接口，当运行中或运行失败后，用户发现运行灾情调查分析任务有误或者不想要该灾情调查分析任务时，
	  * 调用此处逻辑对该灾情调查分析任务以及该灾情调查分析任务产生的任务文件进行删除)
	  * @param id (灾情调查分析任务ID)
	  * @return (接口的执行情况)
	  */
	@LogSystem(target = "灾情调查分析任务删除", type = Constants.LOG_TYPE_DELETE)
	@GetMapping("/delete")
	@ApiOperation(value = "删除或终止运行灾情调查分析任务接口", notes = "{字段说明【\n" +
			"id(必填,灾情调查分析任务ID)  \n"+
   		"】")
	public ExecuteResult<String> deleteDisasterSurveyTaskInfo(@RequestParam("id") String id) {
		
		return disasterSurveyTaskService.deleteDisasterSurveyTaskInfo(id);
	}
	
	/**
     * @Title queryDisasterSurveyTaskProgressInfo 
     * @Description (根据灾情调查分析任务ID查询灾情调查分析任务的执行进度信息接口)
     * @param id (灾情调查分析任务ID)
     * @return (接口的执行情况)
     */
	@GetMapping("/progress/query")
	@ApiOperation(value = "根据任务id查询灾情调查分析任务的执行进度信息接口", notes = "{字段说明【\n" +
			"taskId(必填,灾情调查分析任务ID)  \n"+
  		"】")
	public ExecuteResult<Map<String,Object>> queryDisasterSurveyTaskProgressInfo(@RequestParam("taskId") String taskId){
		
		return disasterSurveyTaskService.queryDisasterSurveyTaskProgressInfo(taskId);
	}

	@LogSystem(target = "更新灾情调查分析任务", type = Constants.LOG_TYPE_UPDATE)
	@PostMapping("/update")
	@ApiOperation(value = "更新灾情调查分析任务结果/状态接口")
	public ExecuteResult<String> updateDisasterSurveyTaskInfo(@RequestBody DisasterSurveyTaskUpdateParamsDto dto) {
		return disasterSurveyTaskService.updateDisasterSurveyTaskInfo(dto);
	}

}
