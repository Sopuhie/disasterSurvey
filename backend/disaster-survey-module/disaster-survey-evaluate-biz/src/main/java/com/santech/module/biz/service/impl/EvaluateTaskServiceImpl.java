package com.santech.module.biz.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.EvaluateTaskManagerDao;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.EvaluateHistoryQueryDto;
import com.santech.module.biz.entity.dto.EvaluateHistorySaveParamsDto;
import com.santech.module.biz.entity.dto.EvaluateHistoryUpdateParamsDto;
import com.santech.module.biz.entity.vo.EvaluateProgressVo;
import com.santech.module.biz.service.EvaluateTaskService;
import com.santech.module.biz.utils.TxtFileHandlerUtils;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;


/**
 * @Title: EvaluateTaskServiceImpl.java 
 * @Description：灾害评估运行任务管理接口实现类 
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class EvaluateTaskServiceImpl implements EvaluateTaskService {
	
	private static final Logger logger = LoggerFactory.getLogger(EvaluateTaskServiceImpl.class);
	@Resource
 	private FileBasicConfig fileBasicConfig;
	@Resource
	private EvaluateTaskManagerDao evaluateTaskManagerDao;
	
	
	@Override
	public ExecuteResult<List<FxEvaluateHistoryModel>> queryEvaluateHistoryInfosWithPage(EvaluateHistoryQueryDto query) {
        logger.info("灾害评估运行任务列表分页查询，入参：【{}】",query);
        try {
        	//参数校验
        	String adminFlag = query.getAdminFlag();
        	/*if(StringUtil.isEmpty(adminFlag)) {
        		return ExecuteResult.error("入参，管理员标识不能为空",null);
        	}*/
        	String otherSql = "";//只查来自第三方的
        	if("0".equals(adminFlag)) {
        		//不是管理员，只能查询当前登录人对应的运行记录信息
        		String loginUser = query.getLoginUser();
        		if(StringUtil.isNotEmpty(loginUser)) {
        			 otherSql = " and cjr = '"+loginUser+"'";
        		}else {
        			return ExecuteResult.error("入参当前登录用户不能为空",null);
        		}
        	}
        	if(StringUtil.isNotEmpty(query.getStartTime())) {
        		if(StringUtil.isEmpty(query.getEndTime())) {
        			query.setEndTime(CommonUtils.getFormatDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
        		}
        		String startTime = query.getStartTime();
        		String endTime = query.getEndTime();
        		String timeSQL = String.format("and CJSJ >= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') and  CJSJ <= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') ",startTime,endTime);
        		otherSql = otherSql+timeSQL;
        	}
           //分页参数
           PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
           //bean转map
           Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
           //拼接查询sql
           Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"CJSJ desc",otherSql);
           //根据拼接sql,调用dao层获取数据
           ExecuteResult<List<FxEvaluateHistoryModel>> results = evaluateTaskManagerDao.queryObjectsWithPageInfo(FxEvaluateHistoryModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
           return results;
        } catch (Exception e) {
            logger.error("灾害评估运行任务信息分页查询失败，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
	
	@Override
	public ExecuteResult<FxEvaluateHistoryModel> queryEvaluateHistoryDetailInfoById(String taskId) {
		try {
   			if (StringUtil.isEmpty(taskId)) {
   				return ExecuteResult.error("评价任务ID不能为空值", null);
   			}
   			logger.info("根据ID查询评价历史详情信息，记录id【{}】", taskId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("taskId", taskId);
   			FxEvaluateHistoryModel targetModel = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class,"ID=:taskId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应评价任务信息!", null);
   			}
   			return ExecuteResult.success("查询成功",targetModel);
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询评价任务详情信息出现异常，原因：【{}】,详情：", taskId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	
	@Override
	public ExecuteResult<String> buildEvaluateTaskInfo(EvaluateHistorySaveParamsDto dto) {
		try {
			logger.info("初次保存灾害评估运行任务的参数信息，入参【{}】",dto);
			//校验参数
			String checkInfo = checkEvaluateTaskAddParams(dto);
			if(StringUtil.isNotEmpty(checkInfo)) {
				return ExecuteResult.error("参数校验不通过，详情："+checkInfo, null);
			}
			
			//保存模型运行历史；liteMode 时前端步骤直达完成，否则由周期微服务执行
			FxEvaluateHistoryModel model = new FxEvaluateHistoryModel();
	   		BeanUtils.copyProperties(dto, model);
	   		model.setId(UuidUtils.buildID("EH"));
	   		boolean lite = dto.getLiteMode() != null && dto.getLiteMode();
	   		model.setStatus(lite ? "3" : "1");
	   		model.setLiteMode(lite ? "1" : "0");
	   		if(StringUtil.isNotEmpty(dto.getSceneId())) {
	   			model.setSceneId(dto.getSceneId());
	   		}
	   		if(lite && StringUtil.isNotEmpty(dto.getResultData())) {
	   			model.setResultData(dto.getResultData());
	   			model.setResultPath("lite://completed");
	   		}
	   		model.setCjsj(new Date());
	   		model.setZhgxr(model.getCjr());
	   		model.setZhgxsj(model.getCjsj());
			//保存灾害评估运行任务信息
	   		int count = evaluateTaskManagerDao.insertEvaluateTask(model);
			if(count>0) {
				return ExecuteResult.success(lite ? "创建并完成轻量评估任务成功" : "创建分析任务成功", model.getId());
			}else {
				return ExecuteResult.error("创建分析任务失败",null);
			}
			
		} catch (Exception e) {
			logger.error("初次保存灾害评估运行任务信息出现异常，原因：【{}】,详情：",e.getMessage(),e);
			return ExecuteResult.error("创建分析任务异常",null);
		}
	}
	@Override
	public ExecuteResult<String> updateEvaluateTaskInfo(EvaluateHistoryUpdateParamsDto dto) {
		String taskId = dto.getId();
		try {
			logger.info("更新评价任务信息，入参【{}】", dto);
   			if(StringUtils.isEmpty(taskId)) {
       			return ExecuteResult.error("评价任务ID不能为空！", null);
       		}
   			if(StringUtil.isEmpty(dto.getZhgxr())) {
   				return ExecuteResult.error("更新人不能为空！", null);
   	 		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("taskId",taskId);
   		    FxEvaluateHistoryModel originTask = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class,"ID=:taskId",paramsMap);
   		    if (StringUtil.isEmpty(originTask)) {
   		    	return ExecuteResult.error("未找到对应评价任务，无法完成更新！", null);
   		    }
   		    FxEvaluateHistoryModel newtask = getEvaluateHistoryModelByUpdateInfo(originTask,dto);
		    int count = evaluateTaskManagerDao.updateEvaluateTaskInfoByModel(newtask);
   		    if(count<1){
   		    	throw new RuntimeException("更新评价任务信息失败");
   		    }
   			return ExecuteResult.success("评价任务更新成功！");
   		} catch (Exception e) {
   			logger.error("更新评价任务【{}】及结果异常，详情：",taskId,e);
   			return ExecuteResult.error("更新异常，请联系管理员！",null);
   		}
	}
	
	
	@Override
	public ExecuteResult<String> deleteEvaluateTaskInfo(String id) {
		try {
			//校验参数
			logger.info("删除不想要的运行记录信息,运行记录id：【{}】",id);
			if(StringUtil.isEmpty(id) ) {
				return ExecuteResult.error("入参校验不通过，记录ID不能为空！",null);
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", id);
			FxEvaluateHistoryModel info = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class, "id=:id", params);
			if(info==null) {
				return ExecuteResult.error("未找到对应运行记录信息", null);
			}
			if("2".equals(info.getStatus())) {
				return ExecuteResult.error("该运行记录正在运行中，不可删除该记录信息", null);
			}
			//删除运行历史记录
			int count = evaluateTaskManagerDao.deleteObject(FxEvaluateHistoryModel.class, "id=:id", params);
			if(count>0) {
				//删除对应分析及结果文件信息，TODO
				
				return ExecuteResult.success("删除成功");
			}else {
				return ExecuteResult.error("删除失败", null);
			}
		} catch (Exception e) {
			logger.error("删除不想要的运行记录信息【{}】出现异常,详情：",id,e);
			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
		}
	}
	

	 //校验新增的目录信息
  	 private String checkEvaluateTaskAddParams(EvaluateHistorySaveParamsDto model) throws Exception {
  		 StringBuilder checkInfo = new StringBuilder("");
  		 if(StringUtil.isEmpty(model)) {
  			 checkInfo.append("EvaluateHistorySaveParamsDto不能为空;");
  			 return checkInfo.toString();
  		 }
  		 if(StringUtil.isEmpty(model.getName())) {
  			model.setName("【"+model.getDisasterType()+"】灾害评估"+System.currentTimeMillis());
  	     }
  		 boolean lite = model.getLiteMode() != null && model.getLiteMode();
  		 if(StringUtil.isEmpty(model.getDisasterType())) {
  	        checkInfo.append("灾害类型【disasterType】不能为空;");
  	     }else {
  	    	if("1".equalsIgnoreCase(model.getDisasterType())&&StringUtil.isEmpty(model.getReportData())) {
  	    		if(lite) {
  	    			model.setReportData("{}");
  	    		} else {
  	 			 checkInfo.append("评风暴潮上报数据【reportData】不能为空;");
  	    		}
  	 		}
  	     }
  		if(StringUtil.isEmpty(model.getCjr())) {
  			checkInfo.append("创建人【cjr】不能为空;");
  		}
  		return checkInfo.toString();
  	 }

	
  	

  	 //获取目标更新的评价任务信息
   	private FxEvaluateHistoryModel getEvaluateHistoryModelByUpdateInfo(FxEvaluateHistoryModel origin,EvaluateHistoryUpdateParamsDto update) {
   			
   		 if(StringUtil.isNotEmpty(update.getStatus())) {
   			origin.setStatus(update.getStatus());
   			if("3".equals(update.getStatus())&&StringUtil.isNotEmpty(origin.getFaildReason())) {
   				//如果为运行成功且完成,则将之前的失败原因置空
   				origin.setFaildReason("无");
   			}
   		 }
   		 if(StringUtil.isNotEmpty(update.getResultPath())) {
   			origin.setResultPath(update.getResultPath());
   		 }
   		 if(StringUtil.isNotEmpty(update.getResultData())) {
   			origin.setResultData(update.getResultData());
   		 }
   		 if(StringUtil.isNotEmpty(update.getReportPath())) {
   			origin.setReportPath(update.getReportPath());
   		 }
   		 if(StringUtil.isNotEmpty(update.getFaildReason())) {
   			origin.setFaildReason(update.getFaildReason());
   		 }
   		 if(StringUtil.isNotEmpty(update.getZhgxr())) {
   			origin.setZhgxr(update.getZhgxr());
    	 }
   		 origin.setZhgxsj(new Date());
   		return origin;
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

	@Override
	public ExecuteResult<Map<String, Object>> queryEvaluateTaskProgressInfo(String taskId) {
		try {
			logger.info("查询评价任务执行进度信息，任务ID【{}】", taskId);
   			if(StringUtil.isEmpty(taskId)) {
       			return ExecuteResult.error("评价任务ID不能为空！", null);
       		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("taskId",taskId);
   		    FxEvaluateHistoryModel task = evaluateTaskManagerDao.queryObject(FxEvaluateHistoryModel.class,"ID=:taskId",paramsMap);
   		    if (StringUtil.isEmpty(task)) {
   		    	return ExecuteResult.error("未找到对应评价任务，无法获取对应进度信息！", null);
   		    }
   		    Map<String,Object> progress = new HashMap<String,Object>();
   		    //运行总体状态，1任务创建，2运行中，3运行完成，4运行失败,5已归档
   		    if("1".endsWith(task.getStatus())) {
   		    	progress.put("overallProgress", 1);
   		    	List<EvaluateProgressVo> progressLogs = new ArrayList<EvaluateProgressVo>();
		    	EvaluateProgressVo vo = new EvaluateProgressVo();
		    	vo.setProgressName("任务创建");
		    	vo.setProgressDescription("任务已创建，等待执行中");
		    	vo.setRecordTime(CommonUtils.getFormatDateString(task.getCjsj(), "yyyy-MM-dd HH:mm:ss"));
		    	progressLogs.add(vo);
		    	progress.put("progressLogs", progressLogs);
   		    }else if("2".endsWith(task.getStatus())) {
	    		String logFile = getFileRootPath(fileBasicConfig)+"evaluate"+File.separator+task.getId()+File.separator+"progresslog.txt";
	    		String logs = TxtFileHandlerUtils.parseBaseInfoTXTContentToStringArray(new File(logFile), 1);
	    		if(StringUtil.isNotEmpty(logs)) {
	    			progress = JSON.parseObject(logs, HashMap.class);
	    		}else {
	    			progress.put("overallProgress", 10);
   	   		    	List<EvaluateProgressVo> progressLogs = new ArrayList<EvaluateProgressVo>();
			    	EvaluateProgressVo vo = new EvaluateProgressVo();
			    	vo.setProgressName("任务执行");
			    	vo.setProgressDescription("任务执行，进行中");
			    	vo.setRecordTime(CommonUtils.getFormatDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
			    	progressLogs.add(vo);
			    	progress.put("progressLogs", progressLogs);
	    		}
   		    	
   		    }else {
   		    	progress.put("overallProgress", 100);
   		    	List<EvaluateProgressVo> progressLogs = new ArrayList<EvaluateProgressVo>();
		    	EvaluateProgressVo vo = new EvaluateProgressVo();
		    	vo.setProgressName("任务执行");
		    	vo.setProgressDescription("任务执行，已结束");
		    	vo.setRecordTime(CommonUtils.getFormatDateString(task.getZhgxsj(), "yyyy-MM-dd HH:mm:ss"));
		    	progressLogs.add(vo);
		    	progress.put("progressLogs", progressLogs);
   		    }
   			return ExecuteResult.success("查询成功！",progress);
   		} catch (Exception e) {
   			logger.error("根据任务ID【{}】查询评价任务执行进度信息出现异常，详情：",taskId,e);
   			return ExecuteResult.error("查询失败", null);
   		}
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
