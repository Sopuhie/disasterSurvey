package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.EvaluateHistoryQueryDto;
import com.santech.module.biz.entity.dto.EvaluateHistorySaveParamsDto;
import com.santech.module.biz.entity.dto.EvaluateHistoryUpdateParamsDto;

/**
 * @Title: EvaluateTaskService.java 
 * @Description：灾害评估分析任务管理接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface EvaluateTaskService {
	
	
	/**
	  * @Title queryEvaluateHistoryInfosWithPage 批量分页查询模型运行记录信息
	  * @Description (此为根据筛选条件进行批量分页查询模型运行记录信息接口的业务层方法，该方法会针对入参生成一条对应的查询sql，
	  * 以此sql进行对应模型运行记录记录的查询并返回。该方法可以通过传入分页参数信息进行分页查询。查询的记录默认按记录的创建时间降序返回。)
	  * @param query (EvaluateHistoryQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的模型运行记录信息)
	  */
	ExecuteResult<List<FxEvaluateHistoryModel>> queryEvaluateHistoryInfosWithPage(EvaluateHistoryQueryDto query);
	
	/**
	  * @Title queryEvaluateHistoryDetailInfoById 
	  * @Description (根据任务ID查询历史记录以及包含的结果明细信息)
	  * @param taskId (任务ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<FxEvaluateHistoryModel> queryEvaluateHistoryDetailInfoById(String taskId);
	
	/**
	  * @Title buildEvaluateTaskInfo 保存模型运行记录信息,用于处理初次运行的业务逻辑
	  * @Description (此为模型新建运行记录信息保存接口具体业务逻辑处理方法，该方法会针对接口入参进行日志输出及校验，
	  * 包括所选模型是否为完备模型，必填字段是否有值等，校验通过会调用接口执行当前或初始阶段的模型工具，并对模型运行记录信息进行新增保存)
	  * @param model (EvaluateHistorySaveParamsDto,模型运行记录要保存的参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> buildEvaluateTaskInfo(EvaluateHistorySaveParamsDto model);
	
	
	/**
	  * @Title deleteEvaluateTaskInfo 删除或终止评价任务记录
	  * @Description (当运行中或运行失败后，用户发现运行模型有误或者不想要该运行记录时，调用此处逻辑对该运行记录以及该运行记录产生的任务文件进行删除)
	  * @param id (模型运行记录ID)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteEvaluateTaskInfo(String id);
	
	
	/**
	  * @Title updateEvaluateTaskInfo 
	  * @Description (此为评价任务更新接口的业务层方法)
	  * @param dto (EvaluateHistoryUpdateParamsDto,评价任务修改参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateEvaluateTaskInfo(EvaluateHistoryUpdateParamsDto dto);
	
	/**
	  * @Title queryEvaluateTaskProgressInfo 
	  * @Description (根据运行记录ID查询运行记录的执行进度信息)
	  * @param id (模型运行记录ID)
	  * @return (接口的执行情况及进度信息)
	  */
	ExecuteResult<Map<String,Object>> queryEvaluateTaskProgressInfo(String taskId);
	
}
