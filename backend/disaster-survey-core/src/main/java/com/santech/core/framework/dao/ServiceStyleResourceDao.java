package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.ZyServiceStyleModel;


@Repository
public class ServiceStyleResourceDao extends BaseDao {

	public int insertServiceStyleResourceInfo(ZyServiceStyleModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
	
	public int updateServiceStyleResourceInfo(ZyServiceStyleModel model) throws Exception {
		return basicDatabase.ORM.update(model);
    }
	public int updateServiceStyleResourceInfoBySql(String sql) throws Exception{
        return basicDatabase.SQL.updateSql(sql);
    }
	public int deleteServiceStyleResourceInfoById(String id) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", id);
		return basicDatabase.ORM.delete(ZyServiceStyleModel.class, "ID=:id", paramsMap);
    }
	
}
