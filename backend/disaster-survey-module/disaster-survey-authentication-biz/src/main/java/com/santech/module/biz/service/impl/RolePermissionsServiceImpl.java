package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.santech.core.framework.dao.ActionDao;
import com.santech.core.framework.dao.CatalogResourceDao;
import com.santech.core.framework.dao.RoleActionDao;
import com.santech.core.framework.dao.RoleCatalogDao;
import com.santech.core.framework.dao.RoleDao;
import com.santech.core.framework.dao.RoleRouterDao;
import com.santech.core.framework.dao.RoleUserDao;
import com.santech.core.framework.entity.model.JcActionModel;
import com.santech.core.framework.entity.model.QxRoleActionModel;
import com.santech.core.framework.entity.model.QxRoleCatalogModel;
import com.santech.core.framework.entity.model.QxRoleModel;
import com.santech.core.framework.entity.model.QxRoleRouterModel;
import com.santech.core.framework.entity.model.QxRoleUserModel;
import com.santech.core.framework.entity.model.ZyCatalogModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.RolePermissionsQueryDto;
import com.santech.module.biz.entity.dto.RolePermissionsSaveDto;
import com.santech.module.biz.entity.vo.CataLogPermissionsVo;
import com.santech.module.biz.entity.vo.NavPermissionsVo;
import com.santech.module.biz.entity.vo.RolePermissionsManagerVo;
import com.santech.module.biz.entity.vo.RolePermissionsVo;
import com.santech.module.biz.service.RolePermissionsService;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Title: RolePermissionsServiceImpl.java 
 * @Description：用户权限业务层接口类 ，实现RolePermissionsService接口
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class RolePermissionsServiceImpl implements RolePermissionsService {
	
	private static final Logger logger = LoggerFactory.getLogger(RolePermissionsServiceImpl.class);
	 
    @Resource
    private ActionDao actionDao;
    
    @Resource
    private CatalogResourceDao catalogResourceDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private RoleUserDao roleUserDao;
    
    @Resource
    private RoleActionDao roleActionDao;
    
    @Resource
    private RoleRouterDao roleRouterDao;
    
    @Autowired
    private RoleCatalogDao roleCatalogDao;
    
	@Override
	public ExecuteResult<RolePermissionsVo> queryPermissionsByRoles(RolePermissionsQueryDto query) {
		logger.info("查询角色权限（菜单功能及资源目录等）信息，入参：【{}】", query);
		try {
			//校验参数
			String adminFlag = query.getAdminFlag();//管理员标识,0非管理员，1管理员
			String roleIds = query.getRoles();
			if(StringUtil.isEmpty(adminFlag)) {
				return ExecuteResult.error("管理员标识不能为空", null);
			}
			//1菜单功能权限
			//获取权限下对应的目标功能项
			List<JcActionModel> actions = getActionData(adminFlag,roleIds);
            //根据过滤后的功能项组装成页面需要的数据
			List<NavPermissionsVo> navs = getActionPermissionsTree(actions,new ArrayList<NavPermissionsVo>(),"-1","根目录");
			//2资源目录权限
			List<ZyCatalogModel> catalogs = null;
			List<CataLogPermissionsVo> catas = null;
			try {
				catalogs = getCatalogData(adminFlag, roleIds);
				catas = getCatalogPermissionsTree(catalogs,new ArrayList<CataLogPermissionsVo>(), "-1");
			} catch (Exception e) {
				logger.error("获取用户资源目录权限出现异常，异常详情：",e);
				//如果异常，按空目录返回
				catas = new ArrayList<CataLogPermissionsVo>();
			}
			//组织权限返回
			RolePermissionsVo result = new RolePermissionsVo();
			result.setNavList(navs);
			result.setCatalogList(catas);
            return ExecuteResult.success("查询成功", result);
		} catch (Exception e) {
			logger.error("查询角色权限信息（菜单功能及资源目录等）出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
		    return ExecuteResult.error(e.getMessage(),null);
		}
	}
	private List<JcActionModel> getActionData(String adminFlag,String roleIds) throws Exception{
		//获取所有目标功能项
		List<JcActionModel> actions = actionDao.getAction();
		//获取角色关联的所有功能项
		if ("0".equals(adminFlag)&&StringUtil.isNotEmpty(actions)) {
			if(StringUtil.isNotEmpty(roleIds)) {
				List<QxRoleActionModel> roleactions = roleActionDao.getActionByRoleId(roleIds);
				Set<String> actionIds = null;
				if(roleactions!=null&&roleactions.size()>0) {
					actionIds = roleactions.stream().filter(x->StrUtil.isNotEmpty(x.getActionId())).map(x -> x.getActionId()).collect(Collectors.toSet());
					if(actionIds!=null&&actionIds.size()>0) {
						List<JcActionModel> collectActions = new ArrayList<JcActionModel>();
						for(String actionid:actionIds) {
							for(JcActionModel action:actions) {
								if(actionid.equals(action.getId())) {
									collectActions.add(action);
									break;
								}
							}
						}
						actions = collectActions;
					}
				}else {
		            throw new RuntimeException("暂无访问权限，请联系管理员");
				}
			}else{
				throw new RuntimeException("暂无访问权限，请联系管理员");
			}
			
        }
		//对数据按序号进行升序排序
		if(actions!=null&&actions.size()>1) {
			Collections.sort(actions, new Comparator<JcActionModel>() {
	            @Override
	            public int compare(JcActionModel o1, JcActionModel o2) {
	                //升序
	                return o1.getSortindex().compareTo(o2.getSortindex());
	            }
	        });
		}
		return actions;
	}
	
    
	//获取菜单功能树
	private List<NavPermissionsVo> getActionPermissionsTree(List<JcActionModel> actions,List<NavPermissionsVo> trees,String parentId,String parentName) {
		if(trees==null) {
			trees = new ArrayList<NavPermissionsVo>();
		}
		if(parentId!=null&&actions!=null&&actions.size()>0) {
     	   for(JcActionModel action:actions) {
     		   if(parentId.equals(action.getParentId())) {
     			  NavPermissionsVo model = new NavPermissionsVo();
        		   model.setId(action.getId());
        		   model.setName(action.getName());
        		   model.setTitle(action.getAlias());
        		   model.setIcon(action.getImage());
        		   model.setPath(action.getRouterpath());
        		   model.setParentId(action.getParentId());
        		   model.setExecutetool(action.getExecutetool());
        		   model.setNodetype(action.getNodetype());
        		   model.setSortindex(action.getSortindex());
        		   model.setType(parentName); 
        		   model.setActiongroup(action.getActiongroup());
        		   //if("checktools".equalsIgnoreCase(action.getActiongroup())) {
        			   model.setConfigure(action.getConfigure());
        		   //}
        		   model.setChildrens(getActionPermissionsTree(actions,new ArrayList<NavPermissionsVo>(),action.getId(),action.getAlias()));
        		   trees.add(model);
     		   }
     	   }
        }
		return trees;
		
	}
	//获取权限内的资源目录信息
	private List<ZyCatalogModel> getCatalogData(String adminFlag,String roleIds) throws Exception {
		//获取全部目录信息
		List<ZyCatalogModel> catalogs= catalogResourceDao.getCatalog();
		if ("0".equals(adminFlag)&&StringUtil.isNotEmpty(catalogs)) {
			//不为管理员时，对资源目录进行过滤
			if(StringUtil.isNotEmpty(roleIds)) {
				// 获取当前登录用户的目录权限
	            List<QxRoleCatalogModel> roleaCatalogs = roleCatalogDao.getCatalogByRoleId(roleIds);
	            Set<String> catalogIds = null;
				if(roleaCatalogs!=null&&roleaCatalogs.size()>0) {
					catalogIds = roleaCatalogs.stream().filter(x->StrUtil.isNotEmpty(x.getCatalogId())).map(x -> x.getCatalogId()).collect(Collectors.toSet());
					if(catalogIds!=null&&catalogIds.size()>0) {
						List<ZyCatalogModel> collectActions = new ArrayList<ZyCatalogModel>();
						for(String catalogid:catalogIds) {
							for(ZyCatalogModel catalog:catalogs) {
								if(catalogid.equals(catalog.getId())) {
									collectActions.add(catalog);
									break;
								}
							}
						}
						catalogs = collectActions;
						//对数据按序号进行升序排序
						if(catalogs!=null&&catalogs.size()>1) {
							Collections.sort(catalogs, new Comparator<ZyCatalogModel>() {
					            @Override
					            public int compare(ZyCatalogModel o1, ZyCatalogModel o2) {
					                //升序
					                return o1.getSortIndex().compareTo(o2.getSortIndex());
					            }
					        });
						}
						
					}
				}else {
		            //有角色，但角色未赋予资源目录权限，则亦返回空的资源目录
		            catalogs = new ArrayList<ZyCatalogModel>();
				}
			}else {
				//无角色信息，则返回空的资源目录
				catalogs = new ArrayList<ZyCatalogModel>();
			}
		}else {
			if(catalogs==null) {
				catalogs = new ArrayList<ZyCatalogModel>();
			}
		}
		return catalogs;
	}
	
	//组织成资源目录树
	private List<CataLogPermissionsVo> getCatalogPermissionsTree(List<ZyCatalogModel> catalogs,List<CataLogPermissionsVo> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<CataLogPermissionsVo>();
		}
		if(parentId!=null&&catalogs!=null&&catalogs.size()>0) {
     	   for(ZyCatalogModel cataLog:catalogs) {
     		   if(parentId.equals(cataLog.getParentId())) {
     			  CataLogPermissionsVo model = new CataLogPermissionsVo();
        		   model.setId(cataLog.getId());
        		   model.setParentId(cataLog.getParentId());
        		   model.setName(cataLog.getName());
        		   model.setType(cataLog.getType()); 
        		   model.setThemeId(cataLog.getThemeId());
        		   model.setSortIndex(cataLog.getSortIndex());
        		   model.setChildrens(getCatalogPermissionsTree(catalogs,new ArrayList<CataLogPermissionsVo>(),cataLog.getId()));
        		   trees.add(model);
     		   }
     	   }
        }
		return trees;
	}
	@Override
	public ExecuteResult<RolePermissionsManagerVo> queryPermissionsByRoleIdForManager(String roleId) {
		try {
			if(StringUtil.isEmpty(roleId)) {
				return ExecuteResult.error("角色ID不能为空", null);
			}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("roleId",roleId);
			//根据角色查询功能权限
			List<QxRoleActionModel> ras = roleActionDao.queryObjects(QxRoleActionModel.class, "ROLEID=:roleId", paramsMap);
			//根据角色查询路由权限
			List<QxRoleRouterModel> rrs = roleRouterDao.queryObjects(QxRoleRouterModel.class, "ROLEID=:roleId", paramsMap);
			//根据角色查询资源目录权限
			List<QxRoleCatalogModel> rcs = roleCatalogDao.queryObjects(QxRoleCatalogModel.class, "ROLEID=:roleId", paramsMap);
			//根据角色查询关联用户
			List<QxRoleUserModel> rus = roleUserDao.queryObjects(QxRoleUserModel.class, "ROLEID=:roleId", paramsMap);
			 // 路由权限id
	        List<String> routerIds = new ArrayList<String>();
	        if (rrs != null) {
	        	routerIds = rrs.stream().map(QxRoleRouterModel::getRouterId).collect(Collectors.toList());
	        }
	        // 功能权限id
	        List<String> actionIds = new ArrayList<String>();
	        if (ras != null) {
	        	actionIds = ras.stream().map(QxRoleActionModel::getActionId).collect(Collectors.toList());
	        }
	        // 目录权限id
	        List<String> catalogIds = new ArrayList<String>();
	        if (rcs != null) {
	        	catalogIds = rcs.stream().map(QxRoleCatalogModel::getCatalogId).collect(Collectors.toList());
	        }
	        // 用户id
	        List<String> userIds = new ArrayList<String>();
	        if (rus != null) {
	        	userIds = rus.stream().map(QxRoleUserModel::getUserId).collect(Collectors.toList());
	        }
	        RolePermissionsManagerVo result = new RolePermissionsManagerVo();
	        result.setActionPermission(actionIds);
	        result.setRouterPermission(routerIds);
	        result.setCatalogPermission(catalogIds);
	        result.setUserPermission(userIds);
			return ExecuteResult.success("查询成功", result);
		} catch (Exception e) {
			logger.error("根据角色ID【{}】查询权限信息出现异常，详情：",roleId,e);
			return ExecuteResult.error("查询失败", null);
		}
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveRoleUserPermission(RolePermissionsSaveDto model) {
		try {
			String roleId = model.getRoleId();
			if(StringUtil.isEmpty(roleId)) {
    			return ExecuteResult.error("角色ID不能为空", null);
    		}
			//循环添加新的绑定
		   String userIds = model.getUserIds();
		   if(StringUtil.isNotEmpty(userIds)) {
			   //删除全部关联用户
			   roleUserDao.deleteRoleUserInfoByRoleId(roleId);
			    String[] arr = userIds.split(",");
	            for (int i = 0;i<arr.length;i++) {
	            	QxRoleUserModel ru = new QxRoleUserModel();
	                ru.setId(UuidUtils.buildID("RU"));
	                ru.setRoleId(roleId);
	                ru.setUserId(arr[i]);
	                roleUserDao.insertRoleUser(ru);
	            }
		    }
		    return ExecuteResult.success("角色用户关联关系添加成功！");
		} catch (Exception e) {
			logger.error("保存角色用户信息异常，详情：",e);
			throw new RuntimeException("保存角色用户信息失败");
		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveRoleActionPermission(RolePermissionsSaveDto model) {
		try {
			String roleId = model.getRoleId();
			if(StringUtil.isEmpty(roleId)) {
    			return ExecuteResult.error("角色ID不能为空", null);
    		}
			String actionIds = model.getActionIds();
			
			//循环添加新的绑定
		   if(StringUtil.isNotEmpty(actionIds)) {
			     //对其子角色中包含了其不含的功能项进行删除
				delChildRoleActionPermission(roleId,actionIds);
				//删除本角色全部关联功能项
			    roleActionDao.deleteRoleActionInfoByRoleId(roleId);
			    String[] arr = actionIds.split(",");
	            for (int i = 0;i<arr.length;i++) {
	            	QxRoleActionModel ra = new QxRoleActionModel();
	            	ra.setId(UuidUtils.buildID("RA"));
	            	ra.setRoleId(roleId);
	            	ra.setActionId(arr[i]);
	            	roleActionDao.insertRoleAction(ra);
	            }
		   }
		    return ExecuteResult.success("角色功能项关联关系添加成功！");
		} catch (Exception e) {
			logger.error("保存角色功能项信息异常，详情：",e);
			throw new RuntimeException("保存角色功能项信息失败");
		}
	}
	
	
	public void delChildRoleActionPermission(String roleId,String actionIds) throws Exception {
		if(StringUtil.isNotEmpty(actionIds)) {
			// 新的权限
			String[] newActionId = actionIds.split(",");
	        // 旧的权限
	        List<String> oldActionId = new ArrayList<String>();
	        List<QxRoleActionModel> roleActionModelList = roleActionDao.getActionByRoleId(roleId);
	        if (StringUtil.isNotEmpty(roleActionModelList)) {
	            oldActionId =  roleActionModelList.stream().map(QxRoleActionModel::getActionId).collect(Collectors.toList());
	            // 比较两个数组，看少了哪些元素,即少了哪些权限，多的不做判断处理
	            List<String> cancelList = new ArrayList<String>();
	            for (String anOld : oldActionId) {
	                if (!ArrayUtil.contains(newActionId,anOld)) {
	                    cancelList.add(anOld);
	                }
	            }
	            if (ObjectUtil.isNotEmpty(cancelList)) {
	                // 所有子角色id
	                List<String> childRoleId = new ArrayList<String>();
	                // 获取出所有角色数据，然后递归获取所有子角色
	                List<QxRoleModel> roleModelList = roleDao.queryObjects(QxRoleModel.class, "", null);
	                // 递归获取当前节点的所有子节点
	                getAllChildId(childRoleId,roleModelList,roleId);
	                if (ObjectUtil.isNotEmpty(childRoleId)) {
	                    // 要删除的子角色权限
	                    List<QxRoleActionModel> delList = new ArrayList<QxRoleActionModel>();
	                    for (String i : cancelList) {
	                        for (String j : childRoleId) {
	                            QxRoleActionModel roleActionModel = new QxRoleActionModel();
	                            roleActionModel.setRoleId(j);
	                            roleActionModel.setActionId(i);
	                            delList.add(roleActionModel);
	                        }
	                    }
	                    roleActionDao.delRoleAction(delList);
	                }
	            }
	        }
		}
        
    }
	
	private void getAllChildId(List<String> childIds, List<QxRoleModel> roleModelList, String roleId) {
        for (QxRoleModel r : roleModelList) {
            if (Objects.equals(r.getParentId(), roleId)) {
                // 递归遍历下一级
            	childIds.add(r.getRoleId());
                getAllChildId(childIds, roleModelList, r.getRoleId());
            }
        }
    }
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveRoleRouterPermission(RolePermissionsSaveDto model) {
		try {
			String roleId = model.getRoleId();
			if(StringUtil.isEmpty(roleId)) {
    			return ExecuteResult.error("角色ID不能为空", null);
    		}
			String routerIds = model.getRouterIds();
			//循环添加新的绑定
		   if(StringUtil.isNotEmpty(routerIds)) {
			   	//对其子角色中包含了其不含的运维路由项进行删除
				delChildRoleRouterPermission(roleId,routerIds);
				//删除本角色全部关联功能项
			    roleRouterDao.deleteRoleRouterInfoByRoleId(roleId);
			    String[] arr = routerIds.split(",");
	            for (int i = 0;i<arr.length;i++) {
	            	QxRoleRouterModel rt = new QxRoleRouterModel();
	            	rt.setId(UuidUtils.buildID("RR"));
	            	rt.setRoleId(roleId);
	            	rt.setRouterId(arr[i]);
	            	roleRouterDao.insertRoleRouter(rt);
	            }
		   }
		    return ExecuteResult.success("角色运维路由关联关系添加成功！");
		} catch (Exception e) {
			logger.error("保存角色运维路由信息异常，详情：",e);
			throw new RuntimeException("保存角色运维路由信息失败");
		}
	}

	public void delChildRoleRouterPermission(String roleId,String routerIds) throws Exception {
		if(StringUtil.isNotEmpty(routerIds)) {
			// 新的权限
			String[] newRouterId = routerIds.split(",");
	        // 旧的权限
	        List<String> oldRouterId = new ArrayList<String>();
	        List<QxRoleRouterModel> roleRouterModelList = roleRouterDao.getRouterByRoleId(roleId);
	        if (StringUtil.isNotEmpty(roleRouterModelList)) {
	            oldRouterId =  roleRouterModelList.stream().map(QxRoleRouterModel::getRouterId).collect(Collectors.toList());
	            // 比较两个数组，看少了哪些元素,即少了哪些权限，多的不做判断处理
	            List<String> cancelList = new ArrayList<String>();
	            for (String anOld : oldRouterId) {
	                if (!ArrayUtil.contains(newRouterId,anOld)) {
	                    cancelList.add(anOld);
	                }
	            }
	            if (ObjectUtil.isNotEmpty(cancelList)) {
	                // 所有子角色id
	                List<String> childRoleId = new ArrayList<String>();
	                // 获取出所有角色数据，然后递归获取所有子角色
	                List<QxRoleModel> roleModelList = roleDao.queryObjects(QxRoleModel.class, "", null);
	                // 递归获取当前节点的所有子节点
	                getAllChildId(childRoleId,roleModelList,roleId);
	                if (ObjectUtil.isNotEmpty(childRoleId)) {
	                    // 要删除的子角色权限
	                    List<QxRoleRouterModel> delList = new ArrayList<QxRoleRouterModel>();
	                    for (String i : cancelList) {
	                        for (String j : childRoleId) {
	                            QxRoleRouterModel roleRouterModel = new QxRoleRouterModel();
	                            roleRouterModel.setRoleId(j);
	                            roleRouterModel.setRouterId(i);
	                            delList.add(roleRouterModel);
	                        }
	                    }
	                    roleRouterDao.delRoleRouter(delList);
	                }
	            }
	        }
		}
    }
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveRoleCatalogPermission(RolePermissionsSaveDto model) {
		try {
			String roleId = model.getRoleId();
			if(StringUtil.isEmpty(roleId)) {
    			return ExecuteResult.error("角色ID不能为空", null);
    		}
			String catalogIds = model.getCatalogIds();
			//循环添加新的绑定
		   if(StringUtil.isNotEmpty(catalogIds)) {
			   	//对其子角色中包含了其不含的资源目录项进行删除
				delChildRoleCatalogPermission(roleId,catalogIds);
				//删除本角色全部关联功能项
			   roleCatalogDao.deleteRoleCatalogInfoByRoleId(roleId);
			    String[] arr = catalogIds.split(",");
	            for (int i = 0;i<arr.length;i++) {
	            	QxRoleCatalogModel rc = new QxRoleCatalogModel();
	            	rc.setId(UuidUtils.buildID("RC"));
	            	rc.setRoleId(roleId);
	            	rc.setCatalogId(arr[i]);
	            	roleCatalogDao.insertRoleCatalog(rc);
	            }
		   }
		    return ExecuteResult.success("角色资源目录关联关系添加成功！");
		} catch (Exception e) {
			logger.error("保存角色资源目录信息异常，详情：",e);
			throw new RuntimeException("保存角色资源目录信息失败");
		}
	}
	public void delChildRoleCatalogPermission(String roleId,String catalogIds) throws Exception {
		if(StringUtil.isNotEmpty(catalogIds)) {
			// 新的权限
			String[] newCatalogId = catalogIds.split(",");
	        // 旧的权限
	        List<String> oldCatalogId = new ArrayList<String>();
	        List<QxRoleCatalogModel> roleCatalogModelList = roleCatalogDao.getCatalogByRoleId(roleId);
	        if (StringUtil.isNotEmpty(roleCatalogModelList)) {
	            oldCatalogId =  roleCatalogModelList.stream().map(QxRoleCatalogModel::getCatalogId).collect(Collectors.toList());
	            // 比较两个数组，看少了哪些元素,即少了哪些权限，多的不做判断处理
	            List<String> cancelList = new ArrayList<String>();
	            for (String anOld : oldCatalogId) {
	                if (!ArrayUtil.contains(newCatalogId,anOld)) {
	                    cancelList.add(anOld);
	                }
	            }
	            if (ObjectUtil.isNotEmpty(cancelList)) {
	                // 所有子角色id
	                List<String> childRoleId = new ArrayList<String>();
	                // 获取出所有角色数据，然后递归获取所有子角色
	                List<QxRoleModel> roleModelList = roleDao.queryObjects(QxRoleModel.class, "", null);
	                // 递归获取当前节点的所有子节点
	                getAllChildId(childRoleId,roleModelList,roleId);
	                if (ObjectUtil.isNotEmpty(childRoleId)) {
	                    // 要删除的子角色权限
	                    List<QxRoleCatalogModel> delList = new ArrayList<QxRoleCatalogModel>();
	                    for (String i : cancelList) {
	                        for (String j : childRoleId) {
	                        	QxRoleCatalogModel roleCatalogModel = new QxRoleCatalogModel();
	                            roleCatalogModel.setRoleId(j);
	                            roleCatalogModel.setCatalogId(i);
	                            delList.add(roleCatalogModel);
	                        }
	                    }
	                    roleCatalogDao.delRoleCatalog(delList);
	                }
	            }
	        }
		}
    }
	

}
