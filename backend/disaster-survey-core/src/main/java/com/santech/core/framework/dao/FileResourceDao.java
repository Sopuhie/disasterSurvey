package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.ZyFileDetailModel;
import com.santech.core.framework.entity.model.ZyFileModel;


@Repository
public class FileResourceDao extends BaseDao {

	public int insertFileResourceInfo(ZyFileModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
	
	public int insertFileDetailInfo(ZyFileDetailModel model) throws Exception {
        
		return basicDatabase.ORM.insert(model);
    }
    
	public int updateFileResourceInfo(ZyFileModel model) throws Exception {
		return basicDatabase.ORM.update(model);
    }
	
	public int deleteFileResourceInfoById(String id) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", id);
		return basicDatabase.ORM.delete(ZyFileModel.class, "ID=:id", paramsMap);
    }
	
	public int deleteFileDetailInfoByFrId(String frid) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("frid", frid);
		return basicDatabase.ORM.delete(ZyFileDetailModel.class, "FRID=:frid", paramsMap);
    }
	
	public int deleteFileDetailInfoById(String id) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", id);
		return basicDatabase.ORM.delete(ZyFileDetailModel.class, "ID=:id", paramsMap);
    }

	public List<Map<String, Object>> selectFileResourceInfos(String sql,Map<String,Object> params) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params==null||params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params);
        }
        return tableList;
    }
    
}
