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

import com.santech.core.framework.dao.CatalogResourceDao;
import com.santech.core.framework.dao.RoleCatalogDao;
import com.santech.core.framework.entity.model.ZyCatalogModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.CatalogAddDto;
import com.santech.module.biz.entity.dto.CatalogResourceQueryDto;
import com.santech.module.biz.entity.dto.CatalogUpdateDto;
import com.santech.module.biz.entity.vo.CatalogResourceTreeVo;
import com.santech.module.biz.service.CatalogResourceService;

@Service
public class CatalogResourceServiceImpl implements CatalogResourceService {
	private static final  Logger logger = LoggerFactory.getLogger(CatalogResourceServiceImpl.class);
	
    @Resource
    private CatalogResourceDao catalogResourceDao;
    
    @Resource
    private RoleCatalogDao roleCatalogDao;

    
    @Override
   	public ExecuteResult<List<CatalogResourceTreeVo>> queryCatalogResourceTreeInfos(CatalogResourceQueryDto query) {
       		logger.info("查询数据目录资源列表信息，入参：【{}】", query);
           try {
               //请求参数
               Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
               //拼接查询sql
               Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SORTINDEX asc","");
               //根据拼接sql,调用dao层获取数据
               List<ZyCatalogModel> catalogDatas = catalogResourceDao.queryObjects(ZyCatalogModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
               //生成目录树
               List<CatalogResourceTreeVo> themeTree = getCatalogTree(catalogDatas,new ArrayList<CatalogResourceTreeVo>(),"-1");
               
               
               return ExecuteResult.success("查询成功", themeTree);
           } catch (Exception e) {
               logger.error("查询数据目录资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
               return ExecuteResult.error("查询失败",null);
           }
   	}
    
  //根据指定的目录的父级id获取其子孙树结构信息
    private List<CatalogResourceTreeVo> getCatalogTree(List<ZyCatalogModel> catalogs,List<CatalogResourceTreeVo> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<CatalogResourceTreeVo>();
		}
		if(parentId!=null&&catalogs!=null&&catalogs.size()>0) {
     	   for(ZyCatalogModel catalog:catalogs) {
     		   if(parentId.equals(catalog.getParentId())) {
     			  CatalogResourceTreeVo tree = new CatalogResourceTreeVo();
     			  tree.setNodeId(catalog.getId());
     			  tree.setNodeName(catalog.getName());
     			  tree.setNodeType(catalog.getType());
     			  tree.setPid(parentId);
     			  tree.setThemeId(catalog.getThemeId());
     			  tree.setSortIndex(catalog.getSortIndex());
     			  tree.setChildren(getCatalogTree(catalogs,new ArrayList<CatalogResourceTreeVo>(),catalog.getId()));
        		  trees.add(tree);
     		   }
     	   }
        }
		return trees;
	}
    
       
       
    @Override
   	public ExecuteResult<ZyCatalogModel> queryCatalogResourceInfoById(String catalogId) {
       	try {
   			if (StringUtil.isEmpty(catalogId)) {
   				return ExecuteResult.error("数据目录资源ID入参不能为空值", null);
   			}
   			logger.info("根据ID查询数据目录资源信息，记录id【{}】", catalogId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("catalogId", catalogId);
   			ZyCatalogModel targetModel = catalogResourceDao.queryObject(ZyCatalogModel.class,"ID=:catalogId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应数据目录资源信息!", null);
   			}else {
   				return ExecuteResult.success("查询成功",targetModel);
   			}
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询数据目录资源信息出现异常，原因：【{}】,详情：", catalogId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
   	}
    
       
    @Override
   	public ExecuteResult<String> saveCatalogResourceInfo(CatalogAddDto dto) {
       	try {
   			String checkErr = checkCatalogResourceInfoForAdd(dto) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			
   		    String pid = dto.getParentId();
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("name",dto.getName());
   		    paramsMap.put("pid",pid);
   		    List<ZyCatalogModel> catalogModels = catalogResourceDao.queryObjects(ZyCatalogModel.class,"NAME=:name and PARENTID=:pid",paramsMap);
   		    if (StringUtil.isNotEmpty(catalogModels)&&catalogModels.size()>0) {
   		    	return ExecuteResult.error("同一个目录下，目录资源名称不能重复！", null);
   		    }
   		    //验证是否是在目录上挂专题
   		    if(StringUtil.isNotEmpty(pid)&&!"-1".equals(pid)) {
   		    	ZyCatalogModel pCatalog = catalogResourceDao.queryObject(ZyCatalogModel.class,"ID=:pid",paramsMap);
   		    	if(pCatalog==null) {
   		    		return ExecuteResult.error("父级目录不存在！", null);
   		    	}
   		    	if(!"folder".equalsIgnoreCase(pCatalog.getType())) {
   		    		return ExecuteResult.error("不可在非目录的节点添加子节点！", null);
   		    	}
   		    }
   		    ZyCatalogModel model = new ZyCatalogModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("CR"));
   			model.setCjsj(new Date());
   			model.setZhgxr(model.getCjr());
   			model.setZhgxsj(model.getCjsj());
   			catalogResourceDao.insertCatalogResourceInfo(model);
   			return ExecuteResult.success("数据目录资源添加成功！");
   		} catch (Exception e) {
   			logger.error("新增数据目录资源异常，详情：",e);
   			return ExecuteResult.error("新增数据目录资源异常，请稍后再试", null);
   		}
   	}
       
    @Override
   	public ExecuteResult<String> updateCatalogResourceInfo(CatalogUpdateDto dto) {
       	try {
   			String catalogId = dto.getId();
   			if(StringUtils.isEmpty(catalogId)) {
       			return ExecuteResult.error("数据目录资源ID不能为空！", null);
       		}
   			if(StringUtil.isEmpty(dto.getZhgxr())) {
   				return ExecuteResult.error("数据更新人不能为空！", null);
   	 		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("catalogId",catalogId);
   		    ZyCatalogModel originCatalog = catalogResourceDao.queryObject(ZyCatalogModel.class,"ID=:catalogId",paramsMap);
   		    if (StringUtil.isEmpty(originCatalog)) {
   		    	return ExecuteResult.error("未找到对应数据目录资源项，无法完成更新！", null);
   		    }
   		    //检查数据目录资源是否包含重名
   		    if(StringUtil.isNotEmpty(dto.getName())&&!(dto.getName().equals(originCatalog.getName()))) {
   			    paramsMap.put("catalogName",dto.getName());
   			    List<ZyCatalogModel> catalogModels = catalogResourceDao.queryObjects(ZyCatalogModel.class,"ID!=:catalogId and NAME=:catalogName",paramsMap);
   			    if (StringUtil.isNotEmpty(catalogModels)&&catalogModels.size()>0) {
   			    	return ExecuteResult.error("要更新的数据目录资源名称已占用！", null);
   			    }
   		    }
   		    ZyCatalogModel service = getCatalogResourceModelByUpdateInfo(originCatalog,dto);
   		    catalogResourceDao.updateCatalogResourceInfo(service);
   			return ExecuteResult.success("数据目录资源更新成功！");
   		} catch (Exception e) {
   			logger.error("更新数据目录资源异常，详情：",e);
   			return ExecuteResult.error("更新数据目录资源异常，请稍后再试", null);
   		}
   	}


   	@Override
   	@Transactional(rollbackFor = Exception.class)
   	public ExecuteResult<String> deleteCatalogResourceInfo(String catalogId,String loginUser) {
   		try {
   			if (StringUtil.isEmpty(catalogId)) {
   				return ExecuteResult.error("数据目录资源ID入参不能为空值", null);
   			}
   			logger.info("删除数据目录资源信息，记录id【{}】", catalogId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("catalogId", catalogId);
   			ZyCatalogModel targetModel = catalogResourceDao.queryObject(ZyCatalogModel.class,"ID=:catalogId", paramsOne);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定数据目录资源信息", catalogId);
   				return ExecuteResult.error("已无对应数据目录资源信息!", null);
   			}
   			int count1 = catalogResourceDao.deleteCatalogResourceInfoById(catalogId);
   			if (count1> 0) {
   		         // 删除数据目录与角色的绑定关系
   				roleCatalogDao.deleteRoleCatalogInfoByCatalogId(catalogId);
   				return ExecuteResult.success("删除成功");
   			}else {
   				throw new RuntimeException("删除数据目录资源失败");
   			}
   		} catch (Exception e) {
   			logger.error("删除数据目录资源【{}】出现异常，原因：【{}】,详情：", catalogId,e.getMessage(), e);
   			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
   		}
   	}
   	
    //获取目标更新的数据目录资源信息
   	private ZyCatalogModel getCatalogResourceModelByUpdateInfo(ZyCatalogModel origin,CatalogUpdateDto update) {
   			
   		 if(StringUtil.isNotEmpty(update.getName())) {
   			origin.setName(update.getName());
   		 }
   		 if(StringUtil.isNotEmpty(update.getParentId())) {
   			origin.setParentId(update.getParentId());
   		 }
   		 if(StringUtil.isNotEmpty(update.getSortIndex())) {
   			origin.setSortIndex(update.getSortIndex());
   		 }
   		 /*if(StringUtil.isNotEmpty(update.getProject())) {
   			origin.setProject(update.getProject());
   		 }*/
   		 if(StringUtil.isNotEmpty(update.getZhgxr())) {
   			origin.setZhgxr(update.getZhgxr());
    	 }
   		 origin.setZhgxsj(new Date());
   		return origin;
   	}
     //校验新增的目录信息
   	 private String checkCatalogResourceInfoForAdd(CatalogAddDto model) throws Exception {
   		 StringBuilder checkInfo = new StringBuilder("");
   		 if(StringUtil.isEmpty(model)) {
   			 checkInfo.append("CatalogAddDto不能为空;");
   			 return checkInfo.toString();
   		 }
   		 if(StringUtil.isEmpty(model.getName())) {
   	        checkInfo.append("目录名称【name】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getParentId())) {
   			model.setParentId("-1");
   	     }
   		 if(StringUtil.isEmpty(model.getType())) {
    	    checkInfo.append("目录类型【type】不能为空;");
    	 }else {
    		 if("theme".equalsIgnoreCase(model.getType())) {
    			 if(StringUtil.isEmpty(model.getThemeId())) {
    		   		checkInfo.append("节点类型为专题的，专题ID【themeId】不能为空;");
    		   	 }
    		 }
    	 }
   		 if(StringUtil.isEmpty(model.getSortIndex())||model.getSortIndex()<1) {
   	         //checkInfo.append("排序索引【sortIndex】不能为空;");
   			 //如果为空，复制为默认的值（库中最大排序加1）
			 model.setSortIndex(catalogResourceDao.getObjectSortIndexForIntegerType("T_ZY_CATALOG", "SORTINDEX"));
   	     }
   		 if(StringUtil.isEmpty(model.getCjr())) {
   			checkInfo.append("目录添加人【cjr】不能为空;");
   		 }
   		 /*if(StringUtil.isEmpty(model.getProject())) {
   			checkInfo.append("所属项目名称【project】不能为空;");
   		 }*/
   		 return checkInfo.toString();
   	 }
}
