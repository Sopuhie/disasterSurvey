package com.santech.module.biz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.ZyServiceStyleModel;
import com.santech.core.framework.entity.model.ZyServiceStyleSimpleModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ServiceStyleAddDto;
import com.santech.module.biz.entity.dto.ServiceStyleResourceQueryDto;
import com.santech.module.biz.entity.dto.ServiceStyleUpdateDto;
import com.santech.module.biz.service.ServiceStyleResourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/style")
@Api(tags = "Resource-服务样式资源管理相关功能接口")
public class ServiceStyleResourceController {
	public static final Logger logger = LoggerFactory.getLogger(ServiceStyleResourceController.class);
    
	@Resource
    private ServiceStyleResourceService serviceStyleResourceService;
	
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ServiceStyleResourceController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("ServiceStyleResourceController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}
	
	/**
	  * @Title queryServiceStyleResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询服务样式资源记录信息)
	  * @param query (ServiceStyleResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
   @PostMapping("/queryWithPage")
   @ApiOperation(value = "服务样式资源列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
   public ExecuteResult<List<ZyServiceStyleSimpleModel>> queryServiceStyleResourceInfosWithPage(HttpServletRequest request,@RequestBody ServiceStyleResourceQueryDto query){
	    if(StringUtil.isEmpty(query.getAdminFlag())) {
	    	query.setAdminFlag((String) request.getAttribute(Constants.ADMIN_FLAG));//request.getHeader("adminFlag")
   		}
	    return serviceStyleResourceService.queryServiceStyleResourceInfosWithPage(query);
   }
   
   /**
	  * @Title queryServiceStyleResourceInfoById 
	  * @Description (根据ID获取服务样式资源信息接口)
	  * @param serviceStyleId (服务样式资源id)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getById")
	 @ApiOperation(value = "根据ID获取服务样式资源信息接口", notes = "{字段说明【\n" +
			 "serviceStyleId（服务样式资源id）\n" +
	         "】")
	 public ExecuteResult<ZyServiceStyleModel> queryServiceStyleResourceInfoById(@RequestParam("serviceStyleId") String serviceStyleId){
		 return serviceStyleResourceService.queryServiceStyleResourceInfoById(serviceStyleId);
	 }
	 

	 
	 /**
	  * @Title queryServiceStyleXmlInfoById 
	  * @Description (根据ID获取服务样式XML信息接口)
	  * @param serviceStyleId (服务样式资源id)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getxml/{serviceStyleId}")
	 @ApiOperation(value = "根据ID获取服务样式XML信息接口", notes = "{字段说明【\n" +
			 "serviceStyleId（服务样式资源id，作为地址的一部分进行值的传递）\n" +
	         "】")
	 public ExecuteResult<String> queryServiceStyleXmlInfoById(@PathVariable("serviceStyleId") String serviceStyleId){
		 return serviceStyleResourceService.queryServiceStyleXmlInfoById(serviceStyleId);
	 }
	 /**
	  * @Title queryServiceStyleXmlInfoByName 
	  * @Description (根据ID获取数据样式XML信息接口)
	  * @param serviceStyleId (数据样式资源id)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getxmlByName")
	 @ApiOperation(value = "根据样式名称获取Shp数据样式XML信息接口", notes = "{字段说明【\n" +
			 "styleName（Shp数据样式资源名称）\n" +
	         "】")
	 public ExecuteResult<String> queryServiceStyleXmlInfoByName(@RequestParam("styleName") String styleName){
		 return serviceStyleResourceService.queryServiceStyleXmlInfoByName(styleName);
	 }
	 
	/**
     * @Title uploadStyleXmlAndFormatCheck
	 * @Description (样式XML文件上传解析检查接口)
     * @param file (MultipartFile，上传的文件流信息)
     * @return (接口的执行情况及上传后的文件解析后的xml信息)
     */
    @ApiOperation(value = "样式XML文件上传解析检查接口",notes = "{字段说明【\n" +
            "xmlfile(上传文件，必传)\n" +
            "】")
	 @PostMapping("/upload")
	 public ExecuteResult<JSONObject> uploadStyleXmlAndFormatCheck (@RequestPart(value = "xmlfile") MultipartFile xmlfile){
	    	
	     return serviceStyleResourceService.uploadStyleXmlAndFormatCheck(xmlfile);
	 }
	 
   /**
	  * @Title saveServiceStyleResourceInfo 
	  * @Description (此为保存服务样式资源信息接口的业务层接口方法)
	  * @param query (ZyServiceStyleModel,参数)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "服务样式资源新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "服务样式资源保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveServiceStyleResourceInfo(HttpServletRequest request, @RequestBody ServiceStyleAddDto model){
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader("adminFlag");
		  if(StringUtil.isEmpty(model.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader("loginUser");
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, "UTF-8"):"";
				  model.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("【"+model.getCjr()+"】非管理员不能添加服务样式资源", null);
		  }
		 return serviceStyleResourceService.saveServiceStyleResourceInfo(model);
	}
	
	/**
	  * @Title updateServiceStyleResourceInfo
	  * @Description (此为更新服务样式资源信息接口的业务层接口方法)
	  * @param model (ZyServiceStyleModel,参数)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "服务样式资源更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "服务样式资源更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateServiceStyleResourceInfo(HttpServletRequest request, @RequestBody ServiceStyleUpdateDto model){
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader("adminFlag");
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改服务样式资源", null);
		  }
		  if(StringUtil.isEmpty(model.getZhgxr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader("loginUser");
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, "UTF-8"):"";
				  model.setZhgxr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		 return serviceStyleResourceService.updateServiceStyleResourceInfo(model);
		 
	 }
	
	/**
	  * @Title deleteServiceStyleResourceInfo 
	  * @Description (此为删除服务样式资源信息接口的业务层接口方法)
	  * @param serviceStyleId (服务样式资源id)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "服务样式资源删除", type = Constants.LOG_TYPE_DELETE)
	  @GetMapping("/delete")
	  @ApiOperation(value = "服务样式资源删除接口", notes = "{字段说明【\n" +
			  "serviceStyleId（服务样式资源id）\n" +
	   		"】")
	  public ExecuteResult<String> deleteServiceStyleResourceInfo(HttpServletRequest request, @RequestParam("serviceStyleId") String serviceStyleId){
		  String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader("adminFlag");
			
		    String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader("loginUser");
			String userName = "";
			try {
				userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, "UTF-8"):"";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if(!"1".equals(adminFlag)) {
				  return ExecuteResult.error("【"+userName+"】非管理员不能删除接口服务资源", null);
			}
		  return serviceStyleResourceService.deleteServiceStyleResourceInfo(userName,serviceStyleId);
	  }
	
	  /**
	    * @Title downloadFile
		* @Description (单文件下载接口-GET，仅限单文件下载，文件来源为文件服务器)
	    * @param fileName(导出文件名称)
	    * @param serviceStyleId(目标样式记录id)
	    * @return (ResponseEntity<byte[]> 下载的文件流信息)
	    */
	    @LogSystem(target = "服务样式资源下载", type = Constants.LOG_TYPE_DOWNLOAD)
	    @ApiOperation(value = "导出服务样式xml文件",notes = "{字段说明【\n" +
	            "fileName(下载的文件名带有后缀，非必填)\n" +
	            "serviceStyleId(目标样式记录id，必填)\n" +
	            "】")
	    @GetMapping(value="/download")
	    public ResponseEntity<byte[]> downloadStyleXmlFile (@RequestParam(value = "fileName", required = false) String fileName , @RequestParam(value = "serviceStyleId",required = true) String serviceStyleId){
	      
	    	logger.info("下载服务样式xml，入参，下载文件名称[{}]，下载目标样式记录id[{}]。",fileName,serviceStyleId);
	    	ResponseEntity<byte[]> responseEntity = null;
	    	try {
	    		ExecuteResult<ZyServiceStyleModel> model = serviceStyleResourceService.queryServiceStyleResourceInfoById(serviceStyleId);
				if("S".equalsIgnoreCase(model.getResultCode())) {
					String xmlContent = model.getData().getContent();
					if(StringUtil.isNotEmpty(xmlContent)) {
						if(StringUtil.isEmpty(fileName)) {
							//fileName = xmlContent.getString("fileName");
							fileName = model.getData().getName()+".xml";
						}
						HttpHeaders headers = new HttpHeaders();
		    	        headers.setContentDispositionFormData("attachment",new String(fileName.getBytes("UTF-8"),"ISO-8859-1"));
		    	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		    	        //此处写出的不是文件，是文件里的内容，前端进行文件生成的，逻辑有点瑕疵
		    	        byte[] out= xmlContent.getBytes();
		    	        
		    	        if(out!=null) {
		        	        return ResponseEntity
		                            .ok()
		                            .headers(headers)
		                            .contentType(MediaType.parseMediaType("application/octet-stream"))
		                            .body(out);
		    	        }else {
		    	        	responseEntity = ResponseEntity.notFound().build();
		    	        }
					}else {
						responseEntity = ResponseEntity.notFound().build();
					}
				}else {
					responseEntity = ResponseEntity.notFound().build();
				}
			} catch (Exception e) {
				logger.error("导出样式XML【{}】异常,详情：",serviceStyleId,e);
				responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
	    	return responseEntity;
	    }
	
	
}
