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
import com.santech.core.framework.dao.ActionDao;
import com.santech.core.framework.dao.RoleActionDao;
import com.santech.core.framework.entity.model.JcActionModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.ActionAddDto;
import com.santech.module.biz.entity.dto.ActionQueryDto;
import com.santech.module.biz.entity.dto.ActionUpdateDto;
import com.santech.module.biz.service.ActionService;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class ActionServiceImpl implements ActionService {
	 private static final  Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class);

    @Autowired
    private ActionDao actionDao;
    @Autowired
    private RoleActionDao roleActionDao;
    

    @Override
	public ExecuteResult<List<JcActionModel>> queryActionInfos(ActionQueryDto query) {
    	logger.info("分页查询功能列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
        	ExecuteResult<List<JcActionModel>> actionResult = null;
        	if(!"1".equals(query.getAdminFlag())) {
        		//非管理员
        		String loginUserId = query.getLoginUserId();
        		if(StringUtil.isEmpty(loginUserId)) {
        			return ExecuteResult.error("当前登录用户ID不能为空",null);
            	}
        		//只能查询其具有权限的功能项
        		String selectSql = "select distinct * from T_JC_ACTION a inner join T_QX_ROLEACTION ra on a.id = ra.actionid ";
        		String otherConditionSql = " and ra.roleid in (select ru.roleid from T_QX_ROLEUSER ru where ru.userid ='"+loginUserId+"')";
        		//请求参数
                Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
                //拼接查询sql
                Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params," a.sortindex desc",otherConditionSql);
                
                String allSql = selectSql +" where "+ dbParams.get("sql");
                ExecuteResult<List<Map<String, Object>>> actionMapResult =  actionDao.selectActionInfos(allSql, (Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
                if("S".equalsIgnoreCase(actionMapResult.getResultCode())) {
	            	List<Map<String, Object>> datas = actionMapResult.getData();
	            	if(StringUtil.isNotEmpty(datas)) {
	            		List<JcActionModel> models = JSONArray.parseArray(JSON.toJSONString(datas), JcActionModel.class);
	            		//添加所属上级名称
	            		models = addParentName(models);
	            		actionResult = ExecuteResult.success("查询成功", models, actionMapResult.getPageInfo());
	            	}else {
	            		actionResult = ExecuteResult.success("查询成功", new ArrayList<JcActionModel>(), actionMapResult.getPageInfo());
	            	}
                }else {
                	actionResult = ExecuteResult.error("查询失败", null);
                }
        	}else {
        		//是管理员
        		//请求参数
                Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
                //拼接查询sql
                Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"sortindex desc","");
                //根据拼接sql,调用dao层获取数据
                actionResult =  actionDao.queryObjectsWithPageInfo(JcActionModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
                if("S".equalsIgnoreCase(actionResult.getResultCode())) {
                	List<JcActionModel> datas = actionResult.getData();
	            	if(StringUtil.isNotEmpty(datas)) {
	            		datas = addParentName(datas);
	            		actionResult.setData(datas);
	            	}
                }
        	}
        	return actionResult;
        } catch (Exception e) {
            logger.error("分页查询功能列表信息异常，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
    private List<JcActionModel> addParentName(List<JcActionModel> actions){
    	if(actions!=null&&actions.size()>0) {
    		for(JcActionModel m1:actions) {
    			String pid = m1.getParentId();
    			if(StringUtil.isEmpty(pid)||pid.equals("-1")) {
    				m1.setParentName("");
    			}else {
    				for(JcActionModel m2:actions) {
        				if(m2.getId().equals(pid)) {
        					m1.setParentName(StringUtil.isEmpty(m2.getAlias())?m2.getName():m2.getAlias());
        					break;
        				}
        			}
    			}
    		}
    	}
    	return actions;
    }
    
	@Override
	public ExecuteResult<JcActionModel> queryActionInfoByName(String actionName) {
		try {
			if(StringUtil.isEmpty(actionName)) {
    			return ExecuteResult.error("功能名称不能为空", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("name",actionName);
		    List<JcActionModel> actionModels = actionDao.queryObjects(JcActionModel.class,"NAME=:name",paramsMap);
		    if (StringUtil.isEmpty(actionModels)&&actionModels.size()<1) {
		    	return ExecuteResult.error("根据功能名称未查询到目标记录！", null);
		    }
			return ExecuteResult.success("查询成功！",actionModels.get(0));
		} catch (Exception e) {
			logger.error("查询异常，详情：",e);
			return ExecuteResult.error("查询失败！", null);
		}
	}
	
    @Override
	public ExecuteResult<String> saveActionInfo(ActionAddDto dto) {
    	try {
			String checkErr = checkActionInfoForAdd(dto) ;
			if(StringUtil.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("name",dto.getName());
		    paramsMap.put("parentId",dto.getParentId());
		    List<JcActionModel> actionModels = actionDao.queryObjects(JcActionModel.class,"PARENTID=:parentId and NAME=:name",paramsMap);
		    if (StringUtil.isNotEmpty(actionModels)&&actionModels.size()>0) {
		    	return ExecuteResult.error("同一父级下的功能名称不能重复！", null);
		    }
		    JcActionModel model = new JcActionModel();
		    BeanUtils.copyProperties(dto, model);
			model.setId(UuidUtils.buildID("A"));
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(model.getCjsj());
			actionDao.insertActionInfo(model);
			return ExecuteResult.success("功能项添加成功！");
		} catch (Exception e) {
			logger.error("新增功能项异常，详情：",e);
			return ExecuteResult.error("新增功能项异常，请稍后再试！", null);
		}
	}
    
    @Override
	public ExecuteResult<String> updateActionInfo(ActionUpdateDto dto) {
    	try {
			String actionId = dto.getId();
			if(StringUtils.isEmpty(actionId)) {
    			return ExecuteResult.error("功能项ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("actionId",actionId);
		    JcActionModel originAction = actionDao.queryObject(JcActionModel.class,"ID=:actionId",paramsMap);
		    if (StringUtil.isEmpty(originAction)) {
		    	return ExecuteResult.error("未找到对应功能项，无法完成更新！", null);
		    }
		    //检查功能项是否包含重名
		    if(StringUtil.isNotEmpty(dto.getName())&&!(dto.getName().equals(originAction.getName()))) {
			    paramsMap.put("name",dto.getName());
			    if(StringUtil.isNotEmpty(dto.getParentId())&&!(dto.getParentId().equals(originAction.getParentId()))) {
			    	paramsMap.put("parentId",dto.getParentId());
			    }else {
			    	paramsMap.put("parentId",originAction.getParentId());
			    }
			    List<JcActionModel> actionModels = actionDao.queryObjects(JcActionModel.class,"PARENTID=:parentId and ID!=:actionId and NAME=:name",paramsMap);
			    if (StringUtil.isNotEmpty(actionModels)&&actionModels.size()>0) {
			    	return ExecuteResult.error("要更新的功能项名称在目标父级下已占用！", null);
			    }
		    }
		    JcActionModel action = getActionModelByUpdateInfo(originAction,dto);
		    actionDao.updateActionInfo(action);
			return ExecuteResult.success("功能项更新成功！");
		} catch (Exception e) {
			logger.error("更新功能项异常，详情：",e);
			return ExecuteResult.success("更新功能项异常，原因：【"+e.getMessage()+"】");
		}
	}
    
    
    @Override
    @Transactional
	public ExecuteResult<String> deleteActionInfo(String actionId, String loginUser) {
    	try {
			if (StringUtil.isEmpty(actionId)) {
				return ExecuteResult.error("功能项ID入参不能为空值", null);
			}
			logger.info("删除功能项信息，记录id【{}】", actionId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("id", actionId);
			JcActionModel targetModel = actionDao.queryObject(JcActionModel.class,"id=:id", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定功能项信息", actionId);
				return ExecuteResult.error("已无对应功能项信息!", null);
			}
			int count1 = actionDao.deleteActionInfoById(actionId);
			if (count1> 0) {
				roleActionDao.deleteRoleActionInfoByActionId(actionId);
				return ExecuteResult.success("删除成功 ");
			}else {
				throw new RuntimeException("删除功能项失败");
			}
		} catch (Exception e) {
			logger.error("删除功能项【{}】出现异常，原因：【{}】,详情：", actionId,e.getMessage(), e);
			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
		}
	}
    @Override
	public ExecuteResult<List<JcActionModel>> queryActionTreeInfos(String loginUserId, String adminFlag) {
		try {
        	if(StringUtil.isEmpty(adminFlag)) {
    			return ExecuteResult.error("管理员标识为空，未登录,请先登录",null);
        	}
        	List<JcActionModel> models = null;
        	if(!"1".equals(adminFlag)) {
        		//非管理员
        		if(StringUtil.isEmpty(loginUserId)) {
        			return ExecuteResult.error("当前登录用户ID不能为空",null);
            	}
        		//只能查询其具有权限的运维路由菜单项
        		String selectSql = "select distinct * from T_JC_ACTION r inner join T_QX_ROLEACTION rr on r.id = rr.actionid ";
        		String otherConditionSql = " and rr.roleid in (select ru.roleid from T_QX_ROLEUSER ru where ru.userid ='"+loginUserId+"')";
        		//请求参数
                Map<String,Object> params = new HashMap<String,Object>();
                params.put("eq_visible", "1");//查询未做隐藏的
                //拼接查询sql
                Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params," r.SORTINDEX asc",otherConditionSql);
                String allSql = selectSql +" where "+ dbParams.get("sql");
                List<Map<String, Object>> actionMapResult =  actionDao.selectRouterInfosWithoutPage(allSql, (Map<String, Object>) dbParams.get("paramsMap"));
            	if(StringUtil.isNotEmpty(actionMapResult)) {
            		models = JSONArray.parseArray(JSON.toJSONString(actionMapResult), JcActionModel.class);
            	}else {
            		models = new ArrayList<JcActionModel>();
            	}
        	}else {
        		//是管理员
        		Map<String,Object> params = new HashMap<String,Object>();
 	    	    params.put("hidden", "1");//查询未做隐藏的
 	            models = actionDao.queryObjects(JcActionModel.class, "VISIBLE=:hidden",params);
        	}
        	//获取树结构
        	List<JcActionModel> tree = getActionTree(models,new ArrayList<JcActionModel>(),"-1");
        	
        	return ExecuteResult.success("查询成功", tree);
        } catch (Exception e) {
            logger.error("分页查询功能列表信息异常，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
    //根据指定的功能的父级id获取其子孙树结构信息
    private List<JcActionModel> getActionTree(List<JcActionModel> actions,List<JcActionModel> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<JcActionModel>();
		}
		if(parentId!=null&&actions!=null&&actions.size()>0) {
     	   for(JcActionModel action:actions) {
     		   if(parentId.equals(action.getParentId())) {
     			  action.setChildren(getActionTree(actions,new ArrayList<JcActionModel>(),action.getId()));
        		  trees.add(action);
     		   }
     	   }
        }
		return trees;
		
	}
     //校验新增的部门信息
  	 private String checkActionInfoForAdd(ActionAddDto model) throws Exception {
  		 StringBuilder checkInfo = new StringBuilder("");
  		 if(StringUtil.isEmpty(model)) {
  			 checkInfo.append("ActionAddDto不能为空;");
  			 return checkInfo.toString();
  		 }
  		 if(StringUtil.isEmpty(model.getName())) {
  			 checkInfo.append("功能名称【name】不能为空;");
  		 }
  		 if(StringUtil.isEmpty(model.getAlias())) {
  			 checkInfo.append("功能别名【alias】不能为空;");
  		 }
  		 if(StringUtil.isEmpty(model.getNodetype())) {
  			 checkInfo.append("节点类型【nodetype】不能为空;");
  		 }
  		 if(StringUtil.isEmpty(model.getSortindex())||model.getSortindex()<1) {
  			 //checkInfo.append("排序索引【sortindex】不能为空;");
  			 //如果为空，设置为默认的值（库中最大排序加1）
			 model.setSortindex(actionDao.getObjectSortIndexForIntegerType("T_JC_ACTION", "SORTINDEX"));
  		 }
  		 if(StringUtil.isEmpty(model.getSystem())) {
 			 checkInfo.append("所属子系统【system】不能为空;");
 		 }
  		 if(StringUtil.isEmpty(model.getCjr())) {
  			 checkInfo.append("部门创建人【cjr】不能为空;");
  		 }
  		 if(StringUtil.isEmpty(model.getVisible())) {
  			model.setVisible("1");//默认可见
 		 }
  		 if(StringUtil.isEmpty(model.getParentId())) {
			//如果pid为空，默认为添加根节点功能
			model.setParentId("-1");
		 }
  		 return checkInfo.toString();
  		 
  	 }
  	 
  	 private JcActionModel getActionModelByUpdateInfo(JcActionModel originAction,ActionUpdateDto updateAction) {
  			
  		 if(StringUtil.isNotEmpty(updateAction.getName())) {
  			originAction.setName(updateAction.getName());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getAlias())) {
  			originAction.setAlias(updateAction.getAlias());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getNodetype())) {
  			originAction.setNodetype(updateAction.getNodetype());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getExecutetool())) {
  			originAction.setExecutetool(updateAction.getExecutetool());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getActiongroup())) {
  			originAction.setActiongroup(updateAction.getActiongroup());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getSortindex())) {
  			originAction.setSortindex(updateAction.getSortindex());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getImage())) {
  			originAction.setImage(updateAction.getImage());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getRouterpath())) {
  			originAction.setRouterpath(updateAction.getRouterpath());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getVisible())) {
  			originAction.setVisible(updateAction.getVisible());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getConfigure())) {
  			originAction.setConfigure(updateAction.getConfigure());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getParentId())) {
  			originAction.setParentId(updateAction.getParentId());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getSystem())) {
  			originAction.setSystem(updateAction.getSystem());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getDescription())) {
  			originAction.setDescription(updateAction.getDescription());
  		 }
  		 if(StringUtil.isNotEmpty(updateAction.getZhgxr())) {
  			originAction.setZhgxr(updateAction.getZhgxr());
  		 }
  		 originAction.setZhgxsj(new Date());
  		return originAction;
  	}

	
    

}
