package com.santech.module.biz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.ZyCatalogCollectModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.CatalogCollectAddDto;
import com.santech.module.biz.entity.dto.CatalogCollectQueryDto;
import com.santech.module.biz.entity.dto.CatalogCollectUpdateDto;
import com.santech.module.biz.service.CatalogCollectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/collect")
@Api(tags = "Resource-专题资源收藏管理相关功能接口")
public class CatalogCollectController {

    public static final Logger logger = LoggerFactory.getLogger(CatalogCollectController.class);
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获CatalogCollectController接口程序中未能预见的异常信息，保证前端响应结果的一致性)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("CatalogCollectController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}

    @Resource
    private CatalogCollectService catalogCollectService;

	 /**
	  * @Title queryCatalogCollectInfosForFront 
	  * @Description (根据筛选条件批量分页查询专题目录收藏列表信息，当分页信息均为0时，查询全部)
	  * @param query (CatalogCollectQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/queryWithPage")
	 @ApiOperation(value = "专题目录收藏列表查询接口（可分页）", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<ZyCatalogCollectModel>> queryCatalogCollectInfos(HttpServletRequest request,@RequestBody CatalogCollectQueryDto query){
		 if(StringUtil.isEmpty(query.getLoginUser())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  query.setLoginUser(userName);
			  } catch (UnsupportedEncodingException e) {
				  e.printStackTrace();
			  }
		  }
		 return catalogCollectService.queryCatalogCollectInfosWithPage(query);
	 }
	 
    
	 /**
	  * @Title saveCatalogCollectInfo 
	  * @Description (专题目录收藏保存或新增接口)
	  * @param model (CatalogCollectAddDto 新增专题目录收藏所要保存的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "专题目录收藏新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "专题目录收藏保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveCatalogCollectInfo(HttpServletRequest request, @RequestBody CatalogCollectAddDto model) {
		  if(StringUtil.isEmpty(model.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				  e.printStackTrace();
			  }
		  }
	      return catalogCollectService.saveCatalogCollectInfo(model);
	 }
    
	 /**
	  * @Title updateCatalogCollectInfo 
	  * @Description (专题目录收藏保存或新增接口)
	  * @param model (CatalogCollectUpdateDto 专题目录收藏所要更新的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "专题目录收藏更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "专题目录收藏更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateCatalogCollectInfo(HttpServletRequest request, @RequestBody CatalogCollectUpdateDto model) {
		 if(StringUtil.isEmpty(model.getLoginUser())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
				  model.setLoginUser(userName);
			  } catch (UnsupportedEncodingException e) {
				  e.printStackTrace();
			  }
		  }
	      return catalogCollectService.updateCatalogCollectInfo(model);
	 }
	/**
	 * @Title deleteCatalogCollectInfo
	 * @Description (此方法用于对专题目录收藏进行删除)
	 * @param collectId(专题目录收藏ID)
	 * @return (删除的执行结果)
	 */
	@LogSystem(target = "专题目录收藏删除", type = Constants.LOG_TYPE_DELETE)
    @GetMapping("/delete")
    @ApiOperation(value = "专题目录收藏删除接口", notes = "{字段说明【\n" +
    		"collectId（专题目录收藏id）\n" +
    		"】")
    public ExecuteResult<String> deleteCatalogCollectInfo(HttpServletRequest request,@RequestParam("collectId") String collectId){
		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return catalogCollectService.deleteCatalogCollectInfo(collectId, userName);
    }
	
	

}
