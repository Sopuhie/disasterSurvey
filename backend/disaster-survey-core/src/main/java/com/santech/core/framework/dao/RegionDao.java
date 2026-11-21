package com.santech.core.framework.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRegionSimpleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * @Title: BasicInfoDao.java 
 * @Description：行政区持久层 
 * @Version： V1.0
 * @Modify：   
 */
@Repository
public class RegionDao extends BaseDao {
	
    /**
     * @Title selectAllXzqhdmlist
     * @Description (获取市级和区县级行政区信息,按行政区代码升序排列返回)
     * @param regionLevel 行政区级别，多个以英文逗号分割，如市级,区级,镇级,村级
     * @return (接口的执行情况)
     */
    public List<QxRegionSimpleModel> selectRegionlist(String regionLevel) throws Exception{
    	String sql = "1 = 1 ";
    	if(StringUtil.isNotEmpty(regionLevel)) {
    		String[] levels = regionLevel.split(",");
    		String regionLevelSql = " REGIONLEVEL='省级' ";
    		for(int i=0;i<levels.length;i++) {
    			String level = levels[i];
    			if(StringUtil.isNotEmpty(level)) {
    				if(StringUtil.isNotEmpty(regionLevelSql)) {
    					regionLevelSql = regionLevelSql+" or REGIONLEVEL='"+level+"'";
    				}else {
    					regionLevelSql = "REGIONLEVEL='"+level+"'";
    				}
    			}
    		}
    		if(StringUtil.isNotEmpty(regionLevelSql)) {
    			sql += " and ("+regionLevelSql+") order by SORTINDEX asc";
    		}else {
    			sql += " and (REGIONLEVEL='镇级' or REGIONLEVEL='区级' or REGIONLEVEL='市级' or REGIONLEVEL='省级' ) order by SORTINDEX asc";
    		}
    	}else {
    		//设置默认查询到市级,区级,镇级
    		sql += " and (REGIONLEVEL='镇级' or REGIONLEVEL='区级' or REGIONLEVEL='市级' or REGIONLEVEL='省级' ) order by SORTINDEX asc";
    	}
        List<QxRegionSimpleModel> lyXzqhdmModelList=basicDatabase.ORM.queryForList(QxRegionSimpleModel.class,sql);
        return lyXzqhdmModelList;
    }
    
    public ExecuteResult<List<Map<String, Object>>> selectRegionInfos(String sql,Map<String,Object> params,PageInfo pageInfo) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql, pageInfo);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params, pageInfo);
        }
        return ExecuteResult.success("查询成功", tableList, pageInfo);
    }

    /**
     * 保存
     * @param clazz
     * @param <T>
     * @throws Exception
     */
    public <T> int save(T clazz) throws Exception {
        return basicDatabase.ORM.insert(clazz);
    }

    /**
     * 修改
     * @param clazz
     * @param sql
     * @param params
     * @param <T>
     */
    public <T> void update(T clazz, String sql, Map<String, Object> params) throws Exception {
        basicDatabase.ORM.update(clazz,sql,params);

    }

    /**
     * 删除
     * @param clazz
     * @param whereSQL
     * @param paramsMap
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> int deleteObject(Class<T> clazz,String whereSQL,Map<String, Object> paramsMap) throws Exception{
        int result = basicDatabase.ORM.delete(clazz, whereSQL, paramsMap);
        return result;
    }


}
