package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.ZyCatalogCollectModel;

@Repository
public class CatalogCollectDao extends BaseDao{


    public int insertCatalogCollectInfo(ZyCatalogCollectModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
    
    public int updateCatalogCollectInfo(ZyCatalogCollectModel model) throws Exception {
		return basicDatabase.ORM.update(model);
    }
    
	public int deleteCatalogCollectInfoById(String collectId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", collectId);
		return basicDatabase.ORM.delete(ZyCatalogCollectModel.class, "ID=:id", paramsMap);
    }
	
	
}
