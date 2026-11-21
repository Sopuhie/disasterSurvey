package com.santech.module.biz.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.engine.ExternalModelExecutor;
import com.santech.module.biz.service.EvaluateTaskExeService;

/**
 * 兼容旧接口：委托 {@link ExternalModelExecutor} 执行外部模型任务。
 * 新代码请直接注入 ExternalModelExecutor。
 */
@Service
public class EvaluateTaskExeServiceImpl implements EvaluateTaskExeService {

	@Resource
	private ExternalModelExecutor externalModelExecutor;

	@Override
	public ExecuteResult<String> exeDisasterSurveyTask(String taskId) {
		return externalModelExecutor.exeDisasterSurveyTask(taskId);
	}

	@Override
	public ExecuteResult<String> exeEvaluateTask(String taskId) {
		return externalModelExecutor.exeEvaluateTask(taskId);
	}
}
