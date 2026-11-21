package com.santech.core.framework.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.core.framework.entity.model.QxRoleUserModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Repository
public class RoleDao extends BaseDao{

    public static final Logger logger = LoggerFactory.getLogger(RoleDao.class);

    public int insertRoleInfo(QxRoleModel roleModel) throws Exception {
        
		return basicDatabase.ORM.insert(roleModel);
    }
    
	public int updateRoleInfo(QxRoleModel roleModel) throws Exception {
		return basicDatabase.ORM.update(roleModel);
    }
	
	public int deleteRoleInfoById(String roleId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", roleId);
		return basicDatabase.ORM.delete(QxRoleModel.class, "ROLEID=:id", paramsMap);
    }
	
	public ExecuteResult<List<Map<String, Object>>> selectRoleInfosWithPage(String sql,Map<String,Object> params,PageInfo pageInfo) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql, pageInfo);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params, pageInfo);
        }
        return ExecuteResult.success("查询成功", tableList, pageInfo);
    }
	
	
    /**
     * 根据用户ID获取绑定的角色信息
     * @param userId
     * @return 用户绑定的角色信息
     * @throws Exception
     */
    public List<QxRoleModel> getRolesByUserId(String userId) throws Exception {
    	List<QxRoleModel> roles = new ArrayList<QxRoleModel>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("userId", userId);
        List<QxRoleUserModel> list = basicDatabase.ORM.queryForList(QxRoleUserModel.class,"userid=:userId",paramsMap);
        if(list!=null&&list.size()>0) {
        	Map<String,String> uniquerole = new HashMap<String,String>();
        	for(QxRoleUserModel model:list) {
        		Map<String, Object> roleParamsMap = new HashMap<String, Object>();
        		roleParamsMap.put("roleId", model.getRoleId());
        		QxRoleModel role =  basicDatabase.ORM.queryForObject(QxRoleModel.class, "roleid=:roleId", roleParamsMap);
        		if(role!=null) {
        			if(StringUtil.isEmpty(uniquerole.get(role.getRoleId()))) {
        				//进行去重
        				roles.add(role);
        				uniquerole.put(role.getRoleId(), role.getRoleId());
        			}
        			
        		}
        	}
        }
        return roles;
    }
	
    /**
     * 根据角色名称获取角色ID
     * @param roleName
     * @return 角色ID
     * @throws Exception
     */
	public String getRoleIdByRoleName(String roleName) throws Exception {
		String roleId = "";
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("rolename", roleName);
		List<QxRoleModel> list = basicDatabase.ORM.queryForList(QxRoleModel.class, "rolename=:rolename", paramsMap);
		if (list != null && !list.isEmpty()) {
			QxRoleModel model = list.get(0);
			roleId = model.getRoleId();
		}
		return roleId;
	}
}


