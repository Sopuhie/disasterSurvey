package com.santech.core.framework.dao;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.FxEvaluateHistoryModel;
import com.santech.core.framework.entity.model.FxSurveyHistoryModel;
/**
 * @Title: EvaluateTaskManagerDao.java 
 * @Description：模型运行记录管理持久层 
 * @Version： V1.0
 * @Modify：   
 */
@Repository
public class EvaluateTaskManagerDao extends BaseDao{
	
	public int insertDisasterSurveyTask(FxSurveyHistoryModel data) throws Exception {
        return basicDatabase.ORM.insert(data);
    }
	public int updateDisasterSurveyTaskInfoByModel(FxSurveyHistoryModel data) throws Exception {
        return basicDatabase.ORM.update(data);
    }
	public int insertEvaluateTask(FxEvaluateHistoryModel data) throws Exception {
        return basicDatabase.ORM.insert(data);
    }
	public int updateEvaluateTaskInfoByModel(FxEvaluateHistoryModel data) throws Exception {
        return basicDatabase.ORM.update(data);
    }
	public int updateEvaluateTaskInfoBySql(String sql) throws Exception{
        return basicDatabase.SQL.updateSql(sql);
    }
	
}
