package com.santech.module.biz.service;

import java.util.List;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ThemeResourceTreeQueryDto;
import com.santech.module.biz.entity.dto.ThemeServiceBindDto;
import com.santech.module.biz.entity.vo.ThemeResourceTreeVo;
import com.santech.module.biz.entity.vo.ThemeWithServiceLayersVo;

public interface ThemeServiceRelateService {
	
	/**
	  * @Title queryThemeResourceTreeInfos 
	  * @Description (根据筛选条件获取专题资源树信息信息)
	  * @param query (ThemeResourceTreeQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<ThemeResourceTreeVo>> queryThemeResourceTreeInfos(ThemeResourceTreeQueryDto query);
	
	/**
	  * @Title serviceThemeBind 
	  * @Description (此为服务专题资源绑定接口的业务层接口方法)
	  * @param model (ThemeServiceBindModel,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> serviceThemeBind(ThemeServiceBindDto model);
	
	/**
	  * @Title serviceThemeUnBind 
	  * @Description (此为服务专题资源解绑接口的业务层接口方法,可以进行批量解绑)
	  * @param models (List<ThemeServiceBindDto>,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> serviceThemeUnBind(List<ThemeServiceBindDto> models);
	
	/**
	  * @Title queryThemeBindLayersInfoById 
	  * @Description (此为根据专题id获取专题下的服务和图层接口的业务层接口方法)
	  * @param themeId (专题ID)
	  * @return (接口的执行情况及返回的目标数据信息)
	  */
	ExecuteResult<List<ThemeWithServiceLayersVo>> queryThemeBindLayersInfoById(String themeId);

 
}
