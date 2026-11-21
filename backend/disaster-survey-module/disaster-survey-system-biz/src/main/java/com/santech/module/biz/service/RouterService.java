package com.santech.module.biz.service;

import java.util.List;

import com.santech.core.framework.entity.model.JcRouterModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.RouterAddDto;
import com.santech.module.biz.entity.dto.RouterQueryDto;
import com.santech.module.biz.entity.dto.RouterUpdateDto;

/**
 * @Title: RouterService.java 
 * @Description：运维路由管理接口
 * @Version： V1.0
 * @Modify：   
 */
public interface RouterService {
	
	/**
	  * @Title saveRouterInfo 
	  * @Description (此为保存运维路由菜单信息接口的业务层接口方法)
	  * @param dto (RouterAddDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> saveRouterInfo(RouterAddDto dto);
	
	
	/**
	  * @Title updateRouterInfo 
	  * @Description (此为更新运维路由菜单信息接口的业务层接口方法)
	  * @param dto (RouterUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> updateRouterInfo(RouterUpdateDto dto);
	
	/**
	  * @Title deleteRouterInfo 
	  * @Description (此为删除运维路由菜单信息接口的业务层接口方法)
	  * @param routerId (运维路由菜单id)
	  * @param loginUser (当前登录用户用户名)
	  * @return (接口的执行情况)
	  */
	ExecuteResult<String> deleteRouterInfo(String routerId,String loginUser);
	
	/**
	  * @Title queryRouterInfosWithPage 
	  * @Description (根据筛选条件批量分页查询运维路由菜单记录信息,不传分页信息会获取全部)
	  * @param query (RouterQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<JcRouterModel>> queryRouterInfosWithPage(RouterQueryDto query);

	/**
	  * @Title queryRouterTreeInfos 
	  * @Description (根据权限查询运维路由菜单信息并组装成树结构返回的接口业务层接口)
	  * @param adminFlag (管理员标识)
	  * @param loginUserId (当前登录用户用户ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<JcRouterModel>> queryRouterTreeInfos(String loginUserId,String adminFlag);
    
	
}
