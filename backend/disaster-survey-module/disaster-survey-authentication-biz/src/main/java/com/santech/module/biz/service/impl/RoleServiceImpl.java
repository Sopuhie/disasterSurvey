package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.santech.core.framework.dao.RoleActionDao;
import com.santech.core.framework.dao.RoleCatalogDao;
import com.santech.core.framework.dao.RoleDao;
import com.santech.core.framework.dao.RoleRouterDao;
import com.santech.core.framework.dao.RoleUserDao;
import com.santech.core.framework.entity.model.QxRoleActionModel;
import com.santech.core.framework.entity.model.QxRoleCatalogModel;
import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.core.framework.entity.model.QxRoleRouterModel;
import com.santech.core.framework.entity.model.QxRoleUserModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.RoleAddDto;
import com.santech.module.biz.entity.dto.RoleQueryDto;
import com.santech.module.biz.entity.dto.RoleUpdateDto;
import com.santech.module.biz.service.RoleService;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * @Title: RoleServiceImpl.java 
 * @Description：角色管理业务层接口类 ，实现RoleService接口
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class RoleServiceImpl implements RoleService {
	
	private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
	 
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private RoleUserDao roleUserDao;
    @Autowired
    private RoleRouterDao roleRouterDao;
    @Autowired
    private RoleActionDao roleActionDao;
    @Autowired
    private RoleCatalogDao roleCatalogDao;
    
    /**
	  * @Title queryRolesWithPage 
	  * @Description (此为根据筛选条件进行批量分页查询角色信息接口的业务层方法，该方法会针对入参生成一条对应的查询sql，
	  * 以此sql进行记录的查询并返回。查询的记录默认按优先级字段升序返回，可进行分页)
	  * @param query (RoleQueryModel,查询条件参数)
	  * @return (接口的执行情况及符合条件的角色记录信息)
	  */
	@Override
	public ExecuteResult<List<QxRoleModel>> queryRolesWithPage(RoleQueryDto query) {
		logger.info("查询角色列表信息（分页），入参：【{}】", query);
		try {
			PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
			
			//请求参数
	         Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
	           //拼接查询sql,不返回超级管理员的角色信息
	         Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SINDEX asc","");
			if("1".equals(query.getAdminFlag())) {
				//是管理员
				return roleDao.queryObjectsWithPageInfo(QxRoleModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
			}else {
				//非管理员，查询其子角色予以返回
				String roles = query.getRoleids();
				if(StringUtil.isEmpty(roles)) {
					return ExecuteResult.error("当前登录用户的角色ID不能为空",null);
				}
				String mainSql = "WITH RECURSIVE t AS ("
						+" select r1.ROLEID,r1.PARENTID,r1.ROLENAME,r1.DISPLAYNAME,r1.DESCRIPTION,r1.SINDEX  from T_QX_ROLE r1"
						+" WHERE r1.PARENTID in  (" +SqlUtil.getInSql(roles)+")  and r1.ROLEID not in  (" +SqlUtil.getInSql(roles)+")" 
						+" UNION ALL"
					    +" select r2.ROLEID,r2.PARENTID,r2.ROLENAME,r2.DISPLAYNAME,r2.DESCRIPTION,r2.SINDEX  from T_QX_ROLE r2"
					    +" JOIN t ON t.ROLEID = r2.PARENTID )"
					    +" SELECT distinct  * FROM t";
				
				 String allSql = mainSql +" where "+ dbParams.get("sql");
				 ExecuteResult<List<Map<String, Object>>> roleMapResult =  roleDao.selectRoleInfosWithPage(allSql, (Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
                if("S".equalsIgnoreCase(roleMapResult.getResultCode())) {
	            	List<Map<String, Object>> datas = roleMapResult.getData();
	            	if(StringUtil.isNotEmpty(datas)) {
	            		List<QxRoleModel> models = JSONArray.parseArray(JSON.toJSONString(datas), QxRoleModel.class);
	            		return ExecuteResult.success("查询成功", models, roleMapResult.getPageInfo());
	            	}else {
	            		return ExecuteResult.success("查询成功", new ArrayList<QxRoleModel>(), roleMapResult.getPageInfo());
	            	}
                }else {
                	return ExecuteResult.error("查询失败", null);
                }
			}
		} catch (Exception e) {
			logger.error("查询角色列表信息（分页）出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
		    return ExecuteResult.error("查询失败",null);
		}
	}
	
	@Override
	public ExecuteResult<List<QxRoleModel>> queryRolesByCondition(RoleQueryDto query) {
		 logger.info("查询角色列表信息（不分页），入参：【{}】", query);
	     try {
	         //请求参数
	         Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
	         //拼接查询sql
	         Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"sindex asc","");
	         //根据拼接sql,调用dao层获取数据
	         List<QxRoleModel> roles = roleDao.queryObjects(QxRoleModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
	         return ExecuteResult.success("查询成功", roles);
	     } catch (Exception e) {
	         logger.error("查询角色列表信息（不分页）出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
	         return ExecuteResult.error("查询失败",null);
	     }
	}
    
	@Override
	public ExecuteResult<String> saveRoleInfo(RoleAddDto dto) {
		try {
			String checkErr = checkRoleInfoForAdd(dto) ;
			if(StringUtil.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("roleName",dto.getRoleName());
		    List<QxRoleModel> roleModels = roleDao.queryObjects(QxRoleModel.class,"ROLENAME=:roleName",paramsMap);
		    if (ObjectUtil.isNotEmpty(roleModels)&&roleModels.size()>0) {
		    	return ExecuteResult.error("该角色名称已存在，不能重复！", null);
		    }
		    QxRoleModel model = new QxRoleModel();
		    BeanUtils.copyProperties(dto, model);
			model.setRoleId(UuidUtils.buildID("R"));
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(model.getCjsj());
			roleDao.insertRoleInfo(model);
			return ExecuteResult.success("角色添加成功！");
		} catch (Exception e) {
			logger.error("新增角色异常，详情：",e);
			return ExecuteResult.error("新增角色异常，请稍后再试！", null);
		}
	}



	@Override
	public ExecuteResult<String> updateRoleInfo(RoleUpdateDto dto) {
		try {
			String roleId = dto.getRoleId();
			if(StringUtils.isEmpty(roleId)) {
    			return ExecuteResult.error("角色ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("roleId",roleId);
		    QxRoleModel originRole = roleDao.queryObject(QxRoleModel.class,"ROLEID=:roleId",paramsMap);
		    if (StringUtil.isEmpty(originRole)) {
		    	return ExecuteResult.error("未找到对应角色，无法完成更新！", null);
		    }
		    //检查角色项是否包含重名
		    if(StringUtil.isNotEmpty(dto.getRoleName())&&!(dto.getRoleName().equals(originRole.getRoleName()))) {
			    paramsMap.put("roleName",dto.getRoleName());
			    List<QxRoleModel> routerModels = roleDao.queryObjects(QxRoleModel.class,"ROLEID!=:roleId and ROLENAME=:roleName",paramsMap);
			    if (StringUtil.isNotEmpty(routerModels)&&routerModels.size()>0) {
			    	return ExecuteResult.error("新的角色名称已被占用，请确认！", null);
			    }
		    }
		    QxRoleModel router = getRoleModelByUpdateInfo(originRole,dto);
		    roleDao.updateRoleInfo(router);
			return ExecuteResult.success("角色更新成功！");
		} catch (Exception e) {
			logger.error("更新角色异常，详情：",e);
			return ExecuteResult.success("更新角色异常，原因：【"+e.getMessage()+"】");
		}
	}



	@Override
	@Transactional
	public ExecuteResult<String> deleteRoleInfo(String roleId, String loginUser) {
		try {
			if (StringUtil.isEmpty(roleId)) {
				return ExecuteResult.error("角色项ID入参不能为空值", null);
			}
			logger.info("删除角色项信息，记录id【{}】", roleId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("roleId", roleId);
			QxRoleModel targetModel = roleDao.queryObject(QxRoleModel.class,"ROLEID=:roleId", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定角色信息", roleId);
				return ExecuteResult.error("已无对应角色信息!", null);
			}
			//角色删除后，得保证用户角色绑定关系得清除
			int count1 = roleDao.deleteRoleInfoById(roleId);
			if (count1> 0) {
				int count2 = 0;
				int count3 = 0;
				int count4 = 0;
				int count5 = 0;
				//用户角色绑定关系删除
				List<QxRoleUserModel> rus= roleUserDao.queryObjects(QxRoleUserModel.class, "ROLEID=:roleId", paramsOne);
				if(rus!=null&&rus.size()>0) {
					count2 = roleUserDao.deleteRoleUserInfoByRoleId(roleId);
					if(count2<1) {
						throw new RuntimeException("删除用户角色的绑定关系失败");
					}
				}
				//运维路由菜单角色绑定关系删除
				List<QxRoleRouterModel> rrs= roleRouterDao.queryObjects(QxRoleRouterModel.class, "ROLEID=:roleId", paramsOne);
				if(rrs!=null&&rrs.size()>0) {
					count3 = roleRouterDao.deleteRoleRouterInfoByRoleId(roleId);
					if(count3<1) {
						throw new RuntimeException("删除运维路由菜单角色的绑定关系失败");
					}
				}
				//功能菜单角色绑定关系删除
				List<QxRoleActionModel> ras= roleActionDao.queryObjects(QxRoleActionModel.class, "ROLEID=:roleId", paramsOne);
				if(ras!=null&&ras.size()>0) {
					count4 = roleActionDao.deleteRoleActionInfoByRoleId(roleId);
					if(count4<1) {
						throw new RuntimeException("删除功能菜单角色的绑定关系失败");
					}
				}
				//资源目录角色绑定关系删除
				List<QxRoleCatalogModel> rcs= roleCatalogDao.queryObjects(QxRoleCatalogModel.class, "ROLEID=:roleId", paramsOne);
				if(rcs!=null&&rcs.size()>0) {
					count5 = roleCatalogDao.deleteRoleCatalogInfoByRoleId(roleId);
					if(count5<1) {
						throw new RuntimeException("删除资源目录角色的绑定关系失败");
					}
				}
				return ExecuteResult.success("删除成功 ");
			}else {
				throw new RuntimeException("删除角色项失败");
			}
		} catch (Exception e) {
			logger.error("删除角色【{}】出现异常，原因：【{}】,详情：", roleId,e.getMessage(), e);
			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
		}
	}
	
	
	//校验新增的路由信息
	 private String checkRoleInfoForAdd(RoleAddDto model) throws Exception {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("QxRoleModel不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getParentId())) {
			 checkInfo.append("上级角色ID【parentId】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getRoleName())) {
			 checkInfo.append("角色名称【path】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getSindex())||model.getSindex()<1) {
			// checkInfo.append("排序索引【sindex】不能为空;");
			//如果为空，复制为默认的值（库中最大排序加1）
			 model.setSindex(roleDao.getObjectSortIndexForIntegerType("T_QX_ROLE", "SINDEX"));
		 }
		 if(StringUtil.isEmpty(model.getCjr())) {
			 checkInfo.append("创建人【cjr】不能为空;");
		 }
		 return checkInfo.toString();
		 
	 }
	 
	 private QxRoleModel getRoleModelByUpdateInfo(QxRoleModel originRole,RoleUpdateDto updateRole) {
			
		 if(StringUtil.isNotEmpty(updateRole.getRoleName())) {
			originRole.setRoleName(updateRole.getRoleName());
		 }
		 if(StringUtil.isNotEmpty(updateRole.getDisplayName())) {
			originRole.setDisplayName(updateRole.getDisplayName());
		 }
		 if(StringUtil.isNotEmpty(updateRole.getDescription())) {
			originRole.setDescription(updateRole.getDescription());
		 }
		 if(StringUtil.isNotEmpty(updateRole.getSindex())) {
			originRole.setSindex(updateRole.getSindex());
		 }
		 if(StringUtil.isNotEmpty(updateRole.getZhgxr())) {
			originRole.setZhgxr(updateRole.getZhgxr());
 		 }
		 originRole.setZhgxsj(new Date());
		return originRole;
	}

	

}
