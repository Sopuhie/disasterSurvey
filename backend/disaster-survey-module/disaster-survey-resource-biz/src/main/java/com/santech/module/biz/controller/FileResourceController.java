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
import com.santech.core.framework.entity.model.ZyFileModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.FileAddDto;
import com.santech.module.biz.entity.dto.FileDeleteDto;
import com.santech.module.biz.entity.dto.FileDetailsUpdateDto;
import com.santech.module.biz.entity.dto.FileResourceQueryDto;
import com.santech.module.biz.entity.dto.FileUpdateDto;
import com.santech.module.biz.service.FileResourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/file")
@Api(tags = "Resource-文件资源运维管理相关功能接口")
public class FileResourceController {

    public static final Logger logger = LoggerFactory.getLogger(FileResourceController.class);
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获FileResourceController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("FileResourceController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}

    @Autowired
    private FileResourceService fileResourceService;
    
    /**
	  * @Title queryFileResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询文件资源记录信息)
	  * @param query (FileResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
    @PostMapping("/queryWithPage")
	@ApiOperation(value = "文件资源列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
    public ExecuteResult<List<ZyFileModel>> queryFileResourceInfosWithPage(HttpServletRequest request,FileResourceQueryDto query){
    	if(StringUtil.isEmpty(query.getAdminFlag())) {
    		query.setAdminFlag((String) request.getAttribute(Constants.ADMIN_FLAG));//request.getHeader(Constants.ADMIN_FLAG)
    	}
    	return fileResourceService.queryFileResourceInfosWithPage(query);
    }
	
	
	/**
	  * @Title queryFileDetailInfosById 
	  * @Description (根据服务ID查询文件资源记录以及包含的文件附件明细信息信息)
	  * @param fileResourceId (文件资源ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
     @GetMapping("/getById")
	 @ApiOperation(value = "根据ID获取文件资源详情信息接口", notes = "{字段说明【\n" +
			 "fileResourceId（文件资源id）\n" +
	         "】")
	 public ExecuteResult<ZyFileModel> queryFileDetailInfosById(@RequestParam("fileResourceId") String fileResourceId){
    	 
    	 return fileResourceService.queryFileDetailInfosById(fileResourceId);
    }
	
	
	/**
	  * @Title saveFileResourceInfo 
	  * @Description (此为保存文件资源信息接口的业务层接口方法)
	  * @param query (FileAddDto,参数)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "文件资源新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "文件资源保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveFileResourceInfo(HttpServletRequest request, @RequestBody FileAddDto model){
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
			  return ExecuteResult.error("【"+model.getCjr()+"】非管理员不能添加文件资源", null);
		  }
		 return fileResourceService.saveFileResourceInfo(model);
	}
	
	/**
	  * @Title updateFileResourceInfo
	  * @Description (此为更新文件资源信息接口的业务层接口方法)
	  * @param model (FileUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "文件资源更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "文件资源更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateFileResourceInfo(HttpServletRequest request, @RequestBody FileUpdateDto model){
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改文件资源", null);
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
		 return fileResourceService.updateFileResourceInfo(model);
		 
	 }
	
	/**
	  * @Title deleteFileResourceInfo 
	  * @Description (此为删除文件资源信息接口的业务层接口方法)
	  * @param model (FileDeleteModel 删除的目标文件参数信息)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "文件资源删除", type = Constants.LOG_TYPE_DELETE)
	  @PostMapping("/delete")
	  @ApiOperation(value = "文件资源删除接口", notes = "{字段说明【\n" +
	   		"】")
	  public ExecuteResult<String> deleteFileResourceInfo(HttpServletRequest request, @RequestBody FileDeleteDto model){
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
			if(!"1".equals(adminFlag)) {
				  return ExecuteResult.error("非管理员不能删除文件资源", null);
			}
			if(StringUtil.isEmpty(model.getLoginUser())) {
				try {
					String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
					String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
					model.setLoginUser(userName);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		  return fileResourceService.deleteFileResourceInfo(model);
	  }
	
	/**
	  * @Title updateFileResourceDetailsInfo 
	  * @Description (此为为文件资源继续添加文件的接口方法)
	  * @param dto (FileDetailsUpdateDto 保存新添加的附件参数信息)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "文件资源继续添加文件", type = Constants.LOG_TYPE_ADD)
	  @PostMapping("/detail/add")
	  @ApiOperation(value = "文件资源继续添加文件的接口", notes = "")
	  public ExecuteResult<String> updateFileResourceDetailsInfo(HttpServletRequest request, @RequestBody FileDetailsUpdateDto dto){
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
			if(!"1".equals(adminFlag)) {
				  return ExecuteResult.error("非管理员不能为文件资源添加文件", null);
			}
			if(StringUtil.isEmpty(dto.getCjr())) {
				try {
					String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
					String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
					dto.setCjr(userName);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		  return fileResourceService.updateFileResourceDetailsInfo(dto);
	  }
	/**
	  * @Title deleteFileDetailInfo 
	  * @Description (此为删除文件资源信息接口的业务层接口方法)
	  * @param model (FileDeleteModel 删除的目标文件参数信息)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "文件资源中具体文件删除", type = Constants.LOG_TYPE_DELETE)
	  @PostMapping("/detail/delete")
	  @ApiOperation(value = "文件资源中具体文件删除接口", notes = "{该接口会连同存储的文件一并删除，字段说明【\n" +
	   		"】")
	  public ExecuteResult<String> deleteFileDetailInfo(HttpServletRequest request, @RequestBody FileDeleteDto model){
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
			if(!"1".equals(adminFlag)) {
				  return ExecuteResult.error("非管理员不能删除文件资源中具体文件", null);
			}
			if(StringUtil.isEmpty(model.getLoginUser())) {
				try {
					String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
					String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
					model.setLoginUser(userName);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		  return fileResourceService.deleteFileDetailInfo(model);
	  }
    
    


}
