package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.ZyCatalogModel;

@Repository
public class CatalogResourceDao extends BaseDao{

	public int insertCatalogResourceInfo(ZyCatalogModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
    
    public int updateCatalogResourceInfo(ZyCatalogModel model) throws Exception {
		return basicDatabase.ORM.update(model);
    }
    
    public int updateCatalogResourceInfoBySql(String sql) throws Exception{
        return basicDatabase.SQL.updateSql(sql);
    }
	
	public int deleteCatalogResourceInfoById(String catalogId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", catalogId);
		return basicDatabase.ORM.delete(ZyCatalogModel.class, "ID=:id", paramsMap);
    }
	
	 public int deleteByThemeId(String themeId) throws Exception {
        int resutlt = 0;
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("themeId", themeId);
        resutlt = basicDatabase.ORM.delete(ZyCatalogModel.class,"THEMEID=:themeId",paramsMap);
        return resutlt;
	 }
	 
	 /**
     * 获取目标项目下的所有资源目录项
     * @throws Exception 
     **/
    public List<ZyCatalogModel> getCatalog() throws Exception{
       
        Map<String, Object> paramsMap = new HashMap<>();
        List<ZyCatalogModel> list = basicDatabase.ORM.queryForList(ZyCatalogModel.class,"order by sortindex ",paramsMap);
        
        return list;
    }
    
}
