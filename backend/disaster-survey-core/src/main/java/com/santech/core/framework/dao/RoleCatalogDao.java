package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRoleCatalogModel;
import com.santech.framework.utils.tool.SqlUtil;


@Repository
public class RoleCatalogDao extends BaseDao {
	
	public int insertRoleCatalog(QxRoleCatalogModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
    }

    /**
     * 获取角色目录权限
     * @throws Exception 
     **/
    public List<QxRoleCatalogModel> getCatalogByRoleId(String roleId) throws Exception {
        List<QxRoleCatalogModel> list = null;
        if (!roleId.contains(",")) {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("roleId", roleId);
            list = basicDatabase.ORM.queryForList(QxRoleCatalogModel.class,"roleId=:roleId",paramsMap);
        } else {
            // 根据多角色id批量获取
            // roleId = "xx,xx"
            Map<String, Object> m = SqlUtil.getInSql2("roleId",roleId);
            list = basicDatabase.ORM.queryForList(QxRoleCatalogModel.class,(String)m.get("sql"),(Map<String, Object>)m.get("paramsMap"));
        }
        return list;
    }
    
    /**
     * 根据角色id删除对应的资源目录的绑定关系
     * @throws Exception 
     **/
	public int deleteRoleCatalogInfoByRoleId(String roleId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("roleId", roleId);
		return basicDatabase.ORM.delete(QxRoleCatalogModel.class, "ROLEID=:roleId", paramsMap);
    }
	
    public int delRoleCatalog(List<QxRoleCatalogModel> rolCatalogModelList) throws Exception {
        int result = 0;
        for (QxRoleCatalogModel r : rolCatalogModelList) {
            Map<String,Object> map = new HashMap<>();
            map.put("roleId",r.getRoleId());
            map.put("catalogId",r.getCatalogId());
            result += basicDatabase.ORM.delete(QxRoleCatalogModel.class,"roleId=:roleId and CATALOGID=:catalogId",map);
        }
        return result;
	}
    
    public int deleteRoleCatalogInfoByCatalogId(String catalogId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("catalogId", catalogId);
		return basicDatabase.ORM.delete(QxRoleCatalogModel.class, "CATALOGID=:catalogId", paramsMap);
    }

}
