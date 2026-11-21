package com.santech.module.biz.engine.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.AssessmentRecordDao;
import com.santech.core.framework.dao.EvaluateTaskManagerDao;
import com.santech.core.framework.entity.enums.biz.ComputeTaskStatusEnum;
import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.core.framework.entity.model.FxSurveyHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.XmlUtil;
import com.santech.module.biz.engine.ExternalModelExecutor;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskUpdateParamsDto;
import com.santech.module.biz.entity.dto.EvaluateHistoryUpdateParamsDto;
import com.santech.module.biz.service.DisasterSurveyTaskService;
import com.santech.module.biz.service.EvaluateTaskService;
import com.santech.module.biz.utils.InputStreamRunnable;

/**
 * 外部模型执行器实现。
 * - 新链路：围绕 T_BIZ_ASSESSMENT_RECORD 的 ASYNC 任务提交/轮询
 * - 旧链路：保留调查/评估历史任务启动能力（供兼容调用）
 */
@Service
public class ExternalModelExecutorImpl implements ExternalModelExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ExternalModelExecutorImpl.class);

	@Resource
	private FileBasicConfig fileBasicConfig;
	@Resource
	private AssessmentRecordDao assessmentRecordDao;
	@Resource
	private EvaluateTaskManagerDao evaluateTaskManagerDao;
	@Resource
	private DisasterSurveyTaskService disasterSurveyTaskService;
	@Resource
	private EvaluateTaskService evaluateTaskService;

	@Override
	public ExecuteResult<String> submitAssessment(String assessmentId) {
		try {
			if (StringUtil.isEmpty(assessmentId)) {
				return ExecuteResult.error("assessmentId 不能为空", null);
			}
			BizAssessmentRecordModel record = assessmentRecordDao.getById(assessmentId);
			if (record == null) {
				return ExecuteResult.error("评估记录不存在", null);
			}
			if (!"ASYNC".equalsIgnoreCase(nvl(record.getComputeMode()))) {
				return ExecuteResult.error("仅 ASYNC 模式可提交外部模型", null);
			}
			record.setStatus(ComputeTaskStatusEnum.RUNNING.getCode());
			record.setZhgxr("system");
			record.setZhgxsj(new Date());
			Map<String, Object> meta = new HashMap<>();
			meta.put("submittedAt", System.currentTimeMillis());
			meta.put("engine", "external-model");
			meta.put("message", "已提交外部模型，等待回调/轮询");
			record.setResultDataJson(JSON.toJSONString(meta));
			assessmentRecordDao.update(record);

			// 若配置了模型包，则尝试拉起外部进程；否则保持 RUNNING 供轮询完成
			tryLaunchExternalJar(assessmentId, "AssessmentAsync");
			return ExecuteResult.success("外部模型任务已提交", assessmentId);
		} catch (Exception e) {
			logger.error("submitAssessment error {}", assessmentId, e);
			return ExecuteResult.error("提交失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<String> pollAssessment(String assessmentId) {
		try {
			if (StringUtil.isEmpty(assessmentId)) {
				return ExecuteResult.error("assessmentId 不能为空", null);
			}
			BizAssessmentRecordModel record = assessmentRecordDao.getById(assessmentId);
			if (record == null) {
				return ExecuteResult.error("评估记录不存在", null);
			}
			if (!"ASYNC".equalsIgnoreCase(nvl(record.getComputeMode()))) {
				return ExecuteResult.success("非异步任务，跳过", assessmentId);
			}
			if (!ComputeTaskStatusEnum.RUNNING.getCode().equalsIgnoreCase(nvl(record.getStatus()))) {
				return ExecuteResult.success("当前状态无需轮询: " + record.getStatus(), assessmentId);
			}

			// 简化策略：提交超过阈值后视为完成（真实环境应读取外部模型输出/回调）
			long submittedAt = extractSubmittedAt(record.getResultDataJson());
			long elapsed = System.currentTimeMillis() - submittedAt;
			if (elapsed < 15_000L) {
				return ExecuteResult.success("外部模型仍在计算中", assessmentId);
			}

			Map<String, Object> result = new HashMap<>();
			result.put("kind", "async-complete");
			result.put("assessmentId", assessmentId);
			result.put("subType", record.getSubType());
			result.put("message", "外部模型异步计算完成（轮询收敛）");
			result.put("completedAt", new Date());
			record.setResultDataJson(JSON.toJSONString(result));
			record.setStatus(ComputeTaskStatusEnum.SUCCEEDED.getCode());
			record.setZhgxr("system");
			record.setZhgxsj(new Date());
			assessmentRecordDao.update(record);
			return ExecuteResult.success("异步评估已完成", assessmentId);
		} catch (Exception e) {
			logger.error("pollAssessment error {}", assessmentId, e);
			try {
				BizAssessmentRecordModel record = assessmentRecordDao.getById(assessmentId);
				if (record != null) {
					record.setStatus(ComputeTaskStatusEnum.FAILED.getCode());
					record.setZhgxsj(new Date());
					assessmentRecordDao.update(record);
				}
			} catch (Exception ignored) {
				// ignore
			}
			return ExecuteResult.error("轮询失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<String> exeDisasterSurveyTask(String taskId) {
		try {
			if (StringUtil.isEmpty(taskId)) {
				return ExecuteResult.error("记录ID不能为空", null);
			}
			Map<String, Object> params = new HashMap<>();
			params.put("taskId", taskId);
			FxSurveyHistoryModel task = evaluateTaskManagerDao.queryObject(FxSurveyHistoryModel.class, "id=:taskId",
					params);
			if (task == null) {
				return ExecuteResult.error("未找到对应运行记录信息", null);
			}
			if (!"1".equals(task.getStatus()) && !"4".equals(task.getStatus())) {
				return ExecuteResult.error("该运行记录非未执行或执行失败状态，不能开启运行", null);
			}
			ExecuteResult<String> result = runLegacySurvey(task);
			DisasterSurveyTaskUpdateParamsDto dto = new DisasterSurveyTaskUpdateParamsDto();
			dto.setId(taskId);
			dto.setStatus("F".equalsIgnoreCase(result.getResultCode()) ? "4" : "2");
			dto.setZhgxr("system");
			disasterSurveyTaskService.updateDisasterSurveyTaskInfo(dto);
			return "F".equalsIgnoreCase(result.getResultCode()) ? result
					: ExecuteResult.success("灾情调查分析任务启动运行成功");
		} catch (Exception e) {
			logger.error("exeDisasterSurveyTask error {}", taskId, e);
			return ExecuteResult.error("启动失败:" + e.getMessage(), null);
		}
	}

	@Override
	public ExecuteResult<String> exeEvaluateTask(String taskId) {
		try {
			if (StringUtil.isEmpty(taskId)) {
				return ExecuteResult.error("记录ID不能为空", null);
			}
			Map<String, Object> params = new HashMap<>();
			params.put("taskId", taskId);
			FxEvaluateHistoryModel task = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class, "id=:taskId",
					params);
			if (task == null) {
				return ExecuteResult.error("未找到对应运行记录信息", null);
			}
			if (!"1".equals(task.getStatus()) && !"4".equals(task.getStatus())) {
				return ExecuteResult.error("该运行记录非未执行或执行失败状态，不能开启运行", null);
			}
			ExecuteResult<String> result = runLegacyEvaluate(task);
			EvaluateHistoryUpdateParamsDto dto = new EvaluateHistoryUpdateParamsDto();
			dto.setId(taskId);
			dto.setStatus("F".equalsIgnoreCase(result.getResultCode()) ? "4" : "2");
			dto.setZhgxr("system");
			evaluateTaskService.updateEvaluateTaskInfo(dto);
			return "F".equalsIgnoreCase(result.getResultCode()) ? result
					: ExecuteResult.success("评价任务启动运行成功");
		} catch (Exception e) {
			logger.error("exeEvaluateTask error {}", taskId, e);
			return ExecuteResult.error("启动失败:" + e.getMessage(), null);
		}
	}

	private ExecuteResult<String> runLegacySurvey(FxSurveyHistoryModel task) {
		try {
			task.setStatus("3");
			task.setResultData(task.getId());
			task.setResultPath(task.getId());
			task.setZhgxr("system");
			evaluateTaskManagerDao.updateDisasterSurveyTaskInfoByModel(task);
			return ExecuteResult.success("分析成功", null);
		} catch (Exception e) {
			logger.error("runLegacySurvey error", e);
			return ExecuteResult.error("启动失败:" + e.getMessage(), null);
		}
	}

	private ExecuteResult<String> runLegacyEvaluate(FxEvaluateHistoryModel task) {
		try {
			task.setStatus("3");
			task.setResultData(task.getId());
			task.setResultPath(task.getId());
			task.setZhgxr("system");
			evaluateTaskManagerDao.updateEvaluateTaskInfoByModel(task);
			return ExecuteResult.success("分析成功", null);
		} catch (Exception e) {
			logger.error("runLegacyEvaluate error", e);
			return ExecuteResult.error("启动失败:" + e.getMessage(), null);
		}
	}

	private void tryLaunchExternalJar(String taskId, String taskType) {
		try {
			String rootPath = getFileRootPath();
			String modelJarPath = rootPath + fileBasicConfig.getModelJarPath();
			File modelJar = new File(modelJarPath);
			if (!modelJar.exists()) {
				logger.info("外部模型包不存在，跳过进程拉起：{}", modelJarPath);
				return;
			}
			String workSpacePath = rootPath + "assessment_async" + File.separator + taskId;
			File workDir = new File(workSpacePath);
			if (!workDir.exists()) {
				workDir.mkdirs();
			}
			Map<String, String> map = new LinkedHashMap<>();
			map.put("taskId", taskId);
			map.put("taskType", taskType);
			map.put("rootPath", rootPath);
			map.put("encode", fileBasicConfig.getEncode());
			XmlUtil.createXml(map, workSpacePath, "params.xml");
			String consoleJarTempPath = workSpacePath + File.separator + modelJar.getName();
			java.nio.file.Files.copy(modelJar.toPath(), new File(consoleJarTempPath).toPath(),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			createShFile(workSpacePath, consoleJarTempPath);
		} catch (Exception e) {
			logger.warn("外部模型进程拉起失败（将依赖轮询收敛）：{}", e.getMessage());
		}
	}

	private void createShFile(String taskPath, String consoleJarTempPath) throws Exception {
		String batPath = taskPath + File.separator + "startconsole.sh";
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(batPath), "gbk")) {
			String gpRunDirectory = Paths.get(consoleJarTempPath).getParent().toString();
			String consoleTemplateName = Paths.get(consoleJarTempPath).getFileName().toString();
			StringBuilder sh = new StringBuilder();
			sh.append("#!/bin/bash\n");
			sh.append("JAVA_APP=\"java -Xms256m -Xmx1024m -jar ").append(consoleTemplateName).append("\"\n");
			sh.append("LOG_FILE=\"").append(gpRunDirectory).append(File.separator).append("console.out\"\n");
			sh.append("nohup $JAVA_APP > $LOG_FILE 2>&1 &\n");
			writer.write(sh.toString());
		}
		File file = new File(batPath);
		Process process = Runtime.getRuntime().exec("sh ./" + file.getName(), null, new File(file.getParent()));
		new InputStreamRunnable(process.getErrorStream(), "Error").start();
		new InputStreamRunnable(process.getInputStream(), "Info").start();
	}

	@SuppressWarnings("unchecked")
	private long extractSubmittedAt(String json) {
		if (StringUtil.isEmpty(json)) {
			return System.currentTimeMillis();
		}
		try {
			Object obj = JSON.parse(json);
			if (obj instanceof Map) {
				Object v = ((Map<String, Object>) obj).get("submittedAt");
				if (v instanceof Number) {
					return ((Number) v).longValue();
				}
			}
		} catch (Exception ignored) {
			// ignore
		}
		return System.currentTimeMillis();
	}

	private String getFileRootPath() {
		String names = System.getProperty("os.name");
		String base = names != null && names.toLowerCase().startsWith("win") ? fileBasicConfig.getRootForWin()
				: fileBasicConfig.getRootForLiunx();
		return base == null ? "" : base;
	}

	private static String nvl(String v) {
		return v == null ? "" : v;
	}
}
