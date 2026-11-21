package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import com.santech.core.framework.entity.model.FxSurveyHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskQueryDto;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskSaveParamsDto;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskUpdateParamsDto;

/**
 * @Title: DisasterSurveyTaskService.java 
 * @Description：灾情调查分析任务管理接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface DisasterSurveyTaskService {
	
	
	/**
	  * @Title queryDisasterSurveyTaskInfosWithPage 批量分页查询灾情调查分析任务信息
	  * @Description (此为根据筛选条件进行批量分页查询灾情调查分析任务信息接口的业务层方法，该方法会针对入参生成一条对应的查询sql，
	  * 以此sql进行对应灾情调查分析任务记录的查询并返回。该方法可以通过传入分页参数信息进行分页查询。查询的记录默认按记录的创建时间降序返回。)
	  * @param query (DisasterSurveyTaskQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的灾情调查分析任务信息)
	  */
	ExecuteResult<List<FxSurveyHistoryModel>> queryDisasterSurveyTaskInfosWithPage(DisasterSurveyTaskQueryDto query);
	
	/**
	  * @Title queryDisasterSurveyTaskDetailInfoById 
	  * @Description (根据任务ID查询历史记录以及包含的结果明细信息)
	  * @param taskId (任务ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<FxSurveyHistoryModel> queryDisasterSurveyTaskDetailInfoById(String taskId);
	
	/**
	  * @Title buildDisasterSurveyTaskInfo 保存灾情调查分析任务信息,用于处理初次运行的业务逻辑
	  * @Description (此为模型新建运行记录信息保存接口具体业务逻辑处理方法，该方法会针对接口入参进行日志输出及校验，
	  * 包括所选模型是否为完备模型，必填字段是否有值等，校验通过会调用接口执行当前或初始阶段的模型工具，并对灾情调查分析任务信息进行新增保存)
	  * @param model (DisasterSurveyTaskSaveParamsDto,灾情调查分析任务要保存的参数信息)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> buildDisasterSurveyTaskInfo(DisasterSurveyTaskSaveParamsDto model);
	
	
	/**
	  * @Title deleteDisasterSurveyTaskInfo 删除或终止评价任务记录
	  * @Description (当运行中或运行失败后，用户发现运行模型有误或者不想要该运行记录时，调用此处逻辑对该运行记录以及该运行记录产生的任务文件进行删除)
	  * @param id (灾情调查分析任务ID)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteDisasterSurveyTaskInfo(String id);
	
	
	/**
	  * @Title updateDisasterSurveyTaskInfo 
	  * @Description (此为评价任务更新接口的业务层方法)
	  * @param dto (DisasterSurveyTaskUpdateParamsDto,评价任务修改参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateDisasterSurveyTaskInfo(DisasterSurveyTaskUpdateParamsDto dto);
	
	/**
	  * @Title queryDisasterSurveyTaskProgressInfo 
	  * @Description (根据运行记录ID查询运行记录的执行进度信息)
	  * @param id (灾情调查分析任务ID)
	  * @return (接口的执行情况及进度信息)
	  */
	ExecuteResult<Map<String,Object>> queryDisasterSurveyTaskProgressInfo(String taskId);
	
}
