package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRoleRouterModel;
import com.santech.framework.utils.tool.SqlUtil;


@Repository
public class RoleRouterDao extends BaseDao {
	
	public int insertRoleRouter(QxRoleRouterModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
    }
    /**
     * 根据角色id删除对应的运维路由菜单的绑定权限
     * @throws Exception 
     **/
	public int deleteRoleRouterInfoByRoleId(String roleId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("roleId", roleId);
		return basicDatabase.ORM.delete(QxRoleRouterModel.class, "ROLEID=:roleId", paramsMap);
    }
	/**
     * 根据角色id获取权限
     * @throws Exception 
     **/
    public List<QxRoleRouterModel> getRouterByRoleId(String roleId) throws Exception{
        List<QxRoleRouterModel> list = null;
        if (!roleId.contains(",")) {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("roleId", roleId);
            list = basicDatabase.ORM.queryForList(QxRoleRouterModel.class,"roleId=:roleId",paramsMap);
        } else {
            // 根据多角色id批量获取
            // roleId = "xx,xx"
            Map<String, Object> m = SqlUtil.getInSql2("roleId",roleId);
            list = basicDatabase.ORM.queryForList(QxRoleRouterModel.class,(String)m.get("sql"),(Map<String, Object>)m.get("paramsMap"));
        }
        return list;
    }
    
    public int delRoleRouter(List<QxRoleRouterModel> roleRouterModelList) throws Exception {
        int result = 0;
        for (QxRoleRouterModel r : roleRouterModelList) {
            Map<String,Object> map = new HashMap<>();
            map.put("roleId",r.getRoleId());
            map.put("routerId",r.getRouterId());
            result += basicDatabase.ORM.delete(QxRoleRouterModel.class,"roleId=:roleId and ROUTERID=:routerId",map);
        }
        return result;
	}
    
    /**
     * 根据运维路由菜单id删除各个角色对应的绑定权限
     * @throws Exception 
     **/
	public int deleteRoleRouterInfoByRouterId(String routerId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("routerId", routerId);
		return basicDatabase.ORM.delete(QxRoleRouterModel.class, "ROUTERID=:routerId", paramsMap);
    }

}
