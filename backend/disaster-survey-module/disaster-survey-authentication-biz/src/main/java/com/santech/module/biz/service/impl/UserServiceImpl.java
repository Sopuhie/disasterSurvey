package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.santech.core.framework.dao.DeptDao;
import com.santech.core.framework.dao.RoleDao;
import com.santech.core.framework.dao.RoleUserDao;
import com.santech.core.framework.dao.UserDao;
import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.core.framework.entity.model.QxRoleUserModel;
import com.santech.core.framework.entity.model.QxUserDeptModel;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.EncryptUtil;
import com.santech.framework.utils.component.encrypt.rsa.RSAUtils;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.config.AuthenticationConfig;
import com.santech.module.biz.entity.dto.RoleQueryDto;
import com.santech.module.biz.entity.dto.UserAddDto;
import com.santech.module.biz.entity.dto.UserPasswordChangeDto;
import com.santech.module.biz.entity.dto.UserQueryDto;
import com.santech.module.biz.entity.dto.UserUpdateDto;
import com.santech.module.biz.entity.vo.UserGroupByDeptVo;
import com.santech.module.biz.service.RoleService;
import com.santech.module.biz.service.UserService;
import com.santech.module.biz.utils.PwdCheckUtil;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * @Title: UserServiceImpl.java
 * @Description：用户业务层处理类 ，实现UserService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class UserServiceImpl implements UserService {
	
	public static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	@Resource
	private AuthenticationConfig authenticationConfig;
    @Resource
    private UserDao userDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private RoleUserDao roleUserDao;
    @Resource
	private DeptDao deptDao;
    @Resource
    private RoleService roleService;

    /**
	  * @Title addUser 
	  * @Description (此为新增用户信息的接口的业务层处理逻辑，保存用户信息前会对必填入参进行非空校验，
	  * 校验通过后会对密码进行加密，并将加密后的密码及其他用户信息进行保存。)
	  * @param model (QxUserModel，新增的用户信息)
	  * @return (接口的执行情况)
	  */
	@Override
	@Transactional
	public ExecuteResult<String> addUser(UserAddDto dto) {
		try {
			String checkErr = checkUserInfoForAdd(dto) ;
			if(StringUtils.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("username",dto.getUsername());
		    paramsMap.put("status",0);
			QxUserModel userModel = userDao.queryObject(QxUserModel.class,"username=:username and status=:status",paramsMap);
		    if (ObjectUtil.isNotEmpty(userModel)) {
		    	return ExecuteResult.error("用户名已占用！", null);
		    }
		    //新添加的用户都给一个默认密码，用户第一次登录的时候提醒其进行密码修改
		    //加密方式一
			//BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		    //String password1 = passwordEncoder.encode(model.getPassword());
		    //加密方式二
			String password2 = EncryptUtil.encrypt(authenticationConfig.getDefaultPassword());
			//加密方式三，待稳定后启用
			//String password3 = KindCryptoUtils.encryptSM4(authenticationConfig.getDefaultPassword(), 3);
			
			QxUserModel model = new QxUserModel();
			BeanUtils.copyProperties(dto, model);
			model.setPassword(password2);
			model.setUserId(UuidUtils.buildID("U"));
			model.setFirstLoginFlag(0);//是否第一次登陆，0是，1不是,当用户初次登录改密后变为1
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(model.getCjsj());
			int count1 = userDao.insertUserInfo(model);
			if(count1<1) {
				throw new RuntimeException("用户未添加成功");
			}
			//给新增的用户增加默认的普通用户角色
			String roleId = roleDao.getRoleIdByRoleName("普通用户");
			if(StringUtil.isNotEmpty(roleId)) {
				QxRoleUserModel roleUserModel = new QxRoleUserModel();
				roleUserModel.setId(UuidUtils.buildID("UR"));
				roleUserModel.setRoleId(roleId);
				roleUserModel.setUserId(model.getUserId());
				int count2 = roleUserDao.insertRoleUser(roleUserModel);
				if(count2<1) {
					throw new RuntimeException("用户默认角色未添加成功");
				}
			}
			//增加部门关联信息
			QxUserDeptModel userDept = new QxUserDeptModel();
			userDept.setId(UuidUtils.buildID("UR"));
			userDept.setDepartmentId(model.getDepartmentId());
			userDept.setUserId(model.getUserId());
			int count3 = deptDao.insertUserDeptInfo(userDept);
			if(count3<1) {
				throw new RuntimeException("用户部门关联关系未添加成功");
			}
			return ExecuteResult.success("用户添加成功！");
		} catch (Exception e) {
			logger.error("新增用户异常，详情：",e);
			throw new RuntimeException("用户添加异常，原因：【"+e.getMessage()+"】");
		}
		
	}
	
	/**
	 * @Title queryUserInfosWithPage
	 * @Description (此为用户信息查询接口的具体业务逻辑处理方法，该方法可根据检索条件对符合条件的用户信息进行查询，
	 * 如果不传检索条件，默认查询全部，如果传分页信息，可对用户信息进行分页查询)
	 * @param model(UserQueryModel 用户查询检索条件信息)
	 * @return (符合检索条件的用户信息)
	 */
	@Override
	public ExecuteResult<List<QxUserSimpleModel>> queryUserInfosWithPage(UserQueryDto query) {
		logger.info("查询用户列表信息（分页），入参：【{}】", query);
	       PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
	       try {
			   //请求参数
	           Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
	           //拼接查询sql
	           Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"","");
			   
	           //根据拼接sql,调用dao层获取数据
	           ExecuteResult<List<QxUserSimpleModel>> result = userDao.queryObjectsWithPageInfo(QxUserSimpleModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
	           //给用户增加角色信息
	           List<QxUserSimpleModel> users = addRolesForUserInfo(result.getData());
	           result.setData(users);
	           return result;
	       } catch (Exception e) {
	           logger.error("查询用户列表信息（分页）出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
	           return ExecuteResult.error("查询失败",null);
	       }
	}
	
	@Override
	public ExecuteResult<QxUserModel> queryUserInfoById(String userId) {
		
		try {
   			if (StringUtil.isEmpty(userId)) {
   				return ExecuteResult.error("用户ID入参不能为空值", null);
   			}
   			logger.info("根据ID查询用户信息，记录id【{}】", userId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("userId", userId);
   			QxUserModel targetModel = userDao.queryObject(QxUserModel.class,"USERID=:userId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应用户信息!", null);
   			}else {
   				//根据ID查询用户部门信息
   				String  querySql = "select d.id,d.orgName from T_QX_DEPARTMENT d " + 
		           		"left join T_QX_USERDEPARTMENT ud  on d.ID = ud.DEPARTMENTID " + 
		           		"where ud.USERID = '"+userId+"'";
	           List<Map<String, Object>> list = deptDao.querySql(querySql);
	           if(list!=null&&list.size()>0) {
	        	   List<QxDepartmentModel> depts = JSONArray.parseArray(JSON.toJSONString(list), QxDepartmentModel.class);
	        	   if(depts!=null&&depts.size()>0) {
	        		   targetModel.setDepartmentId(depts.get(0).getId());
	        		   targetModel.setUserDepartment(depts.get(0).getOrgName());
	        	   }
	           }
   			   return ExecuteResult.success("查询成功",targetModel);
   			}
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询用户信息出现异常，原因：【{}】,详情：", userId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	
	@Override
	public ExecuteResult<List<UserGroupByDeptVo>> queryUserInfosGroupByDept() {
	       try {
	    	   //获取现有有效的部门
	    	   Map<String,Object> params = new HashMap<String,Object>();
	    	   params.put("status", "running");
	           List<QxDepartmentModel> depts = deptDao.queryObjects(QxDepartmentModel.class, "STATUS=:status  order by SORTINDEX asc",params);
	           
	           List<UserGroupByDeptVo>  deptTree = getDeptTree(depts,new ArrayList<UserGroupByDeptVo>(),"-1");
	    	   
	           //根据sql获取用户信息以及其关联的部门id信息
	           String  querySql = "select u.userid,u.username,u.name,ud.departmentid from T_QX_USER u " + 
		           		"left join T_QX_USERDEPARTMENT ud  on u.userid = ud.userid " + 
		           		"where u.status = '0' order by ud.user_order asc";
	           
	           List<Map<String, Object>> list = userDao.querySql(querySql);
	           if(list!=null&&list.size()>0) {
	        	   //将用户挂在部门树上，并重新组织成树信息返回
	        	   List<QxUserModel> users = JSONArray.parseArray(JSON.toJSONString(list), QxUserModel.class);
	        	   deptTree = getUserDeptTree(deptTree,users);
	        	   //针对未分配部门的用户，挂一个虚拟部门
	        	   List<UserGroupByDeptVo>  others = new ArrayList<UserGroupByDeptVo>(); 
	        	   for(QxUserModel user:users) {
	        		   if(StringUtil.isEmpty(user.getDepartmentId())) {
	        			  UserGroupByDeptVo tree = new UserGroupByDeptVo();
	          			  tree.setNodeId(user.getUserId());
	          			  tree.setNodeCode(user.getUsername());
	          			  tree.setNodeName(user.getName());
	          			  tree.setNodeType("user");
	          			  tree.setPid("other");
	          			  others.add(tree);
	        		   }
	        	   }
	        	   if(others.size()>0) {
	        		  UserGroupByDeptVo otherDept = new UserGroupByDeptVo();
	        		  otherDept.setNodeId("other");
	        		  otherDept.setNodeCode("nodept");
	        		  otherDept.setNodeName("未分配部门人员");
	        		  otherDept.setNodeType("dept");
	        		  otherDept.setPid("-1");
	        		  otherDept.setChildren(others);
	        		  deptTree.add(otherDept);
	        	   }
	           }
	           return ExecuteResult.success("查询成功", deptTree);
	       } catch (Exception e) {
	           logger.error("根据部门进行分组分级查询用户信息出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
	           return ExecuteResult.error("用户查询失败",null);
	       }
	}
	//根据指定的组织的父级id获取其子孙树结构信息
    private List<UserGroupByDeptVo> getDeptTree(List<QxDepartmentModel> depts,List<UserGroupByDeptVo> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<UserGroupByDeptVo>();
		}
		if(parentId!=null&&depts!=null&&depts.size()>0) {
     	   for(QxDepartmentModel dept:depts) {
     		   if(parentId.equals(dept.getPid())) {
     			  UserGroupByDeptVo tree = new UserGroupByDeptVo();
     			  tree.setNodeId(dept.getId());
     			  tree.setNodeCode(dept.getOrgCode());
     			  tree.setNodeName(dept.getOrgName());
     			  tree.setNodeType("dept");
     			  tree.setPid(parentId);
     			  tree.setChildren(getDeptTree(depts,new ArrayList<UserGroupByDeptVo>(),dept.getId()));
        		  trees.add(tree);
     		   }
     	   }
        }
		return trees;
	}
	
	 //根据指定的组织的父级id获取其子孙树结构信息
    private List<UserGroupByDeptVo> getUserDeptTree(List<UserGroupByDeptVo> deptTrees,List<QxUserModel> users) {
		if(deptTrees!=null&&deptTrees.size()>0) {
     	   for(UserGroupByDeptVo dept:deptTrees) {
     		   List<UserGroupByDeptVo> userChildren = new ArrayList<UserGroupByDeptVo>();
     		   for(QxUserModel user:users) {
     			   if(dept.getNodeId().equals(user.getDepartmentId())) {
     				  UserGroupByDeptVo tree = new UserGroupByDeptVo();
         			  tree.setNodeId(user.getUserId());
         			  tree.setNodeCode(user.getUsername());
         			  tree.setNodeName(user.getName());
         			  tree.setNodeType("user");
         			  tree.setPid(dept.getNodeId());
     				  userChildren.add(tree);
     			   }
     		   }
     		   List<UserGroupByDeptVo> deptChildren = dept.getChildren();
     		   if(deptChildren!=null&&deptChildren.size()>0) {
     			  deptChildren = getUserDeptTree(deptChildren,users);
     			  userChildren.addAll(deptChildren);
     			  dept.setChildren(userChildren);
     		   }else {
     			  dept.setChildren(userChildren);
     		   }
     	   }
        }
		return deptTrees;
	}
    
	
	//给查询到的用户添加角色信息
	private List<QxUserSimpleModel> addRolesForUserInfo(List<QxUserSimpleModel> users){
		if(users!=null&&users.size()>0) {
			//获取全部角色信息
			ExecuteResult<List<QxRoleModel>> roles = roleService.queryRolesByCondition(new RoleQueryDto());
			List<QxRoleModel> roleData = roles.getData();
			if("S".equals(roles.getResultCode())&&roleData!=null&&roleData.size()>0) {
				try {
					//获取全部用户角色匹配关系
					Map<String,Object> params = new HashMap<String,Object>();
					List<QxRoleUserModel> roleusers = roleDao.queryObjects(QxRoleUserModel.class, "",params);
					if(roleusers!=null&&roleusers.size()>0) {
						//给用户添加角色
						for(QxUserSimpleModel user:users) {
							List<QxRoleModel> rolecollect = new ArrayList<QxRoleModel>();
							Map<String,String> uniquerole = new HashMap<String,String>();
							String userid = user.getUserId();
							for(QxRoleUserModel roleuser:roleusers) {
								if(userid.equals(roleuser.getUserId())) {
									String roleid = roleuser.getRoleId();
									if(StringUtil.isEmpty(uniquerole.get(roleid))) {
										//对用户角色进行去重
										for(QxRoleModel role:roleData) {
											if(roleid.equals(role.getRoleId())) {
												rolecollect.add(role);
												uniquerole.put(roleid, roleid);
												break;
											}
										}
									}	
								}
							}
							user.setRoles(rolecollect);
						}
					}
					
				} catch (Exception e) {
					logger.error("给用户添加角色信息出现异常，异常详情：",e);
				}
			}
		}
		return users;
	}

	@Override
	public ExecuteResult<String> deleteUser(String userid,String loginUser) {
		try {
			if(StringUtils.isEmpty(userid)) {
    			return ExecuteResult.error("用户ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("userid",userid);
			QxUserModel originUser = userDao.queryObject(QxUserModel.class,"userid=:userid",paramsMap);
		    if (ObjectUtil.isEmpty(originUser)) {
		    	return ExecuteResult.error("未找到对应用户，无法执行删除！", null);
		    }
		    UserUpdateDto updateUser = new UserUpdateDto();
		    updateUser.setUserId(userid);
		    updateUser.setStatus(1);//状态，0在职，1离职
		    updateUser.setZhgxr(loginUser);
		    QxUserModel user = getUserModelByUpdateInfo(originUser,updateUser);
			userDao.updateUserInfo(user);
			return ExecuteResult.success("用户删除成功！");
		} catch (Exception e) {
			logger.error("删除用户异常，详情：",e);
			return ExecuteResult.error("删除用户异常，请稍后再试！", null);
		}
	}

	@Override
	@Transactional
	public ExecuteResult<String> updateUser(UserUpdateDto dto) {
		try {
			String userid = dto.getUserId();
			if(StringUtils.isEmpty(userid)) {
    			return ExecuteResult.error("用户ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("userid",userid);
			QxUserModel originUser = userDao.queryObject(QxUserModel.class,"userid=:userid",paramsMap);
		    if (ObjectUtil.isEmpty(originUser)) {
		    	return ExecuteResult.error("未找到对应用户，无法完成更新！", null);
		    }
		    //该方法不会对用户名及密码等信息进行修改，只更改一般用户数据
		    QxUserModel user = getUserModelByUpdateInfo(originUser,dto);
		    
		    //对关联部门进行更改
		     //更新部门关联关系，系统默认用户只有一个部门
	    	QxUserDeptModel ud = deptDao.queryObject(QxUserDeptModel.class, "USERID=:userid", paramsMap);
	    	if(ud!=null&&StringUtil.isNotEmpty(dto.getDepartmentId())&&!(ud.getDepartmentId().equals(dto.getDepartmentId()))) {
	    		ud.setDepartmentId(dto.getDepartmentId());
	    		deptDao.updateUserDeptInfo(ud);
	    	}
	    	//更新用户信息
			userDao.updateUserInfo(user);
			return ExecuteResult.success("用户更新成功！");
		} catch (Exception e) {
			logger.error("更新用户异常，详情：",e);
			throw new RuntimeException("更新用户异常，原因：【"+e.getMessage()+"】");
		}
	}
	
	@Override
	public ExecuteResult<String> changePasswordForFirstLogin(UserPasswordChangeDto model) {
		try {
			String checkError = checkChangePasswordParams(model);
			if(StringUtils.isNotEmpty(checkError)) {
				return ExecuteResult.error("入参校验不通过，详情："+checkError, null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("userid",model.getUserId());
			QxUserModel originUser = userDao.queryObject(QxUserModel.class,"userid=:userid",paramsMap);
		    if (ObjectUtil.isEmpty(originUser)) {
		    	return ExecuteResult.error("未找到对应用户，无法完成更新！", null);
		    }
		    String decryptOriginPassword = RSAUtils.decryptByPrivateKey(authenticationConfig.getRsaPrivateKey(), model.getOriginPassword());
		    String decryptTargetPassword = RSAUtils.decryptByPrivateKey(authenticationConfig.getRsaPrivateKey(), model.getTargetPassword());
		    boolean check = EncryptUtil.encrypt(decryptOriginPassword).equals(originUser.getPassword());
		    //待稳定后启用如下加密方式
		    //boolean check = KindCryptoUtils.encryptSM4(decryptOriginPassword, 3).equals(originUser.getPassword());
		    if(!check) {
		    	return ExecuteResult.error("原密码有误，无法完成更改！", null);
		    }
		    if (decryptOriginPassword.equals(decryptTargetPassword)) {
	            return ExecuteResult.error("新密码不能与原密码相同", null);
	        }
		    //检查长度
	        if (!PwdCheckUtil.checkPasswordComplex(decryptTargetPassword, 6, 18)) {
	            return ExecuteResult.error("请输入6-18位字母、数字、特殊符号中的至少两种的新密码", null);
	        }
	        String encryptTargetPassword = EncryptUtil.encrypt(decryptTargetPassword);
	        //待稳定后启用如下加密方式
	        //String encryptTargetPassword = KindCryptoUtils.encryptSM4(decryptTargetPassword, 3);
	        String updateSql = "update T_QX_USER set PASSWORD = '"+encryptTargetPassword+"',FIRST_LOGIN_FLAG='1',ZHGXR='"+model.getLoginUser()+"',ZHGXSJ='"+new Date()+"' where USERID = '"+model.getUserId()+"'";
			int count= userDao.updateSql(updateSql);
			if(count>0) {
				return ExecuteResult.success("密码更新成功！");
			}else {
				return ExecuteResult.error("密码更新失败！", null);
			}
		} catch (Exception e) {
			logger.error("更新用户异常，详情：",e);
			return ExecuteResult.error("密码更新异常，请稍后再试！", null);
		}
	}
	
	//校验新增的用户信息
	 private String checkUserInfoForAdd(UserAddDto model) {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("QxUserModel不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getUsername())) {
			 checkInfo.append("用户名或编码【username】不能为空;");		 
		 }else {
			 if(model.getUsername().length()>32) {
				 checkInfo.append("用户名或编码【username】不能不能超过32个长度;");
			 }
		 }
		 if(StringUtil.isEmpty(model.getName())) {
			 checkInfo.append("用户昵称【name】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getGender())) {
			 checkInfo.append("用户性别【gender】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getStatus())) {
			 model.setStatus(0);//状态，0在职，1离职或删除或无效,非必填,不传默认0
		 }
		 if(StringUtil.isEmpty(model.getDistrictcode())) {
			 checkInfo.append("用户隶属的行政区代码【districtcode】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getDepartmentId())) {
			 checkInfo.append("用户绑定部门Id【departmentId】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getCjr())) {
			 checkInfo.append("创建人用户编码【cjr】不能为空;");
		 }
		 return checkInfo.toString();
		 
	 }
	//校验用户密码修改参数信息
	 private String checkChangePasswordParams(UserPasswordChangeDto model) {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("UserPasswordChangeModel不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getUserId())) {
			 checkInfo.append("用户ID【userId】不能为空;");		 
		 }
		 if(StringUtil.isEmpty(model.getOriginPassword())) {
			 checkInfo.append("原始密码【originPassword】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getTargetPassword())) {
			 checkInfo.append("新密码【targetPassword】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getLoginUser())) {
			 checkInfo.append("当前登录人用户名【LoginUser】不能为空;");
		 }
		 return checkInfo.toString();
		 
	 }
	
	private QxUserModel getUserModelByUpdateInfo(QxUserModel originUser,UserUpdateDto updateUser) {
		
		 if(StringUtil.isNotEmpty(updateUser.getName())) {
			originUser.setName(updateUser.getName());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getGender())) {
			originUser.setGender(updateUser.getGender());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getEmail())) {
			originUser.setEmail(updateUser.getEmail());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getTel())) {
			originUser.setTel(updateUser.getTel());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getPhoto())) {
			originUser.setPhoto(updateUser.getPhoto());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getDistrictcode())) {
			originUser.setDistrictcode(updateUser.getDistrictcode());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getStatus())) {
			originUser.setStatus(updateUser.getStatus());
		 }
		 if(StringUtil.isNotEmpty(updateUser.getDescription())) {
			originUser.setDescription(updateUser.getDescription());
		 }
		 /*if(StringUtil.isNotEmpty(updateUser.getFirstLoginFlag())) {
			originUser.setFirstLoginFlag(updateUser.getFirstLoginFlag());
		 }*/
		 if(StringUtil.isNotEmpty(updateUser.getZhgxr())) {
			originUser.setZhgxr(updateUser.getZhgxr());
		 }
		 originUser.setZhgxsj(new Date());
		 return originUser;
	}
	
	
}
