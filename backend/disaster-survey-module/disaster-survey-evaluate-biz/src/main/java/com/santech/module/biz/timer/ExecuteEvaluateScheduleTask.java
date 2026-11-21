package com.santech.module.biz.timer;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.santech.core.framework.dao.AssessmentRecordDao;
import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.engine.ExternalModelExecutor;

/**
 * 异步评估任务状态轮询。
 * 仅处理 T_BIZ_ASSESSMENT_RECORD 中 compute_mode='ASYNC' AND status='RUNNING' 的记录。
 * 默认不启用调度注解；需要后台轮询时取消注释 @EnableScheduling / @Scheduled。
 */
@Component
//@EnableScheduling
public class ExecuteEvaluateScheduleTask {

	private static final Logger logger = LoggerFactory.getLogger(ExecuteEvaluateScheduleTask.class);

	@Resource
	private AssessmentRecordDao assessmentRecordDao;
	@Resource
	private ExternalModelExecutor externalModelExecutor;

	/**
	 * 定时轮询外部异步模型运行中的评估记录。
	 */
	//@Scheduled(cron = "0/30 * * * * ?")
	public void pollAsyncAssessmentRecords() {
		try {
			logger.info("开始轮询 ASYNC RUNNING 评估记录");
			List<BizAssessmentRecordModel> running = assessmentRecordDao.listAsyncRunning();
			if (running == null || running.isEmpty()) {
				logger.debug("无运行中的异步评估任务");
				return;
			}
			int limit = Math.min(running.size(), 5);
			for (int i = 0; i < limit; i++) {
				BizAssessmentRecordModel row = running.get(i);
				ExecuteResult<String> result = externalModelExecutor.pollAssessment(row.getId());
				if ("F".equals(result.getResultCode())) {
					logger.error("评估记录[{}]轮询失败：{}", row.getId(), result.getResultMsg());
				} else {
					logger.info("评估记录[{}]轮询结果：{}", row.getId(), result.getResultMsg());
				}
			}
		} catch (Exception e) {
			logger.error("轮询异步评估任务异常", e);
		}
	}
}
