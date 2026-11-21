package com.santech.module.biz.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.EvaluateTaskManagerDao;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.EvaluateReportService;

/**
 * 灾害评估报告导出。优先 Word 模板；模板缺失时回落 JSON 报告文件，保证前端可下载。
 */
@Service
public class EvaluateReportServiceImpl implements EvaluateReportService {
	private static final Logger logger = LoggerFactory.getLogger(EvaluateReportServiceImpl.class);

	@Resource
	private FileBasicConfig fileBasicConfig;
	@Resource
	private EvaluateTaskManagerDao evaluateTaskManagerDao;

	@Override
	public ExecuteResult<String> generateDisasterEvaluateReport(String evaluateId, String exposureType) {
		try {
			if (StringUtil.isEmpty(evaluateId)) {
				return ExecuteResult.error("灾害评估任务ID不能为空", null);
			}
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.put("taskId", evaluateId);
			FxEvaluateHistoryModel task = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class, "ID=:taskId",
					paramsMap);
			if (task == null) {
				return ExecuteResult.error("未找到对应评估任务", null);
			}

			Map<String, Object> datas = genarateDisasterEvaluateReportParams(task, exposureType);
			String reportTempPath = getFileRootPath(fileBasicConfig) + "template" + File.separator + "灾害评估报告模板.docx";
			String relativePath;
			File templateFile = new File(reportTempPath);
			if (templateFile.exists()) {
				try {
					LoopRowTableRenderPolicy policy = new LoopRowTableRenderPolicy();
					Configure config = Configure.builder().bind("dangerList", policy).build();
					XWPFTemplate template = XWPFTemplate.compile(reportTempPath, config).render(datas);
					relativePath = "evaluate_report" + File.separator + evaluateId + File.separator + "灾害评估报告.docx";
					String resultFile = getFileRootPath(fileBasicConfig) + relativePath;
					File resultFileF = new File(resultFile);
					if (!resultFileF.getParentFile().exists()) {
						resultFileF.getParentFile().mkdirs();
					}
					template.writeAndClose(new FileOutputStream(resultFile));
				} catch (Exception ex) {
					logger.warn("Word 模板渲染失败，回落 JSON 报告：{}", ex.getMessage());
					relativePath = writeJsonReport(evaluateId, datas);
				}
			} else {
				logger.warn("未找到灾害评估报告模板，回落 JSON 报告文件");
				relativePath = writeJsonReport(evaluateId, datas);
			}

			try {
				task.setReportPath(relativePath);
				task.setZhgxsj(new java.util.Date());
				evaluateTaskManagerDao.updateEvaluateTaskInfoByModel(task);
			} catch (Exception e) {
				logger.warn("回写报告路径失败：{}", e.getMessage());
			}
			return ExecuteResult.success("导出成功", relativePath);
		} catch (Exception e) {
			logger.error("导出灾害评估报告【{}】出现异常，异常详情：", evaluateId, e);
			return ExecuteResult.error("导出异常:" + e.getMessage(), null);
		}
	}

	private String writeJsonReport(String evaluateId, Map<String, Object> datas) throws Exception {
		String relativePath = "evaluate_report" + File.separator + evaluateId + File.separator + "灾害评估报告.json";
		String resultFile = getFileRootPath(fileBasicConfig) + relativePath;
		File resultFileF = new File(resultFile);
		if (!resultFileF.getParentFile().exists()) {
			resultFileF.getParentFile().mkdirs();
		}
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(resultFileF), StandardCharsets.UTF_8)) {
			writer.write(JSON.toJSONString(datas, true));
		}
		return relativePath;
	}

	private Map<String, Object> genarateDisasterEvaluateReportParams(FxEvaluateHistoryModel task, String exposureType) {
		Map<String, Object> datas = new HashMap<String, Object>();
		datas.put("evaluateId", task.getId());
		datas.put("name", task.getName());
		datas.put("disasterType", task.getDisasterType());
		datas.put("targetEntity", task.getTargetEntity());
		datas.put("exposureType", exposureType);
		datas.put("factorJgss", task.getFactorJgss());
		datas.put("factorYjsy", task.getFactorYjsy());
		datas.put("factorBzzb", task.getFactorBzzb());
		datas.put("recommend", task.getRecommend());
		datas.put("status", task.getStatus());
		datas.put("cjr", task.getCjr());
		datas.put("cjsj", task.getCjsj());
		if (StringUtil.isNotEmpty(task.getReportData())) {
			try {
				datas.put("reportData", JSON.parse(task.getReportData()));
			} catch (Exception e) {
				datas.put("reportData", task.getReportData());
			}
		}
		if (StringUtil.isNotEmpty(task.getResultData())) {
			try {
				datas.put("resultData", JSON.parse(task.getResultData()));
			} catch (Exception e) {
				datas.put("resultData", task.getResultData());
			}
		}
		datas.put("title", task.getName() != null ? task.getName() : "灾害评估报告");
		datas.put("summary", "本报告由灾害评估任务 " + task.getId() + " 生成。");
		return datas;
	}

	private String getFileRootPath(FileBasicConfig commonConfig) {
		String names = System.getProperty("os.name");
		if (names != null && names.toLowerCase().startsWith("win")) {
			return commonConfig.getRootForWin();
		}
		return commonConfig.getRootForLiunx();
	}
}
