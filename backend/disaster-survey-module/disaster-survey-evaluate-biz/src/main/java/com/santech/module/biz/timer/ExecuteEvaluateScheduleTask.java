package com.santech.module.biz.timer;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.santech.core.framework.dao.EvaluateTaskManagerDao;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.core.framework.entity.model.FxSurveyHistoryModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.service.EvaluateTaskExeService;

/**
 * @Title: ExecuteEvaluateScheduleTask.java 
 * @Description：水质插值评价任务执行处理类 
 * @Version： V1.0
 * @Modify：   
 */
@Component
//@EnableScheduling
public class ExecuteEvaluateScheduleTask {
	public static final Logger logger = LoggerFactory.getLogger(ExecuteEvaluateScheduleTask.class);
	@Resource
	private EvaluateTaskManagerDao evaluateTaskManagerDao;
	
	@Resource
	private EvaluateTaskExeService evaluateTaskExeService;
	
    /**
     * 
     * 定时拉取新建以及同步失败的进度记录，进行发起或重新数据同步
     * 
     */
    //@Scheduled(cron = "0/30 * * * * ?")// (每15秒(cron = "0/15 * * * * ?");每分钟(cron = "0 0/5 * * * ?");每小时(cron = "0 0 * * * ?"))
    public void executeDisasterSurveyTask() {
        
        try {
    		logger.info("拉取待运行的灾情调查分析任务，进行发起分析，开始时间【{}】",System.currentTimeMillis());
    		//验证当前启动任务数，运行总体状态，1任务创建，2运行中，3运行完成，4运行失败,5已归档
    		String wheresql1 = " STATUS = '2' ";
    		List<FxSurveyHistoryModel> actions = evaluateTaskManagerDao.queryObjects(FxSurveyHistoryModel.class, wheresql1,null);
    		if(actions==null||actions.size()<3) {
    			int countAll = actions==null?0:actions.size();
    			//当前运行的任务数不超标，查询所有新创建分析任务信息
            	String wheresql2 = " STATUS = '1' order by CJSJ desc ";//" STATUS = '1' or STATUS = '4' order by CJSJ desc ";
                //根据拼接sql,调用dao层获取数据
    			List<FxSurveyHistoryModel> tasks = evaluateTaskManagerDao.queryObjects(FxSurveyHistoryModel.class, wheresql2,null);
    	    	//遍历数据执行数据同步
    			if(tasks!=null&&tasks.size()>0) {
    				for(FxSurveyHistoryModel model:tasks) {
    					//控制总任务数小于4个
    					if(countAll>2) {
    						break;
    					}
    					String taskId = model.getId();
    					ExecuteResult<String> result = evaluateTaskExeService.exeDisasterSurveyTask(taskId);
    					if("F".equals(result.getResultCode())) {
    						//同步调用失败
    						logger.error("任务id【{}】,调用评价任务执行接口出现异常，异常详情【{}】",taskId,result.getResultMsg());
    					}else {
    						countAll++;
    					}
    				}
    				//logger.info("拉取待运行的评价任务，进行发起评价，结束时间【{}】",System.currentTimeMillis());
    			}
    			
    		}else {
    			logger.info("灾情调查分析任务已超过3个，为了避免内存耗尽，放入下一次的任务执行");
    		}
		} catch (Exception e) {
			logger.error("拉取待运行的灾情调查分析任务，并执行出现异常，详情【{}】",e);
		}
    }
    /**
     * 
     * 定时拉取新建以及同步失败的进度记录，进行发起或重新数据同步
     * 
     */
    //@Scheduled(cron = "0/30 * * * * ?")// (每15秒(cron = "0/15 * * * * ?");每分钟(cron = "0 0/5 * * * ?");每小时(cron = "0 0 * * * ?"))
    public void executeEvaluateTask() {
        
        try {
    		logger.info("拉取待运行的评价任务，进行发起评价，开始时间【{}】",System.currentTimeMillis());
    		//验证当前启动任务数，运行总体状态，1任务创建，2运行中，3运行完成，4运行失败,5已归档
    		String wheresql1 = " STATUS = '2' ";
    		List<FxEvaluateHistoryModel> actions = evaluateTaskManagerDao.queryObjects(FxEvaluateHistoryModel.class, wheresql1,null);
    		if(actions==null||actions.size()<3) {
    			int countAll = actions==null?0:actions.size();
    			//当前运行的任务数不超标，查询所有新创建分析任务信息
            	String wheresql2 = " STATUS = '1' order by CJSJ desc ";//" STATUS = '1' or STATUS = '4' order by CJSJ desc ";
                //根据拼接sql,调用dao层获取数据
    			List<FxEvaluateHistoryModel> tasks = evaluateTaskManagerDao.queryObjects(FxEvaluateHistoryModel.class, wheresql2,null);
    	    	//遍历数据执行数据同步
    			if(tasks!=null&&tasks.size()>0) {
    				for(FxEvaluateHistoryModel model:tasks) {
    					//控制总任务数小于4个
    					if(countAll>2) {
    						break;
    					}
    					String taskId = model.getId();
    					ExecuteResult<String> result = evaluateTaskExeService.exeEvaluateTask(taskId);
    					if("F".equals(result.getResultCode())) {
    						//同步调用失败
    						logger.error("任务id【{}】,调用评价任务执行接口出现异常，异常详情【{}】",taskId,result.getResultMsg());
    					}else {
    						countAll++;
    					}
    				}
    				//logger.info("拉取待运行的评价任务，进行发起评价，结束时间【{}】",System.currentTimeMillis());
    			}
    			
    		}else {
    			logger.info("评价任务已超过3个，为了避免内存耗尽，放入下一次的任务执行");
    		}
		} catch (Exception e) {
			logger.error("拉取待运行的评价任务，并执行出现异常，详情【{}】",e);
		}
    }    

}
