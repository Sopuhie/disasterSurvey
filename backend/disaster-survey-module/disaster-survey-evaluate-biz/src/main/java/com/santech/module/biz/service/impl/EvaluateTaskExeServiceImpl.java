package com.santech.module.biz.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.EvaluateTaskManagerDao;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.core.framework.entity.model.FxSurveyHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.XmlUtil;
import com.santech.module.biz.entity.dto.DisasterSurveyTaskUpdateParamsDto;
import com.santech.module.biz.entity.dto.EvaluateHistoryUpdateParamsDto;
import com.santech.module.biz.service.DisasterSurveyTaskService;
import com.santech.module.biz.service.EvaluateTaskExeService;
import com.santech.module.biz.service.EvaluateTaskService;
import com.santech.module.biz.utils.InputStreamRunnable;


/**
 * @Title: EvaluateTaskExeServiceImpl.java 
 * @Description：水质插值评价模型任务执行接口实现类 
 * @Modify：   
 */
@Service
public class EvaluateTaskExeServiceImpl  implements EvaluateTaskExeService {
	
	private static final Logger logger = LoggerFactory.getLogger(EvaluateTaskExeServiceImpl.class);
	
	@Resource
 	private FileBasicConfig fileBasicConfig;
	
	@Resource
	private EvaluateTaskManagerDao evaluateTaskManagerDao;
	@Resource
	private DisasterSurveyTaskService disasterSurveyTaskService;
	@Resource
	private EvaluateTaskService evaluateTaskService;
	@Override
	public ExecuteResult<String> exeDisasterSurveyTask(String taskId) {
		try {
			//获取任务记录
			if(StringUtil.isEmpty(taskId) ) {
				return ExecuteResult.error("入参校验不通过，记录ID不能为空！",null);
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("taskId", taskId);
			FxSurveyHistoryModel task = evaluateTaskManagerDao.queryObject(FxSurveyHistoryModel.class, "id=:taskId", params);
			if(task==null) {
				return ExecuteResult.error("未找到对应运行记录信息", null);
			}
			//校验评价任务状态
			//运行总体状态，1任务创建，2运行中，3运行完成，4运行失败,5已归档
			if(!"1".equals(task.getStatus())&&!"4".equals(task.getStatus())) {
				return ExecuteResult.error("该运行记录非未执行或者非执行失败的任务，不能开启运行！", null);
			}
			ExecuteResult<String> result = runDisasterSurveyTask(task);
			if("F".equalsIgnoreCase(result.getResultCode())) {
				//更新评价任务状态为运行失败
				DisasterSurveyTaskUpdateParamsDto dto = new DisasterSurveyTaskUpdateParamsDto();
				dto.setId(taskId);
				dto.setStatus("4");
				dto.setZhgxr("system");
				disasterSurveyTaskService.updateDisasterSurveyTaskInfo(dto);
			}else {
				//更新评价任务状态为运行中
				DisasterSurveyTaskUpdateParamsDto dto = new DisasterSurveyTaskUpdateParamsDto();
				dto.setId(taskId);
				dto.setStatus("2");
				dto.setZhgxr("system");
				disasterSurveyTaskService.updateDisasterSurveyTaskInfo(dto);
			}
	        return ExecuteResult.success("灾情调查分析任务启动运行成功！");
		} catch (Exception e) {
			logger.error("灾情调查分析任务【{}】启动出现异常，详情：",taskId,e);
			return ExecuteResult.error("灾情调查分析任务启动失败,原因【"+e.getMessage()+"】", null);
		}
	}
	 private ExecuteResult<String> runDisasterSurveyTask(FxSurveyHistoryModel task){
	        String taskId = task.getId();
	        try {
	        	task.setStatus("3");//运行总体状态，1任务创建，2运行中，3运行完成，4运行失败
	        	task.setResultData(taskId);//TODO
	        	task.setResultPath(taskId);
	        	task.setZhgxr("system");
	        	evaluateTaskManagerDao.updateDisasterSurveyTaskInfoByModel(task);
	        	return ExecuteResult.success("分析成功", null);
/**	        	//根目录
	        	String rootPath = getFileRootPath(fileBasicConfig);
	            //任务工作空间根路径
	            String workSpacePath = rootPath+"surveytask"+File.separator+task.getId();
	            //评价服务包路径
	            String modelJarPath = rootPath+fileBasicConfig.getModelJarPath();
	            File  modelJar = new File(modelJarPath);
	            if (!modelJar.exists()) {
	            	return ExecuteResult.error("执行工具的公共服务包程序不存在", null);
	            }
	            File taskFileDir = new File(workSpacePath);
	            if (!StringUtil.isEmpty(taskFileDir.listFiles())){
	                //删除任务工作空间下所有文件
	            	deleteFolder(taskFileDir);
	            }
	            //当前任务评价服务所需参数信息
	            Map<String, String> map = new LinkedHashMap<String, String>();
	            map.put("taskId", taskId);
	            map.put("taskType", "DisasterSurvey");
	            map.put("rootPath", rootPath);
	            map.put("encode", fileBasicConfig.getEncode());
	            XmlUtil.createXml(map, workSpacePath, "params.xml");
	            //复制评价执行包到目标任务工作空间
	            String consoleJarTempPath = workSpacePath + File.separator + modelJar.getName();
	            Files.copy(new File(modelJarPath).toPath(), new File(consoleJarTempPath).toPath());
	            // 创建并执行bat或sh
	            createShFile(workSpacePath, consoleJarTempPath);
	            return ExecuteResult.success("任务启动成功", null);*/
	        } catch (Exception e) {
	            logger.error("启动灾情调查分析任务【{}】出现异常，异常详情：",taskId,e);
	            return ExecuteResult.error("灾情调查分析任务启动失败，详情："+e.getMessage(), null);
	        }
	}
	@Override
	public ExecuteResult<String> exeEvaluateTask(String taskId) {
		try {
			//获取任务记录
			if(StringUtil.isEmpty(taskId) ) {
				return ExecuteResult.error("入参校验不通过，记录ID不能为空！",null);
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("taskId", taskId);
			FxEvaluateHistoryModel task = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class, "id=:taskId", params);
			if(task==null) {
				return ExecuteResult.error("未找到对应运行记录信息", null);
			}
			//校验评价任务状态
			//运行总体状态，1任务创建，2运行中，3运行完成，4运行失败,5已归档
			if(!"1".equals(task.getStatus())&&!"4".equals(task.getStatus())) {
				return ExecuteResult.error("该运行记录非未执行或者非执行失败的任务，不能开启运行！", null);
			}
			ExecuteResult<String> result = runTask(task);
			if("F".equalsIgnoreCase(result.getResultCode())) {
				//更新评价任务状态为运行失败
				EvaluateHistoryUpdateParamsDto dto = new EvaluateHistoryUpdateParamsDto();
				dto.setId(taskId);
				dto.setStatus("4");
				dto.setZhgxr("system");
				evaluateTaskService.updateEvaluateTaskInfo(dto);
			}else {
				//更新评价任务状态为运行中
				EvaluateHistoryUpdateParamsDto dto = new EvaluateHistoryUpdateParamsDto();
				dto.setId(taskId);
				dto.setStatus("2");
				dto.setZhgxr("system");
				evaluateTaskService.updateEvaluateTaskInfo(dto);
			}
	        return ExecuteResult.success("评价任务启动运行成功！");
		} catch (Exception e) {
			logger.error("灾害评估分析任务【{}】启动出现异常，详情：",taskId,e);
			return ExecuteResult.error("灾害评估分析任务启动失败,原因【"+e.getMessage()+"】", null);
		}
	}
	
	 private ExecuteResult<String> runTask(FxEvaluateHistoryModel task){
	        String taskId = task.getId();
	        try {
	        	task.setStatus("3");//运行总体状态，1任务创建，2运行中，3运行完成，4运行失败
	        	task.setResultData(taskId);
	        	task.setResultPath(taskId);
	        	task.setZhgxr("system");
	        	evaluateTaskManagerDao.updateEvaluateTaskInfoByModel(task);
	        	return ExecuteResult.success("分析成功", null);
	        	
	        	//根目录
/**	        	String rootPath = getFileRootPath(fileBasicConfig);
	            //任务工作空间根路径
	            String workSpacePath = rootPath+"evaluatetask"+File.separator+task.getId();
	            //评价服务包路径
	            String modelJarPath = rootPath+fileBasicConfig.getModelJarPath();
	            File  modelJar = new File(modelJarPath);
	            if (!modelJar.exists()) {
	            	return ExecuteResult.error("执行工具的公共服务包程序不存在", null);
	            }
	            File taskFileDir = new File(workSpacePath);
	            if (!StringUtil.isEmpty(taskFileDir.listFiles())){
	                //删除任务工作空间下所有文件
	            	deleteFolder(taskFileDir);
	            }
	            //当前任务评价服务所需参数信息
	            Map<String, String> map = new LinkedHashMap<String, String>();
	            map.put("taskId", taskId);
	            map.put("taskType", "DisasterEvaluate");
	            map.put("rootPath", rootPath);
	            map.put("encode", fileBasicConfig.getEncode());
	            XmlUtil.createXml(map, workSpacePath, "params.xml");
	            //复制评价执行包到目标任务工作空间
	            String consoleJarTempPath = workSpacePath + File.separator + modelJar.getName();
                Files.copy(new File(modelJarPath).toPath(), new File(consoleJarTempPath).toPath());
	            // 创建并执行bat或sh
	            createShFile(workSpacePath, consoleJarTempPath);
	            return ExecuteResult.success("灾害评估分析任务启动成功", null);*/
	        } catch (Exception e) {
	            logger.error("启动灾害评估分析任务【{}】出现异常，异常详情：",taskId,e);
	            return ExecuteResult.error("灾害评估分析任务启动失败，详情："+e.getMessage(), null);
	        }
	    }
	 	/**
	     * 需要注意的是当删除某一目录时，必须保证该目录下没有其他文件才能正确删除，否则将删除失败。
	     * @param [folder]
	     * @return void
	     */
	    private void deleteFolder(File folder) throws Exception {
	        if (folder.isDirectory()&&folder.exists()) {
	        	File[] files = folder.listFiles();
	            if (files != null) {
	                for (File file : files) {
	                    if (file.isDirectory()) {
	                        //递归直到目录下没有文件
	                        deleteFolder(file);
	                    } else {
	                        //删除
	                        file.delete();
	                    }
	                }
	            }
	            //工作空间目录无需删除
	            //folder.delete();
	        }
	    }
	 
	    /**
	     * @Description:linux系统生成sh文件，并运行sh文件，用于启动console服务
	     * @param taskPath 当前任务工作空间路径文件夹
	     * @param consoleJarTempPath 当前任务所用的执行评价的程序包路径
	     * @return:
	     */
	    protected void createShFile(String taskPath, String consoleJarTempPath) throws Exception {
	        String batPath = taskPath + File.separator + "startconsole.sh";
	        File fileBat = new File(batPath);
	        FileOutputStream fileOutputStream = new FileOutputStream(fileBat);
	        OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, "gbk");
	        String gpRunDirectory = Paths.get(consoleJarTempPath).getParent().toString();
	        String consoleTemplateName = Paths.get(consoleJarTempPath).getFileName().toString();
	        StringBuilder sh = new StringBuilder();
	        sh.append("#!/bin/bash").append("\n");
	        //sh.append("JAVA_APP=\"java -Xms256m -Xmx2048m -jar ").append(consoleTemplateName).append("\"").append("\n");
	        //运行完删除console，由于是一次性运行，暂时不加内存上限限制了，降低溢出分险
	        sh.append("JAVA_APP=\"java -Xms256m  -Xmx1024m -jar ").append(consoleTemplateName).append("\"").append("\n");
	        sh.append("LOG_FILE=\"").append(gpRunDirectory+File.separator+"console.out").append("\"").append("\n");
	        //将console端口设置为随机端口了，暂时不用做端口占用验证了
	        sh.append("start_service() {").append("\n");
	        sh.append("  echo \"启动Java服务...\"").append("\n");
	        sh.append("  nohup $JAVA_APP > $LOG_FILE 2>&1 &").append("\n");
	        sh.append("  echo \"服务已启动，PID: $!\"").append("\n");
	        sh.append("}").append("\n");
	        sh.append("  start_service").append("\n");
	        sh.append("echo \"启动脚本执行完毕\"").append("\n");
	        writer.write(sh.toString());
	        writer.flush();
	        writer.close();
	        File file = new File(batPath);
	        Process process = Runtime.getRuntime().exec("sh ./"+file.getName(), null, new java.io.File(file.getParent()));
	       //记录程序运行情况
	        new InputStreamRunnable(process.getErrorStream(), "Error").start();
	        new InputStreamRunnable(process.getInputStream(), "Info").start();
	    }

	    /**
	     * 获取分片数据本地存储根目录
	     * @return
	     */
	    private String getFileRootPath(FileBasicConfig commonConfig) {
	    	String names= System.getProperty("os.name");
	    	String baseFilePath;
	    	 if(names.toLowerCase().startsWith("win")){
	         	baseFilePath = commonConfig.getRootForWin();
	         }else{
	         	baseFilePath = commonConfig.getRootForLiunx();
	         }
	    	 return baseFilePath;
	    }

		
}
