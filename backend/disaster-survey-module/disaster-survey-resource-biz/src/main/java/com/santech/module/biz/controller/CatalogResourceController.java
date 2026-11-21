package com.santech.module.biz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.ZyCatalogModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.CatalogAddDto;
import com.santech.module.biz.entity.dto.CatalogResourceQueryDto;
import com.santech.module.biz.entity.dto.CatalogUpdateDto;
import com.santech.module.biz.entity.vo.CatalogResourceTreeVo;
import com.santech.module.biz.service.CatalogResourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/catalog")
@Api(tags = "Resource-数据应用资源目录运维管理相关功能接口")
public class CatalogResourceController {

	public static final Logger logger = LoggerFactory.getLogger(CatalogResourceController.class);
	


    @Autowired
    private CatalogResourceService catalogResourceService;
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获CatalogResourceController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("CatalogResourceController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}
    
    
    /**
	  * @Title queryCatalogResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询数据目录资源列表信息)
	  * @param query (CatalogResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/tree")
	 @ApiOperation(value = "数据目录资源树查询接口-不分页", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<CatalogResourceTreeVo>> queryCatalogResourceTreeInfos(@RequestBody CatalogResourceQueryDto query){
		 return catalogResourceService.queryCatalogResourceTreeInfos(query);
	 }
	 
	 
	 /**
	  * @Title queryCatalogResourceInfoById 
	  * @Description (根据ID获取数据目录信息接口)
	  * @param catalogId (目录ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getById")
	 @ApiOperation(value = "根据ID获取数据目录信息接口", notes = "{字段说明【\n" +
			 "catalogId（数据目录资源id）\n" +
	         "】")
	 public ExecuteResult<ZyCatalogModel> queryCatalogResourceInfoById(@RequestParam("catalogId") String catalogId){
		 return catalogResourceService.queryCatalogResourceInfoById(catalogId);
	 }
	 
  
	 /**
	  * @Title saveCatalogResourceInfo 
	  * @Description (数据目录资源保存或新增接口)
	  * @param model (CatalogAddDto 新增数据目录资源所要保存的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "数据目录项新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "数据目录资源保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveCatalogResourceInfo(HttpServletRequest request, @RequestBody CatalogAddDto model) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(StringUtil.isEmpty(model.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("【"+model.getCjr()+"】非管理员不能添加数据目录资源", null);
		  }
	      return catalogResourceService.saveCatalogResourceInfo(model);
	 }
  
	 /**
	  * @Title updateCatalogResourceInfo 
	  * @Description (数据目录资源保存或新增接口)
	  * @param model (CatalogUpdateDto 新增数据目录资源所要更新的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "数据目录项更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "数据目录资源更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateCatalogResourceInfo(HttpServletRequest request, @RequestBody CatalogUpdateDto model) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改数据目录资源", null);
		  }
		  if(StringUtil.isEmpty(model.getZhgxr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setZhgxr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
	      return catalogResourceService.updateCatalogResourceInfo(model);
	 }
	/**
	 * @Title deleteCatalogResourceInfo
	 * @Description (此方法用于对数据目录资源进行删除)
	 * @param catalogId(数据目录资源ID)
	 * @return (删除的执行结果)
	 */
	  @LogSystem(target = "数据目录项删除", type = Constants.LOG_TYPE_DELETE)
	  @GetMapping("/delete")
	  @ApiOperation(value = "数据目录资源删除接口", notes = "{字段说明【\n" +
	  		"catalogId（数据目录资源id）\n" +
	  		"】")
	  public ExecuteResult<String> deleteCatalogResourceInfo(HttpServletRequest request,@RequestParam("catalogId") String catalogId){
		    String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
			if(!"1".equals(adminFlag)) {
				  return ExecuteResult.error("非管理员不能删除数据目录资源", null);
			}
			String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
			String userName = "";
			try {
				userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return catalogResourceService.deleteCatalogResourceInfo(catalogId, userName);
	  }
    
}
