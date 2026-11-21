package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRoleActionModel;
import com.santech.framework.utils.tool.SqlUtil;


@Repository
public class RoleActionDao extends BaseDao {
	
	public int insertRoleAction(QxRoleActionModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
    }

    /**
     * 根据角色id获取权限
     * @throws Exception 
     **/
    public List<QxRoleActionModel> getActionByRoleId(String roleId) throws Exception{
        List<QxRoleActionModel> list = null;
        if (!roleId.contains(",")) {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("roleId", roleId);
            list = basicDatabase.ORM.queryForList(QxRoleActionModel.class,"roleId=:roleId",paramsMap);
        } else {
            // 根据多角色id批量获取
            // roleId = "xx,xx"
            Map<String, Object> m = SqlUtil.getInSql2("roleId",roleId);
            list = basicDatabase.ORM.queryForList(QxRoleActionModel.class,(String)m.get("sql"),(Map<String, Object>)m.get("paramsMap"));
        }
        return list;
    }

    /**
     * 根据角色id删除对应的资源目录的绑定关系
     * @throws Exception 
     **/
	public int deleteRoleActionInfoByRoleId(String roleId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("roleId", roleId);
		return basicDatabase.ORM.delete(QxRoleActionModel.class, "ROLEID=:roleId", paramsMap);
    }
	
	 public int delRoleAction(List<QxRoleActionModel> roleActionModelList) throws Exception {
        int result = 0;
        for (QxRoleActionModel r : roleActionModelList) {
            Map<String,Object> map = new HashMap<>();
            map.put("roleId",r.getRoleId());
            map.put("actionId",r.getActionId());
            result += basicDatabase.ORM.delete(QxRoleActionModel.class,"roleId=:roleId and actionId=:actionId",map);
        }
        return result;
	}
	 
	 /**
     * 根据功能项id删除各个角色对应的绑定权限
     * @throws Exception 
     **/
	public int deleteRoleActionInfoByActionId(String actionId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("actionId", actionId);
		return basicDatabase.ORM.delete(QxRoleActionModel.class, "ACTIONID=:actionId", paramsMap);
    }
}
