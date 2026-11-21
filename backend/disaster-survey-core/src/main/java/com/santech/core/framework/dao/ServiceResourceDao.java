package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.ZyServiceModel;

@Repository
public class ServiceResourceDao extends BaseDao{

    public static final Logger logger = LoggerFactory.getLogger(ServiceResourceDao.class);

    public int insertServiceResourceInfo(ZyServiceModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
    
    public int updateServiceResourceInfo(ZyServiceModel model) throws Exception {
		return basicDatabase.ORM.update(model);
    }
    
    public int updateServiceResourceInfoBySql(String sql) throws Exception{
        return basicDatabase.SQL.updateSql(sql);
    }
	
	public int deleteServiceResourceInfoById(String serviceId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", serviceId);
		return basicDatabase.ORM.delete(ZyServiceModel.class, "ID=:id", paramsMap);
    }
	
	 public List<Map<String, Object>> selectServiceResourcesWithoutPage(String sql,Map<String,Object> params) throws Exception {
	        List<Map<String, Object>> tableList = null;
	        if(params==null||params.size()==0){
	            tableList = basicDatabase.SQL.queryListMap(sql);
	        }else{
	            tableList = basicDatabase.SQL.queryListMap(sql, params);
	        }
	        return tableList;
	 }
	
	
}
