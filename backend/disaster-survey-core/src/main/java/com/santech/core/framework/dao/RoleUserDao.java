package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRoleUserModel;

@Repository
public class RoleUserDao extends BaseDao{

    public static final Logger logger = LoggerFactory.getLogger(RoleUserDao.class);

    /**
	 * 保存或插入用户角色信息
	 * @param RoleUserModel
	 * @throws Exception
	 */
	public int insertRoleUser(QxRoleUserModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
    }

	 /**
     * 根据角色id删除对应的用户的绑定关系
     * @throws Exception 
     **/
	public int deleteRoleUserInfoByRoleId(String roleId) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("roleId", roleId);
		return basicDatabase.ORM.delete(QxRoleUserModel.class, "ROLEID=:roleId", paramsMap);
    }
	

}
