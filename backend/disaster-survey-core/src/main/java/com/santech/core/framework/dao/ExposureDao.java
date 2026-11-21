package com.santech.core.framework.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.exposure.BaseCultureAreaModel;
import com.santech.core.framework.entity.model.exposure.BaseDikeModel;
import com.santech.core.framework.entity.model.exposure.BaseFisheryModel;
import com.santech.core.framework.entity.model.exposure.BaseTouristAreaModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;

import santech.common.dbcomponent.entity.DatabaseHandler;
import santech.common.dbcomponent.entity.dbcomponents.OrmInfo;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;
/**
 * 承灾体数据库操作类
 */
@Repository
public class ExposureDao {
	
	@Autowired
    @Qualifier("db2")
	DatabaseHandler dangerDatabase;
	
	/**
     * 根据传入的实体类定义及查询参数，获取指定实体类信息,多条查询，可做分页
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return List<T>
     */
    public <T> ExecuteResult<List<T>> queryDangerObjectsWithPageInfo(Class<T> clazz,String whereSQL,Map<String, Object> params,PageInfo pageInfo) throws Exception{
    	 List<T> list = new ArrayList<T>();
         OrmInfo ormInfo = new OrmInfo();
         ormInfo.queryString = whereSQL;
         ormInfo.paramMap = params;
         ormInfo.pageInfo = pageInfo;
         list = dangerDatabase.ORM.queryForList(clazz,ormInfo);
         return ExecuteResult.success("查询成功", list, pageInfo);
    }
    
    
    /**
     * 根据传入的实体类定义及查询参数，获取指定实体类信息，单条记录查询
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return T
     */
    public <T> T queryDangerObject(Class<T> clazz,String whereSQL,Map<String, Object> paramsMap) throws Exception{
    	T resultModel = dangerDatabase.ORM.queryForObject(clazz,whereSQL,paramsMap);
        return resultModel;
    }
    
    /**
     * 根据传入的实体类定义及查询参数，获取指定实体类信息，多条查询，不分页
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @return List<T>
     */
    public <T> List<T> queryDangerObjects(Class<T> clazz,String whereSQL,Map<String, Object> paramsMap) throws Exception{
    	List<T> tableList = null;
    	if(StringUtil.isNotEmpty(whereSQL)) {
    		tableList = dangerDatabase.ORM.queryForList(clazz, whereSQL, paramsMap);
    	}else {
    		tableList = dangerDatabase.ORM.queryForList(clazz, "");
    	}
    	
     return  tableList;
    }
	
	
	
}
