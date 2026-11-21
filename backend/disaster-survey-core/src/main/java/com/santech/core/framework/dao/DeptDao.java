package com.santech.core.framework.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.core.framework.entity.model.QxUserDeptModel;

@Repository
public class DeptDao extends BaseDao{
	
	/**
	 * 保存或插入组织机构信息
	 * @param deptModel
	 * @throws Exception
	 */
	public int insertDeptInfo(QxDepartmentModel deptModel) throws Exception {
        
		return basicDatabase.ORM.insert(deptModel);
    }
	
	/**
	 * 保存或插入用户组织关联信息
	 * @param userDeptModel
	 * @throws Exception
	 */
	public int insertUserDeptInfo(QxUserDeptModel userDeptModel) throws Exception {
        
		return basicDatabase.ORM.insert(userDeptModel);
    }
	
	/**
	 * 修改组织机构信息
	 * @param deptModel
	 * @throws Exception
	 */
	public int updateDeptInfo(QxDepartmentModel deptModel) throws Exception {
		return basicDatabase.ORM.update(deptModel);
    }
	
	/**
	 * 修改用户组织关联信息
	 * @param userDept
	 * @throws Exception
	 */
	public int updateUserDeptInfo(QxUserDeptModel userDept) throws Exception {
		return basicDatabase.ORM.update(userDept);
    }
	public int deleteUserDeptInfoByUserId(String userID) throws Exception {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("userID", userID);
		return basicDatabase.ORM.delete(QxUserDeptModel.class, "USERID=:userID", paramsMap);
    }
	public int deleteUserDeptInfoByDeptId(String deptID) throws Exception {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("deptID", deptID);
		return basicDatabase.ORM.delete(QxUserDeptModel.class, "DEPARTMENTID=:deptID", paramsMap);
    }
	/**
	 * 删除用户组织机构关联信息
	 * @param deleteUserDeptInfo
	 * @throws Exception
	 */
	public int deleteUserDeptInfo(QxUserDeptModel udModel) throws Exception {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("id", udModel.getId());
		return basicDatabase.ORM.delete(QxUserDeptModel.class, "id=:id", paramsMap);
    }
	/**
	 * 删除组织机构信息
	 * @param deptModel
	 * @throws Exception
	 */
	public int deleteDeptInfo(QxDepartmentModel deptModel) throws Exception {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("id", deptModel.getId());
		return basicDatabase.ORM.delete(QxDepartmentModel.class, "id=:id", paramsMap);
    }
	
	/**
	 * @Title: 通过sql查询组织机构信息<br/>
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> querySql(String sql) throws Exception {
		List<Map<String, Object>> list = basicDatabase.SQL.queryListMap(sql);
		return list;
	}
	
	/**
     * 根据ID获取组织机构信息
     * @param id (标识码)
     * @return
     * @throws Exception
     */
    public QxDepartmentModel getDeptById(String id) throws Exception {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("id", id);
        QxDepartmentModel model = basicDatabase.ORM.queryForObject(QxDepartmentModel.class,"id=:id",paramsMap);
        return model;
    }
    /**
     * 根据ID获取其下的子组织机构信息
     * @param id (标识码)
     * @return
     * @throws Exception
     */
    public List<QxDepartmentModel> getSubDepartments(String departmentId) throws Exception {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", departmentId);
        String sql = "STATUS='running' AND PID=:id";
        List<QxDepartmentModel> departmentInfoList = basicDatabase.ORM.queryForList(QxDepartmentModel.class, sql, paramMap);
        return departmentInfoList;
    }
    
    
    /**
     * 根据用户ID获取绑定的部门信息
     * @param userId
     * @return 用户绑定的角色信息
     * @throws Exception
     */
    public List<QxDepartmentModel> getDeptByUserId(String userId) throws Exception {
    	List<QxDepartmentModel> depts = new ArrayList<QxDepartmentModel>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("userId", userId);
        List<QxUserDeptModel> list = basicDatabase.ORM.queryForList(QxUserDeptModel.class,"userid=:userId",paramsMap);
        if(list!=null&&list.size()>0) {
        	for(QxUserDeptModel model:list) {
        		Map<String, Object> deptParamsMap = new HashMap<String, Object>();
        		deptParamsMap.put("id", model.getDepartmentId());
        		QxDepartmentModel dept =  basicDatabase.ORM.queryForObject(QxDepartmentModel.class, "id=:id", deptParamsMap);
        		if(dept!=null) {
        			depts.add(dept);
        		}
        	}
        }
        return depts;
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
    
    /** 根据部门id获取绑定的用户id信息
     * @param departmentId
     * @return 部门绑定的用户信息
     * @throws Exception
     */
    public List<Map<String, Object>> getUserByDepartmentId(String departmentId) throws Exception {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("departmentId", departmentId);
        String queryStr = "SELECT u.* FROM T_QX_USER u INNER JOIN T_QX_USERDEPARTMENT m ON u.USERID = m.USERID " +
                " INNER JOIN T_QX_DEPARTMENT d ON d.ID = m.DEPARTMENTID " +
                " WHERE u.STATUS = 0 AND d.STATUS = 'running' AND d.ID=:departmentId";
        List<Map<String, Object>> departmentUserList = basicDatabase.SQL.queryListMap(queryStr, paramMap);
        return departmentUserList;
    }
    
}
