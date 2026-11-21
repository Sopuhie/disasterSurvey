package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.santech.core.framework.dao.RoleRouterDao;
import com.santech.core.framework.dao.RouterDao;
import com.santech.core.framework.entity.model.JcRouterModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.RouterAddDto;
import com.santech.module.biz.entity.dto.RouterQueryDto;
import com.santech.module.biz.entity.dto.RouterUpdateDto;
import com.santech.module.biz.service.RouterService;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class RouterServiceImpl implements RouterService {
	
	private static final  Logger logger = LoggerFactory.getLogger(RouterServiceImpl.class);
	
    @Resource
    private RouterDao routerDao;
    @Resource
    private RoleRouterDao roleRouterDao;
    
    @Override
	public ExecuteResult<List<JcRouterModel>> queryRouterInfosWithPage(RouterQueryDto query) {
    	logger.info("查询运维路由菜单列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SORTINDEX asc","");
            //根据拼接sql,调用dao层获取数据
            return routerDao.queryObjectsWithPageInfo(JcRouterModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询运维路由菜单列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
    
    @Override
	public ExecuteResult<String> saveRouterInfo(RouterAddDto dto) {
    	try {
			String checkErr = checkRouterInfoForAdd(dto) ;
			if(StringUtil.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("title",dto.getName());
		    List<JcRouterModel> routerModels = routerDao.queryObjects(JcRouterModel.class,"TITLE=:title",paramsMap);
		    if (ObjectUtil.isNotEmpty(routerModels)&&routerModels.size()>0) {
		    	return ExecuteResult.error("运维路由菜单标题不能重复！", null);
		    }
		    JcRouterModel model = new JcRouterModel();
		    BeanUtils.copyProperties(dto, model);
			model.setRouterId(UuidUtils.buildID("RT"));
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(model.getCjsj());
			routerDao.insertRouterInfo(model);
			return ExecuteResult.success("运维路由菜单项添加成功！");
		} catch (Exception e) {
			logger.error("新增运维路由菜单项异常，详情：",e);
			return ExecuteResult.error("新增运维路由菜单项异常，请稍后再试！", null);
		}
	}

	@Override
	public ExecuteResult<String> updateRouterInfo(RouterUpdateDto dto) {
		try {
			String routerId = dto.getRouterId();
			if(StringUtils.isEmpty(routerId)) {
    			return ExecuteResult.error("运维路由菜单项ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("routerId",routerId);
		    JcRouterModel originRouter = routerDao.queryObject(JcRouterModel.class,"ROUTERID=:routerId",paramsMap);
		    if (StringUtil.isEmpty(originRouter)) {
		    	return ExecuteResult.error("未找到对应运维路由菜单项，无法完成更新！", null);
		    }
		    //检查运维路由菜单项是否包含重名
		    if(StringUtil.isNotEmpty(dto.getTitle())&&!(dto.getTitle().equals(originRouter.getTitle()))) {
			    paramsMap.put("title",dto.getTitle());
			    List<JcRouterModel> routerModels = routerDao.queryObjects(JcRouterModel.class,"ROUTERID!=:routerId and TITLE=:title",paramsMap);
			    if (StringUtil.isNotEmpty(routerModels)&&routerModels.size()>0) {
			    	return ExecuteResult.error("要更新的运维路由菜单项名称已占用！", null);
			    }
		    }
		    JcRouterModel router = getRouterModelByUpdateInfo(originRouter,dto);
		    routerDao.updateRouterInfo(router);
			return ExecuteResult.success("运维路由菜单项更新成功！");
		} catch (Exception e) {
			logger.error("更新运维路由菜单项异常，详情：",e);
			return ExecuteResult.success("更新运维路由菜单项异常，原因：【"+e.getMessage()+"】");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> deleteRouterInfo(String routerId, String loginUser) {
		try {
			if (StringUtil.isEmpty(routerId)) {
				return ExecuteResult.error("运维路由菜单项ID入参不能为空值", null);
			}
			logger.info("删除运维路由菜单项信息，记录id【{}】", routerId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("routerId", routerId);
			JcRouterModel targetModel = routerDao.queryObject(JcRouterModel.class,"ROUTERID=:routerId", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定运维路由菜单项信息", routerId);
				return ExecuteResult.error("已无对应运维路由菜单项信息!", null);
			}
			int count1 = routerDao.deleteRouterInfoById(routerId);
			if (count1> 0) {
				roleRouterDao.deleteRoleRouterInfoByRouterId(routerId);
				return ExecuteResult.success("删除成功 ");
			}else {
				throw new RuntimeException("删除运维路由菜单项失败");
			}
		} catch (Exception e) {
			logger.error("删除运维路由菜单项【{}】出现异常，原因：【{}】,详情：", routerId,e.getMessage(), e);
			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
		}
	}
    

	@Override
	public ExecuteResult<List<JcRouterModel>> queryRouterTreeInfos(String loginUserId, String adminFlag) {
		try {
        	if(StringUtil.isEmpty(adminFlag)) {
    			return ExecuteResult.error("管理员标识为空，未登录,请先登录",null);
        	}
        	logger.info("查询路由树，入参【{}】【{}】",loginUserId,adminFlag);
        	List<JcRouterModel> models = null;
        	if(!"1".equals(adminFlag)) {
        		//非管理员
        		if(StringUtil.isEmpty(loginUserId)) {
        			return ExecuteResult.error("当前登录用户ID不能为空",null);
            	}
        		//只能查询其具有权限的运维路由菜单项
        		String selectSql = "select distinct r.* from T_JC_ROUTER r inner join T_QX_ROLEROUTER rr on r.ROUTERID = rr.ROUTERID ";
        		String otherConditionSql = " and rr.roleid in (select ru.roleid from T_QX_ROLEUSER ru where ru.userid ='"+loginUserId+"')";
        		//请求参数
                Map<String,Object> params = new HashMap<String,Object>();
                params.put("eq_hidden", 0);//查询未做隐藏的
                //拼接查询sql
                Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params," r.SORTINDEX asc",otherConditionSql);
                String allSql = selectSql +" where "+ dbParams.get("sql");
                List<Map<String, Object>> routerMapResult =  routerDao.selectRouterInfos(allSql, (Map<String, Object>) dbParams.get("paramsMap"));
            	if(StringUtil.isNotEmpty(routerMapResult)) {
            		models = JSONArray.parseArray(JSON.toJSONString(routerMapResult), JcRouterModel.class);
            	}else {
            		models = new ArrayList<JcRouterModel>();
            	}
        	}else {
        		//是管理员
        		Map<String,Object> params = new HashMap<String,Object>();
 	    	    params.put("hidden", 0);//查询未做隐藏的
 	            models = routerDao.queryObjects(JcRouterModel.class, "HIDDEN=:hidden order by SORTINDEX asc",params);
        	}
        	//获取树结构
        	List<JcRouterModel> tree = getRouterTree(models,new ArrayList<JcRouterModel>(),"-1");
        	
        	return ExecuteResult.success("查询成功", tree);
        } catch (Exception e) {
            logger.error("分页查询功能列表信息异常，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
	
	//根据指定的路由的父级id获取其子孙树结构信息
    private List<JcRouterModel> getRouterTree(List<JcRouterModel> routers,List<JcRouterModel> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<JcRouterModel>();
		}
		if(parentId!=null&&routers!=null&&routers.size()>0) {
     	   for(JcRouterModel router:routers) {
     		   if(parentId.equals(router.getParentId())) {
     			  router.setChildren(getRouterTree(routers,new ArrayList<JcRouterModel>(),router.getRouterId()));
        		  trees.add(router);
     		   }
     	   }
        }
		return trees;
		
	}
    
    //校验新增的路由信息
 	 private String checkRouterInfoForAdd(RouterAddDto model) throws Exception {
 		 StringBuilder checkInfo = new StringBuilder("");
 		 if(StringUtil.isEmpty(model)) {
 			 checkInfo.append("RouterAddDto不能为空;");
 			 return checkInfo.toString();
 		 }
 		 if(StringUtil.isEmpty(model.getTitle())) {
 			 checkInfo.append("标题名称【title】不能为空;");
 		 }
 		 if(StringUtil.isEmpty(model.getPath())) {
 			 checkInfo.append("路径【path】不能为空;");
 		 }
 		 if(StringUtil.isEmpty(model.getName())) {
 			 checkInfo.append("路由名称【name】不能为空;");
 		 }
 		 if(StringUtil.isEmpty(model.getSortIndex())||model.getSortIndex()<1) {
 			 //checkInfo.append("排序索引【sortIndex】不能为空;");
 			//如果为空，复制为默认的值（库中最大排序加1）
			 model.setSortIndex(routerDao.getObjectSortIndexForIntegerType("T_JC_ROUTER", "SORTINDEX"));
 		 }
 		 if(StringUtil.isEmpty(model.getComponent())) {
			 checkInfo.append("组件【component】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getHidden())) {
			 checkInfo.append("是否隐藏【hidden】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getCjr())) {
 			 checkInfo.append("创建人【cjr】不能为空;");
 		 }
 		 if(StringUtil.isEmpty(model.getParentId())) {
			//如果pid为空，默认为添加根节点功能
			model.setParentId("-1");
		 }
 		 return checkInfo.toString();
 		 
 	 }
 	 
 	 private JcRouterModel getRouterModelByUpdateInfo(JcRouterModel originRouter,RouterUpdateDto updateRouter) {
 			
 		 if(StringUtil.isNotEmpty(updateRouter.getTitle())) {
 			originRouter.setTitle(updateRouter.getTitle());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getIcon())) {
 			originRouter.setIcon(updateRouter.getIcon());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getPath())) {
 			originRouter.setPath(updateRouter.getPath());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getName())) {
 			originRouter.setName(updateRouter.getName());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getNoCache())) {
 			originRouter.setNoCache(updateRouter.getNoCache());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getSortIndex())) {
 			originRouter.setSortIndex(updateRouter.getSortIndex());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getComponent())) {
 			originRouter.setComponent(updateRouter.getComponent());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getRedirect())) {
 			originRouter.setRedirect(updateRouter.getRedirect());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getParentId())) {
 			originRouter.setParentId(updateRouter.getParentId());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getHidden())) {
 			originRouter.setHidden(updateRouter.getHidden());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getAlwaysShow())) {
 			originRouter.setAlwaysShow(updateRouter.getAlwaysShow());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getIframe())) {
 			originRouter.setIframe(updateRouter.getIframe());
 		 }
 		 if(StringUtil.isNotEmpty(updateRouter.getZhgxr())) {
 			originRouter.setZhgxr(updateRouter.getZhgxr());
  		 }
 		 originRouter.setZhgxsj(new Date());
 		return originRouter;
 	}

}
