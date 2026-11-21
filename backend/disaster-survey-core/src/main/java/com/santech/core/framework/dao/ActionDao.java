package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.JcActionModel;
import com.santech.framework.global.ExecuteResult;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Repository
public class ActionDao extends BaseDao{

    public ExecuteResult<List<Map<String, Object>>> selectActionInfos(String sql,Map<String,Object> params,PageInfo pageInfo) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql, pageInfo);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params, pageInfo);
        }
        return ExecuteResult.success("查询成功", tableList, pageInfo);
    }
    
    public List<Map<String, Object>> selectRouterInfosWithoutPage(String sql,Map<String,Object> params) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params);
        }
        return tableList;
    }
    
	public int insertActionInfo(JcActionModel actionModel) throws Exception {
        
		return basicDatabase.ORM.insert(actionModel);
    }
    
	public int updateActionInfo(JcActionModel actionModel) throws Exception {
		return basicDatabase.ORM.update(actionModel);
    }
	
	public int deleteActionInfoById(String actionId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", actionId);
		return basicDatabase.ORM.delete(JcActionModel.class, "id=:id", paramsMap);
    }
	
	/**
     * 获取目标项目的功能项
     * @throws Exception 
     **/
    public List<JcActionModel> getAction() throws Exception{
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("visible", "1");//只查启用的功能项
        List<JcActionModel> list = basicDatabase.ORM.queryForList(JcActionModel.class,"visible=:visible",paramsMap);
        return list;
    }
}
