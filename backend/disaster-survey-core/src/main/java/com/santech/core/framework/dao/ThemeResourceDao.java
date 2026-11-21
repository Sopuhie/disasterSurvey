package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.ZyThemeModel;

@Repository
public class ThemeResourceDao extends BaseDao{

	 	public int insertThemeResourceInfo(ZyThemeModel model) throws Exception {
	        
			return basicDatabase.ORM.insert(model);
	    }
	    
	    public int updateThemeResourceInfo(ZyThemeModel model) throws Exception {
			return basicDatabase.ORM.update(model);
	    }
	    
	    public int updateThemeResourceInfoBySql(String sql) throws Exception{
	        return basicDatabase.SQL.updateSql(sql);
	    }
		
		public int deleteThemeResourceInfoById(String themeId) throws Exception {
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.put("id", themeId);
			return basicDatabase.ORM.delete(ZyThemeModel.class, "ID=:id", paramsMap);
	    }
		


}
