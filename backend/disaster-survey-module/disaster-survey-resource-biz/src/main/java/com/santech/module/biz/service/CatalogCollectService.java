package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.ZyCatalogCollectModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.CatalogCollectAddDto;
import com.santech.module.biz.entity.dto.CatalogCollectQueryDto;
import com.santech.module.biz.entity.dto.CatalogCollectUpdateDto;

public interface CatalogCollectService {
	
	/**
	  * @Title queryCatalogCollectInfosWithPage 
	  * @Description (根据筛选条件批量分页查询专题目录收藏记录信息)
	  * @param query (CatalogCollectQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<ZyCatalogCollectModel>> queryCatalogCollectInfosWithPage(CatalogCollectQueryDto query);
	
	
	/**
	  * @Title saveCatalogCollectInfo 
	  * @Description (此为保存专题目录收藏信息接口的业务层接口方法)
	  * @param dto (CatalogCollectAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveCatalogCollectInfo(CatalogCollectAddDto dto);
	
	/**
	  * @Title updateCatalogCollectInfo 
	  * @Description (此为更新专题目录收藏信息接口的业务层接口方法)
	  * @param dto (CatalogCollectUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateCatalogCollectInfo(CatalogCollectUpdateDto dto);
	
	/**
	  * @Title deleteCatalogCollectInfo 
	  * @Description (此为删除专题目录收藏信息接口的业务层接口方法)
	  * @param collectId (专题目录收藏id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteCatalogCollectInfo(String collectId,String loginUser);
	
}
