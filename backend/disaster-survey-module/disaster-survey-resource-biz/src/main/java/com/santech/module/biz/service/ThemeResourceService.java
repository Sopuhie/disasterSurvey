package com.santech.module.biz.service;

import com.santech.core.framework.entity.model.ZyThemeModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ThemeAddDto;
import com.santech.module.biz.entity.dto.ThemeUpdateDto;

public interface ThemeResourceService {
	
	

	
	/**
	  * @Title queryThemeResourceInfoById 
	  * @Description (根据服务ID查询数据专题资源记录信息)
	  * @param themeId (专题ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ZyThemeModel> queryThemeResourceInfoById(String themeId);
	
	/**
	  * @Title queryThemeResourceInfoByName 
	  * @Description (根据服务ID查询数据专题资源记录信息)
	  * @param themeName (专题名称)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ZyThemeModel> queryThemeResourceInfoByName(String themeName);
	
	/**
	  * @Title saveThemeResourceInfo 
	  * @Description (此为保存数据资源服务信息接口的业务层接口方法)
	  * @param model (ThemeAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveThemeResourceInfo(ThemeAddDto model);
	
	/**
	  * @Title updateThemeResourceInfo 
	  * @Description (此为更新数据专题资源信息接口的业务层接口方法)
	  * @param model (ThemeUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateThemeResourceInfo(ThemeUpdateDto model);
	
	/**
	  * @Title deleteThemeResourceInfo 
	  * @Description (此为删除数据专题资源信息接口的业务层接口方法)
	  * @param themeId (数据专题资源id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteThemeResourceInfo(String themeId,String loginUser);
	
	
}
