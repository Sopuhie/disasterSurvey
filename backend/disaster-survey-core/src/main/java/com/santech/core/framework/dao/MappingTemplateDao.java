package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.MappingLogModel;
import com.santech.core.framework.entity.model.MappingTemplateModel;


@Repository
public class MappingTemplateDao extends BaseDao{
	
	public int insertMappingTemplateResourceInfo(MappingTemplateModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
	public int insertLog(MappingLogModel model) throws Exception {
		
        return basicDatabase.ORM.insert(model);
    }
	public int updateMappingTemplateResourceInfo(MappingTemplateModel model) throws Exception {
		
		return basicDatabase.ORM.update(model);
    }
	
	public int updateMappingTemplateResourceInfoBySql(String sql) throws Exception{
        return basicDatabase.SQL.updateSql(sql);
    }
	
	public int deleteMappingTemplateResourceInfoById(String id) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", id);
		return basicDatabase.ORM.delete(MappingTemplateModel.class, "ID=:id", paramsMap);
    }
	
}
