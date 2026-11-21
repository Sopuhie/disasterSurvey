package com.santech.module.biz.engine;

import com.santech.framework.global.ExecuteResult;

/**
 * 外部重型遥感/SAR 模型异步执行器。
 * 仅负责启动外部计算进程与轮询状态，不承载业务评估编排。
 */
public interface ExternalModelExecutor {

	/**
	 * 提交评估记录对应的外部异步模型任务。
	 * @param assessmentId T_BIZ_ASSESSMENT_RECORD.id
	 */
	ExecuteResult<String> submitAssessment(String assessmentId);

	/**
	 * 轮询并刷新一条 ASYNC+RUNNING 评估记录状态。
	 */
	ExecuteResult<String> pollAssessment(String assessmentId);

	/**
	 * 兼容旧调查历史任务启动（FxSurveyHistory）。
	 */
	ExecuteResult<String> exeDisasterSurveyTask(String taskId);

	/**
	 * 兼容旧评估历史任务启动（FxEvaluateHistory）。
	 */
	ExecuteResult<String> exeEvaluateTask(String taskId);
}
