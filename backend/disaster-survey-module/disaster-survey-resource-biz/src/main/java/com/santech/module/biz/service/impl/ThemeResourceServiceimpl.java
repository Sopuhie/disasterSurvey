package com.santech.module.biz.service.impl;

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
import com.santech.core.framework.dao.ThemeResourceDao;
import com.santech.core.framework.dao.ThemeServiceRelateDao;
import com.santech.core.framework.entity.model.ZyThemeModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.ThemeAddDto;
import com.santech.module.biz.entity.dto.ThemeUpdateDto;
import com.santech.module.biz.service.ThemeResourceService;

import cn.hutool.core.util.ObjectUtil;

@Service
public class ThemeResourceServiceimpl implements ThemeResourceService {
	private static final  Logger logger = LoggerFactory.getLogger(ThemeResourceServiceimpl.class);
	
    @Resource
    private ThemeResourceDao themeResourceDao;
    
    @Resource
    private ThemeServiceRelateDao themeServiceRelateDao;
    
    @Resource
    private CatalogResourceDao catalogResourceDao;
    
    
    
    @Override
   	public ExecuteResult<ZyThemeModel> queryThemeResourceInfoById(String themeId) {
       	try {
   			if (StringUtil.isEmpty(themeId)) {
   				return ExecuteResult.error("数据专题资源ID入参不能为空值", null);
   			}
   			logger.info("根据ID查询数据专题资源信息，记录id【{}】", themeId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("themeId", themeId);
   			ZyThemeModel targetModel = themeResourceDao.queryObject(ZyThemeModel.class,"ID=:themeId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应数据专题资源信息!", null);
   			}else {
   				return ExecuteResult.success("查询成功",targetModel);
   			}
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询数据专题资源信息出现异常，原因：【{}】,详情：", themeId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
   	}
    
    @Override
   	public ExecuteResult<ZyThemeModel> queryThemeResourceInfoByName(String themeName) {
       	try {
   			if (StringUtil.isEmpty(themeName)) {
   				return ExecuteResult.error("数据专题资源名称入参不能为空值", null);
   			}
   			logger.info("根据名称查询数据专题资源信息，记录id【{}】", themeName);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("themeName", themeName);
   			ZyThemeModel targetModel = themeResourceDao.queryObject(ZyThemeModel.class,"THEMENAME=:themeName", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据名称未查询到对应数据专题资源信息!", null);
   			}else {
   				return ExecuteResult.success("查询成功",targetModel);
   			}
   		} catch (Exception e) {
   			logger.error("根据名称【{}】查询数据专题资源信息出现异常，原因：【{}】,详情：", themeName,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
   	}
       
       
    @Override
   	public ExecuteResult<String> saveThemeResourceInfo(ThemeAddDto dto) {
       	try {
   			String checkErr = checkThemeResourceInfoForAdd(dto) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("themeName",dto.getThemeName());
   		    paramsMap.put("pid",dto.getParentId());
   		    List<ZyThemeModel> themeModels = themeResourceDao.queryObjects(ZyThemeModel.class,"THEMENAME=:themeName and PARENTID=:pid",paramsMap);
   		    if (ObjectUtil.isNotEmpty(themeModels)&&themeModels.size()>0) {
   		    	return ExecuteResult.error("同一目录下，数据专题资源名称不能重复！", null);
   		    }
   		    ZyThemeModel model = new ZyThemeModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("TR"));
   			model.setCjsj(new Date());
   			model.setZhgxr(model.getCjr());
   			model.setZhgxsj(model.getCjsj());
   			themeResourceDao.insertThemeResourceInfo(model);
   			return ExecuteResult.success("数据专题资源添加成功！");
   		} catch (Exception e) {
   			logger.error("新增数据专题资源异常，详情：",e);
   			return ExecuteResult.error("新增数据专题资源异常，请稍后再试", null);
   		}
   	}
       
    @Override
   	public ExecuteResult<String> updateThemeResourceInfo(ThemeUpdateDto dto) {
       	try {
   			String themeId = dto.getId();
   			if(StringUtils.isEmpty(themeId)) {
       			return ExecuteResult.error("数据专题资源ID不能为空！", null);
       		}
   			if(StringUtil.isEmpty(dto.getZhgxr())) {
   				return ExecuteResult.error("数据更新人不能为空！", null);
   	 		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("themeId",themeId);
   		    ZyThemeModel originTheme = themeResourceDao.queryObject(ZyThemeModel.class,"ID=:themeId",paramsMap);
   		    if (StringUtil.isEmpty(originTheme)) {
   		    	return ExecuteResult.error("未找到对应数据专题资源项，无法完成更新！", null);
   		    }
   		    //检查数据专题资源是否包含重名
   		    if(StringUtil.isNotEmpty(dto.getThemeName())&&!(dto.getThemeName().equals(originTheme.getThemeName()))) {
   			    paramsMap.put("themeName",dto.getThemeName());
   			    List<ZyThemeModel> themeModels = themeResourceDao.queryObjects(ZyThemeModel.class,"ID!=:themeId and THEMENAME=:themeName",paramsMap);
   			    if (StringUtil.isNotEmpty(themeModels)&&themeModels.size()>0) {
   			    	return ExecuteResult.error("要更新的数据专题资源名称已占用！", null);
   			    }
   		    }
   		    ZyThemeModel service = getThemeResourceModelByUpdateInfo(originTheme,dto);
   		    themeResourceDao.updateThemeResourceInfo(service);
   			return ExecuteResult.success("数据专题资源更新成功！");
   		} catch (Exception e) {
   			logger.error("更新数据专题资源异常，详情：",e);
   			return ExecuteResult.error("更新数据专题资源异常，请稍后再试", null);
   		}
   	}


   	@Override
   	@Transactional(rollbackFor = Exception.class)
   	public ExecuteResult<String> deleteThemeResourceInfo(String themeId,String loginUser) {
   		try {
   			if (StringUtil.isEmpty(themeId)) {
   				return ExecuteResult.error("数据专题资源ID入参不能为空值", null);
   			}
   			logger.info("删除数据专题资源信息，记录id【{}】", themeId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("themeId", themeId);
   			ZyThemeModel targetModel = themeResourceDao.queryObject(ZyThemeModel.class,"ID=:themeId", paramsOne);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定数据专题资源信息", themeId);
   				return ExecuteResult.error("已无对应数据专题资源信息!", null);
   			}
   			int count1 = themeResourceDao.deleteThemeResourceInfoById(themeId);
   			if (count1> 0) {
   		         // 删除专题与数据服务资源的绑定关系
   		         themeServiceRelateDao.deleteByThemeId(themeId);
   		         //删除专题与资源目录的绑定关系
   		         catalogResourceDao.deleteByThemeId(themeId);
   				return ExecuteResult.success("删除成功");
   			}else {
   				throw new RuntimeException("删除数据专题资源失败");
   			}
   		} catch (Exception e) {
   			logger.error("删除数据专题资源【{}】出现异常，原因：【{}】,详情：", themeId,e.getMessage(), e);
   			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
   		}
   	}
   	
    //获取目标更新的数据专题资源信息
   	private ZyThemeModel getThemeResourceModelByUpdateInfo(ZyThemeModel origin,ThemeUpdateDto update) {
   			
   		 if(StringUtil.isNotEmpty(update.getThemeName())) {
   			origin.setThemeName(update.getThemeName());
   		 }
   		 if(StringUtil.isNotEmpty(update.getSortIndex())) {
   			origin.setSortIndex(update.getSortIndex());
   		 }
   		 if(StringUtil.isNotEmpty(update.getTimeLine())) {
   			origin.setTimeLine(update.getTimeLine());
   		 }
   		 if(StringUtil.isNotEmpty(update.getZhgxr())) {
   			origin.setZhgxr(update.getZhgxr());
    	 }
   		 origin.setZhgxsj(new Date());
   		return origin;
   	}
     //校验新增的专题信息
   	 private String checkThemeResourceInfoForAdd(ThemeAddDto model) throws Exception {
   		 StringBuilder checkInfo = new StringBuilder("");
   		 if(StringUtil.isEmpty(model)) {
   			 checkInfo.append("ThemeAddDto不能为空;");
   			 return checkInfo.toString();
   		 }
   		 if(StringUtil.isEmpty(model.getThemeName())) {
   	        checkInfo.append("专题名称【themeName】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getType())) {
    	    checkInfo.append("节点类型【type】不能为空;");
    	 }
   		 if(StringUtil.isEmpty(model.getSortIndex())||model.getSortIndex()<1) {
   	        //checkInfo.append("排序索引【sortIndex】不能为空;");
   			 //如果为空，复制为默认的值（库中最大排序加1）
 			 model.setSortIndex(themeResourceDao.getObjectSortIndexForIntegerType("T_ZY_THEME", "SORTINDEX"));
   	     }
   		 if(StringUtil.isEmpty(model.getParentId())&&!"folder".equalsIgnoreCase(model.getType())) {
   			checkInfo.append("专题节点父节点为空（专题类型节点不能做根节点）;");
   		 }
   		 if(StringUtil.isEmpty(model.getParentId())) {
   			model.setParentId("-1");
   	     }
   		 if(StringUtil.isEmpty(model.getCjr())) {
   			 checkInfo.append("专题添加人【cjr】不能为空;");
   		 }
   		 return checkInfo.toString();
   		 
   	 }
    
}
