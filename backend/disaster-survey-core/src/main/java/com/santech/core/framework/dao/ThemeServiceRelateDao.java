package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.ZyThemeServiceRelateModel;
import com.santech.framework.utils.tool.StringUtil;

@Repository
public class ThemeServiceRelateDao extends BaseDao{

    public int deleteByServiceId(String serviceId) throws Exception {
        int resutlt = 0;
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("id", serviceId);
        resutlt = basicDatabase.ORM.delete(ZyThemeServiceRelateModel.class,"SERVICEID=:id",paramsMap);
        return resutlt;
    }
    
    public int deleteByThemeId(String themeId) throws Exception {
        int resutlt = 0;
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("id", themeId);
        resutlt = basicDatabase.ORM.delete(ZyThemeServiceRelateModel.class,"THEMEID=:id",paramsMap);
        return resutlt;
    }
    
    public int insertThemeServiceRelateInfo(ZyThemeServiceRelateModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
    
    /**
     * 根据传入的表名及字段，获取对应表的字段的最大值，需要保证字段对应的数据类型给数值型，主要用来获取排序的默认值
     * @param tablename，表名
     * @param tableColumName ，表字段名
     * @param wheresql ，检索语句，如 THEMEID = 'z'
     * @return 最大值加1
     */
    public int getObjectSortIndexForIntegerType(String tablename,String tableColumName,String wheresql) throws Exception{
    	
    	String querySql = "select max("+tableColumName+") as SORTINDEX from "+tablename+" ";
    	if(StringUtil.isNotEmpty(wheresql)) {
    		querySql += wheresql;
    	}
    	Map<String,Object> data = basicDatabase.SQL.queryRowMap(querySql);
    	if(data==null||StringUtil.isEmpty(data.get("SORTINDEX"))) {
    		return 1;
    	}else {
    		int max = Integer.parseInt(String.valueOf(data.get("SORTINDEX")));
    		return max+1;
    	}
    }
    
    
}
