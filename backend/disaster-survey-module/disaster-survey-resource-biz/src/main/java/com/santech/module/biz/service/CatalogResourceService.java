package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.ZyCatalogModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.CatalogAddDto;
import com.santech.module.biz.entity.dto.CatalogResourceQueryDto;
import com.santech.module.biz.entity.dto.CatalogUpdateDto;
import com.santech.module.biz.entity.vo.CatalogResourceTreeVo;

public interface CatalogResourceService {

	/**
	  * @Title queryCatalogResourceTreeInfos 
	  * @Description (根据筛选条件获取专题资源树信息信息)
	  * @param query (CatalogResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<CatalogResourceTreeVo>> queryCatalogResourceTreeInfos(CatalogResourceQueryDto query);
	
	/**
	  * @Title queryCatalogResourceInfoById 
	  * @Description (根据服务ID查询数据目录资源记录信息)
	  * @param catalogId (目录ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<ZyCatalogModel> queryCatalogResourceInfoById(String catalogId);
	
	/**
	  * @Title saveCatalogResourceInfo 
	  * @Description (此为保存数据资源服务信息接口的业务层接口方法)
	  * @param query (CatalogAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveCatalogResourceInfo(CatalogAddDto model);
	
	/**
	  * @Title updateCatalogResourceInfo 
	  * @Description (此为更新数据目录资源信息接口的业务层接口方法)
	  * @param model (CatalogUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateCatalogResourceInfo(CatalogUpdateDto model);
	
	/**
	  * @Title deleteCatalogResourceInfo 
	  * @Description (此为删除数据目录资源信息接口的业务层接口方法)
	  * @param catalogId (数据目录资源id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteCatalogResourceInfo(String catalogId,String loginUser);
}
