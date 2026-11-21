package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.JcRouterModel;

@Repository
public class RouterDao extends BaseDao {

    public int insertRouterInfo(JcRouterModel routerModel) throws Exception {
        
		return basicDatabase.ORM.insert(routerModel);
    }
    
	public int updateRouterInfo(JcRouterModel routerModel) throws Exception {
		return basicDatabase.ORM.update(routerModel);
    }
	
	public int deleteRouterInfoById(String routerId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", routerId);
		return basicDatabase.ORM.delete(JcRouterModel.class, "ROUTERID=:id", paramsMap);
    }

	public List<Map<String, Object>> selectRouterInfos(String sql,Map<String,Object> params) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params);
        }
        return tableList;
    }
    
}
