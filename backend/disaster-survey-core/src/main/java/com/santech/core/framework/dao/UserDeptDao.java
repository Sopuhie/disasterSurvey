package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxUserDeptModel;

@Repository
public class UserDeptDao extends BaseDao{
	public static final Logger logger = LoggerFactory.getLogger(UserDeptDao.class);
	
	/**
	 * 保存或插入组织机构用户关联信息
	 * @param userDeptModel
	 * @throws Exception
	 */
	public int insertUserDeptInfo(QxUserDeptModel userDeptModel) throws Exception {
        
		return basicDatabase.ORM.insert(userDeptModel);
    }
	
	/**
	 * 修改组织机构用户关联信息
	 * @param userDeptModel
	 * @throws Exception
	 */
	public int updateUserDeptInfo(QxUserDeptModel userDeptModel) throws Exception {
		return basicDatabase.ORM.update(userDeptModel);
    }
	
	/**
	 * 删除组织机构用户关联信息
	 * @param userDeptModel
	 * @throws Exception
	 */
	public int deleteUserDeptInfo(QxUserDeptModel userDeptModel) throws Exception {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("id", userDeptModel.getId());
		return basicDatabase.ORM.delete(QxUserDeptModel.class, "id=:id", paramsMap);
    }
	
	/**
	 * @Title: 通过sql查询组织机构用户关联信息<br/>
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> querySql(String sql) throws Exception {
		List<Map<String, Object>> list = basicDatabase.SQL.queryListMap(sql);
		return list;
	}
	
	/**
     * 根据ID获取组织机构用户关联信息
     * @param id (标识码)
     * @return
     * @throws Exception
     */
    public QxUserDeptModel getUserDeptById(String id) throws Exception {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("id", id);
        QxUserDeptModel model = basicDatabase.ORM.queryForObject(QxUserDeptModel.class,"id=:id",paramsMap);
        return model;
    }
    
    /**
     * @Title: 通过sql更新审查记录过程信息<br/>
     * @param sql
     * @return
     * @throws Exception
     */
    public int updateSql(String sql) throws Exception{
        int result =  basicDatabase.SQL.updateSql(sql);
        return result;
    }
}
