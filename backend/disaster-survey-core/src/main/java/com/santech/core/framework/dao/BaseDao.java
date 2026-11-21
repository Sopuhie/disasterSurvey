package com.santech.core.framework.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;

import santech.common.dbcomponent.entity.DatabaseHandler;
import santech.common.dbcomponent.entity.dbcomponents.OrmInfo;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

public class BaseDao {

	@Resource
    public DatabaseHandler basicDatabase;

    
    
    
    /**
     * 根据传入的实体类定义及查询参数，获取指定实体类信息,多条查询，可做分页
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return List<T>
     */
    public <T> ExecuteResult<List<T>> queryObjectsWithPageInfo(Class<T> clazz,String whereSQL,Map<String, Object> params,PageInfo pageInfo) throws Exception{
    	 List<T> list = new ArrayList<T>();
         OrmInfo ormInfo = new OrmInfo();
         ormInfo.queryString = whereSQL;
         ormInfo.paramMap = params;
         ormInfo.pageInfo = pageInfo;
         list = basicDatabase.ORM.queryForList(clazz,ormInfo);
         return ExecuteResult.success("查询成功", list, pageInfo);
    }
    
    
    /**
     * 根据传入的实体类定义及查询参数，获取指定实体类信息，单条记录查询
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return T
     */
    public <T> T queryObject(Class<T> clazz,String whereSQL,Map<String, Object> paramsMap) throws Exception{
    	T resultModel = basicDatabase.ORM.queryForObject(clazz,whereSQL,paramsMap);
        return resultModel;
    }
    
    /**
     * 根据传入的实体类定义及查询参数，获取指定实体类信息，多条查询，不分页
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return List<T>
     */
    public <T> List<T> queryObjects(Class<T> clazz,String whereSQL,Map<String, Object> paramsMap) throws Exception{
    	List<T> tableList = null;
    	if(StringUtil.isNotEmpty(whereSQL)) {
    		tableList = basicDatabase.ORM.queryForList(clazz, whereSQL, paramsMap);
    	}else {
    		tableList = basicDatabase.ORM.queryForList(clazz, "");
    	}
    	
     return  tableList;
    }
    
    /**
     * 根据传入的实体类定义及删除参数，删除符合条件的数据信息
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return T
     */
    public <T> int deleteObject(Class<T> clazz,String whereSQL,Map<String, Object> paramsMap) throws Exception{
    	int result = basicDatabase.ORM.delete(clazz, whereSQL, paramsMap);
        return result;
    }
    
    /**
     * 根据传入的表名及字段，获取对应表的字段的最大值，需要保证字段对应的数据类型给数值型，主要用来获取排序的默认值
     * @param tablename，表名
     * @param tableColumName ，表字段名
     * @return 最大值加1
     */
    public int getObjectSortIndexForIntegerType(String tablename,String tableColumName) throws Exception{
    	String querySql = "select max("+tableColumName+") as SORTINDEX from "+tablename+"";
    	Map<String,Object> data = basicDatabase.SQL.queryRowMap(querySql);
    	if(data==null||StringUtil.isEmpty(data.get("SORTINDEX"))) {
    		return 1;
    	}else {
    		int max = Integer.parseInt(String.valueOf(data.get("SORTINDEX")));
    		return max+1;
    	}
    }
}
