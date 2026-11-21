package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.QxRoleUserModel;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.utils.component.encrypt.EncryptUtil;
import com.santech.framework.utils.tool.SqlUtil;

import cn.hutool.core.util.ObjectUtil;

@Repository
public class UserDao extends BaseDao{

    public static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    /**
     * @param username 用户名，password 密码
     * @throws Exception 
     * @Return
     * @Description 登录
     */
    public Map<String,Object> login(String username,String password) throws Exception {
    	 Map<String,Object> result = new HashMap<String,Object>();
        int checkResult  = 0;
        Map<String,Object> paramsMap = new HashMap<String,Object>();
        paramsMap.put("username",username);
        paramsMap.put("status",0);
        QxUserModel userModel = basicDatabase.ORM.queryForObject(QxUserModel.class, "username=:username and status=:status", paramsMap);
        if (ObjectUtil.isNotEmpty(userModel)) {
            // 校验密码
            //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        	//passwordEncoder.matches(password,userModel.getPassword());//如果单点登录需要将此注释掉，由于单点登录后采用同步过来用户，故密码不再进行此验证
            boolean a  = EncryptUtil.encrypt(password).equals(userModel.getPassword());
            //boolean a = KindCryptoUtils.encryptSM4(password, 3).equals(userModel.getPassword());
            if (a) {
            	//认证通过
            	checkResult = 1;
            	userModel.setPassword("");//密码不做返回
            	result.put("userInfo", userModel);
            }
        }
        result.put("checkResult", checkResult);
        return result;
    }
    
    /**
     * 根据角色ID获取角色用户关联信息
     * @param roleId
     * @return
     * @throws Exception
     */
    public List<QxRoleUserModel> getRoleUsersByRoleId(String roleId) throws Exception {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("roleId", roleId);
        List<QxRoleUserModel> list = basicDatabase.ORM.queryForList(QxRoleUserModel.class,"roleid=:roleId",paramsMap);
        return list;
    }
    
    /**
     * 根据Id获取用户信息
     * 可批量获取用户，以逗号分隔id
     **/
    public List<QxUserModel> getUserInfosByIds(String userIds) throws Exception {
        Map<String, Object> m = SqlUtil.getInSql2("userid",userIds);
        String querySql = (String)m.get("sql") + " and status = 0 ";//查询用户id均为有效的用户信息
        List<QxUserModel> list = basicDatabase.ORM.queryForList(QxUserModel.class, querySql,(Map<String, Object>)m.get("paramsMap"));
        return list;
    }

    /**
	 * 保存或插入用户信息
	 * @param achievementInfoModel
	 * @throws Exception
	 */
	public int insertUserInfo(QxUserModel userModel) throws Exception {
        
		return basicDatabase.ORM.insert(userModel);
    }
	
	/**
	 * 修改用户信息
	 * @param userModel
	 * @throws Exception
	 */
	public int updateUserInfo(QxUserModel userModel) throws Exception {
		return basicDatabase.ORM.update(userModel);
    }
	
	/**
	 * 删除用户信息
	 * @param userModel
	 * @throws Exception
	 */
	public int deleteUserInfo(QxUserModel userModel) throws Exception {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("userid", userModel.getUserId());
		return basicDatabase.ORM.delete(QxUserModel.class, "userid=:userid", paramsMap);
    }
	
	/**
	 * @Title: 通过sql查询用户信息<br/>
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> querySql(String sql) throws Exception {
		 List<Map<String, Object>> list = basicDatabase.SQL.queryListMap(sql);
		return list;
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
    
	/**
     * 根据用户ID获取用户信息
     * @param userid (用户ID)
     * @return
     * @throws Exception
     */
    public QxUserModel getUserByUserId(String userid) throws Exception {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("userid", userid);
        QxUserModel model = basicDatabase.ORM.queryForObject(QxUserModel.class,"userid=:userid",paramsMap);
        return model;
    }
    /**
     * 根据用户手机号获取用户信息
     * @param mobile (用户手机号)
     * @return
     * @throws Exception
     */
    public QxUserSimpleModel getUserByUserTel(String mobile) throws Exception {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("tel", mobile);
        QxUserSimpleModel model = basicDatabase.ORM.queryForObject(QxUserModel.class,"TEL=:tel",paramsMap);
        return model;
    }
}


