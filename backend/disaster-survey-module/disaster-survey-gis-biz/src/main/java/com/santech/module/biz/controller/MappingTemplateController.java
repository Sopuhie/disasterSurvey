package com.santech.module.biz.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.MappingLogModel;
import com.santech.core.framework.entity.model.MappingTemplateModel;
import com.santech.core.framework.entity.view.UploadFileVo;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.excel.easyexcel.EasyExcelUtils;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.MappingLogAddDto;
import com.santech.module.biz.entity.dto.MappingLogQueryDto;
import com.santech.module.biz.entity.dto.MappingTemplateAddDto;
import com.santech.module.biz.entity.dto.MappingTemplateResourceQueryDto;
import com.santech.module.biz.entity.dto.MappingTemplateUpdateDto;
import com.santech.module.biz.service.MappingTemplateService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-map/web/disaster/map/mapping")
@Api(tags = "Gis-专题出图模板资源管理相关功能接口")
public class MappingTemplateController {
	public static final Logger logger = LoggerFactory.getLogger(MappingTemplateController.class);
	@Resource
 	private FileBasicConfig fileBasicConfig;
	@Resource
    private MappingTemplateService mappingTemplateService;
	
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获MappingTemplateResourceController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("MappingTemplateResourceController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}
	
	/**
	  * @Title queryMappingTemplateResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询专题出图模板资源记录信息)
	  * @param query (MappingTemplateResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
   @PostMapping("/template/queryWithPage")
   @ApiOperation(value = "专题出图模板资源列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
   public ExecuteResult<List<MappingTemplateModel>> queryMappingTemplateResourceInfosWithPage(@RequestBody MappingTemplateResourceQueryDto query){
   	return mappingTemplateService.queryMappingTemplateResourceInfosWithPage(query);
   }
   
   /**
	  * @Title saveMappingTemplateResourceInfo 
	  * @Description (此为保存专题出图模板资源信息的接口)
	  * @param query (MappingTemplateAddDto,参数)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "专题出图模板资源新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/template/add")
	 @ApiOperation(value = "专题出图模板资源保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveMappingTemplateResourceInfo(HttpServletRequest request, @RequestBody MappingTemplateAddDto model){
		  if(StringUtil.isEmpty(model.getCjr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader("loginUser");
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, "UTF-8"):"";
				  model.setCjr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		 return mappingTemplateService.saveMappingTemplateResourceInfo(model);
	}
	
	/**
	  * @Title updateMappingTemplateResourceInfo
	  * @Description (此为更新专题出图模板资源信息的接口法)
	  * @param model (MappingTemplateUpdateDto,参数)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "专题出图模板资源更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/template/update")
	 @ApiOperation(value = "专题出图模板资源更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateMappingTemplateResourceInfo(HttpServletRequest request, @RequestBody MappingTemplateUpdateDto model){
		  if(StringUtil.isEmpty(model.getZhgxr())) {
			  try {
				  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader("loginUser");
				  String userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, "UTF-8"):"";
				  model.setZhgxr(userName);
			  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			  }
		  }
		 return mappingTemplateService.updateMappingTemplateResourceInfo(model);
		 
	 }
	
	/**
	  * @Title deleteMappingTemplateResourceInfo 
	  * @Description (此为删除专题出图模板资源信息的接口)
	  * @param apiId (专题出图模板id)
	  * @return (接口的执行情况)
	  */
	  @LogSystem(target = "专题出图模板资源删除", type = Constants.LOG_TYPE_DELETE)
	  @GetMapping("/template/delete")
	  @ApiOperation(value = "专题出图模板资源删除接口", notes = "{字段说明【\n" +
			  "templateId（专题出图模板资源id）\n" +
	   		"】")
	  public ExecuteResult<String> deleteMappingTemplateResourceInfo(HttpServletRequest request, @RequestParam("templateId") String templateId){
			
		  String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader("loginUser");
			String userName = "";
			try {
				userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, "UTF-8"):"";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		  return mappingTemplateService.deleteMappingTemplateResourceInfo(userName,templateId);
	  }
	  
	  
//如下为出图符号的上传、查询、删除操作
	  
	  /**
	     * @Title symbolFilesUpload
		 * @Description (此为符号图片上传接口)
	     * @param files(MultipartFile[] 上传文件，必传)
	     * @param typeName(标注分类名称，如symbol_dot(点符号),symbol_compass（指南针符号），必传)
	     * @return (接口的执行情况及文件结果信息)
	     */
	    @ApiOperation(value = "标注图片或文件上传接口",notes = "{字段说明【\n" +
	            "files(MultipartFile[] 上传文件，必传)\n" +
	            "typeName(标注分类名称，如symbol_dot(点符号),symbol_compass（指南针符号），必传)\n" +
	            "】")
	    @PostMapping("/symbol/upload")
	    public ExecuteResult<JSONObject> symbolFilesUpload (@RequestPart("files") MultipartFile[] files,@RequestParam("typeName") String typeName){
	    	//校验上传的文件
	    	 if(files==null||files.length<1) {
	     		return ExecuteResult.error("文件不能为空", null);
	     	 }
	    	JSONObject result = new JSONObject();//响应结果
	    	JSONArray successFiles = new JSONArray();//上传成功的文件
	    	JSONArray failFiles = new JSONArray();//上传失败的文件
	    	try {
	    		//循环保存文件
		    	for(MultipartFile file:files) {
		    		ExecuteResult<UploadFileVo> upResult = mappingTemplateService.symbolFilesUpload(file,typeName);
	 	        	if("S".equalsIgnoreCase(upResult.getResultCode())) {
	 	      			successFiles.add(upResult.getData());
	 	        	}else {
	 	        		JSONObject f = new JSONObject();
	 	        		f.put("fileName", file.getOriginalFilename());//文件原名
	 	        		f.put("fileSize", file.getSize()+"");//文件大小
	 	        		failFiles.add(f);
	 	        	}
	    		 }
		    	result.put("successFiles", successFiles);
		    	result.put("failFiles", failFiles);
		    	return ExecuteResult.success("文件上传成功", result);
	    	} catch (Exception e) {
	        	logger.error("文件上传出现异常，详情：",e);
	        	return ExecuteResult.error("文件上传失败", null);
	        }
	  }
	    
	    /**
	     * @Title getSymbolFileInfoByFolder
		 * @Description (此为根据目标符号存储文件夹获取期内包含的符号文件路径的接口方法)
	     * @param folderName(符号所在文件夹名称)
	     * @return (接口的执行情况及文件信息)
	     */
	   @ApiOperation("根据文件夹获取其里面的文件列表")
	   @ApiImplicitParams({
	           @ApiImplicitParam(name = "folderName",value = "文件夹名称"),
	   })
	   @GetMapping("/symbol/query")
	   public ExecuteResult<List<String>> getSymbolFileInfoByFolder(@RequestParam("folderName") String folderName){
	      
	       return mappingTemplateService.getFilePathInfoByFolder(folderName);
	   }
	   /**
	     * @Title deleteSymbolFileByFilePath
		 * @Description (此为根据符号文件相对路径删除目标文件的接口方法)
	     * @param symbolFilePath(符号文件相对路径)
	     * @return (接口的执行情况)
	     */
	   @ApiOperation("根据符号文件相对路径删除目标文件")
	   @ApiImplicitParams({
	           @ApiImplicitParam(name = "symbolFilePath",value = "符号文件相对路径"),
	   })
	   @GetMapping("/symbol/delete")
	   public ExecuteResult<String> deleteSymbolFileByFilePath(@RequestParam("symbolFilePath") String symbolFilePath){
		   
		   return mappingTemplateService.deleteSymbolFileByFilePath(symbolFilePath);
	   }
	   
	   
/************出图日志相关接口*****************/
	   
	   /**
		  * @Title queryLogInfos 
		  * @Description (根据筛选条件批量查询日志录信息)
		  * @param query (MappingLogQueryDto,查询条件参数)
		  * @return (接口的执行情况及符合条件的记录信息)
		  */
	   @PostMapping("/log/query")
	   @ApiOperation(value = "日志批量查询接口（可分页）", notes = "{字段说明【\n" +
	           "】")
	   public ExecuteResult<List<MappingLogModel>> queryLogInfos(@RequestBody MappingLogQueryDto query){
	       return mappingTemplateService.queryLogInfos(query);
	   }
	    
	   /**
		  * @Title saveLogInfo 
		  * @Description (日志保存或新增接口)
		  * @param model (MappingLogAddDto 所要保存的日志信息)
		  * @return (接口的执行情况)
		  */
	   @PostMapping("/log/save")
	   @ApiOperation(value = "日志保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	   public ExecuteResult<String> saveLogInfo(@RequestBody MappingLogAddDto model) {
		   
	       return mappingTemplateService.saveLogInfo(model);
	   }
	   
	   /**
		  * @Title logExport 
		  * @Description (根据筛选条件进行日志查询导出)
		  * @param query (MappingLogQueryDto,查询条件参数)
		  * @param response (HttpServletResponse，响应信息)
		  */
	   @PostMapping("/log/export")
	   @ApiOperation(value = "日志查询导出", notes = "{字段说明【\n" +
	 		"】")
	   public void logExport(HttpServletResponse response,@RequestBody MappingLogQueryDto query) {
		 	query.setCurrent(0);
		   	query.setSize(0);
		   	//查询日志数据
		    ExecuteResult<List<MappingLogModel>> result = mappingTemplateService.queryLogInfos(query);
		    //对数据写入excel
		    List<MappingLogModel> datas = null;
		    if("S".equalsIgnoreCase(result.getResultCode())) {
		    	 datas = result.getData();
		    }else {
		    	 datas = new ArrayList<MappingLogModel>();
		    }
		    //对数据写入excel
		    String rootPath = getFileCacheRootPath(fileBasicConfig);
		    File dir = new File(rootPath);
		    if(!dir.exists()) {
		    	dir.mkdirs();
		    }
		    String fileName = "出图日志"+System.currentTimeMillis() + ".xlsx";
		    String targetFilePath = rootPath+fileName;
		    //将生成的excel，进行写出
	        FileInputStream fis = null;
	        BufferedInputStream bis = null;
		   	try {
		   		boolean flag = EasyExcelUtils.writeExcelSingleSheetByAllFields(targetFilePath, "出图日志记录", MappingLogModel.class,  datas);
				 // 实现文件下载
		   		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		        response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(fileName, Constants.UTF8));
		        response.setDateHeader("Expires", 0);
		        response.setHeader("Pragma", "no-cache");
		        response.setHeader("Cache-Control", "no-cache");
		        OutputStream os = response.getOutputStream();
	            byte[] buffer = new byte[1024];
	            fis = new FileInputStream(new File(targetFilePath));
	            bis = new BufferedInputStream(fis);
	            int i = bis.read(buffer);
	            while (i != -1) {
	                os.write(buffer, 0, i);
	                i = bis.read(buffer);
	            }
	            os.flush();
	            response.flushBuffer();
			} catch (Exception e) {
				logger.error("导出日志出现异常，原因：【{}】,详情：", e.getMessage(), e);
			}finally {
	            if (bis != null) {
	                try {
	                    bis.close();
	                } catch (IOException e) {
	                	logger.error(e.getMessage());
	                }
	            }
	            if (fis != null) {
	                try {
	                    fis.close();
	                } catch (IOException e) {
	                	logger.error(e.getMessage());
	                }
	            }
	        }
	 }

		/**
	    * 获取数据本地存储根目录
	    * @return
	    */
	   private String getFileCacheRootPath(FileBasicConfig commonConfig) {
	   	String names= System.getProperty("os.name");
	   	String baseFilePath;
	   	 if(names.toLowerCase().startsWith("win")){
	        	baseFilePath = commonConfig.getLocalCachePathWin();
	        }else{
	        	baseFilePath = commonConfig.getLocalCachePathLin();
	        }
	   	 return baseFilePath;
	   }
	
	
}
