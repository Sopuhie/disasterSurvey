package com.santech.module.biz.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.view.ChunkMergeParamsModel;
import com.santech.core.framework.entity.view.ChunkTaskModel;
import com.santech.core.framework.entity.view.ChunkUploadParamsModel;
import com.santech.core.framework.entity.view.UploadFileVo;
import com.santech.core.framework.service.FileHandlerService;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: FileHandlerController.java 
 * @Description：文件操作相关接口
 * @Version： V1.0
 * @Modify：
 */
@RestController
@Api(tags = "Common-项目文件操作功能接口")
@RequestMapping("/web-disaster-common/web/disaster/common/file")
public class FileHandlerController {
    public static final Logger logger = LoggerFactory.getLogger(FileHandlerController.class);
    
    @Autowired
    @Qualifier("fileLocalHandlerServiceImpl")
	private FileHandlerService fileHandlerService;
    
    /**
	 * @Title exceptionHandler
	 * @Description (用来捕获FileHandlerController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
	 * @param e(Exception,所有异常的父类异常)
	 * @return (接口执行情况)
	 */
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> exceptionHandler(Exception e){
    	logger.error("FileHandlerController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }
    /**
     * @Title uploadFile
	 * @Description (此为单文件上传接口对应的业务层方法)
     * @param file(MultipartFile 上传的文件)
     * @return (接口的执行情况及文件信息)
     */
    @ApiOperation(value = "单文件上传接口",notes = "{字段说明【\n" +
            "file(MultipartFile 上传文件，必传)\n" +
            "】")
    @PostMapping("/singleUpload")
    public ExecuteResult<UploadFileVo> singleFileUpload (@RequestPart(value = "file") MultipartFile file){
    	
        return fileHandlerService.uploadFile(file);
    }
    
    /**
     * @Title filesUpload
 	 * @Description (多文件上传接口-POST，上传的文件，根据文件扩展名，默认放到对应的目录，并通过改名字进行磁盘存放)
     * @param files(上传的文件)
     * @return (ExecuteResult<JSONObject> 上传成功与失败的文件信息)
     */
    @ApiOperation(value = "多文件上传",notes = "{字段说明【\n" +
            "files(上传文件)\n" +
            "】")
    @PostMapping(value="/multipleUpload",consumes="multipart/*",headers="content-type=multipart/form-data")
    public ExecuteResult<JSONObject> filesUpload (@RequestPart(value = "files") MultipartFile[] files){
    	
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
	    		ExecuteResult<UploadFileVo> upResult = fileHandlerService.uploadFile(file);
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
     * @Title deleteFile
	 * @Description (单文件删除接口-GET，仅限单文件删除)
     * @param request (HttpServletRequest，用来获取当前登录人信息)
     * @param filePath(目标文件的相对路径)
     * @return (接口的执行情况)
     */
    @LogSystem(target = "物理磁盘文件删除", type = Constants.LOG_TYPE_DELETE)
    @ApiOperation(value = "删除单个文件",notes = "{字段说明【\n" +
            "filePath(目标删除文件相对路径，含文件名及后缀)\n" +
            "】")
    @GetMapping("/delete")
    public ExecuteResult<String> deleteFile (@RequestParam(value = "filePath",required = true) String filePath){
    	 
    	return fileHandlerService.deleteFile(filePath);
    }
   
   
   /**
    * @Title downloadSmallFile
	* @Description (单文件下载接口-GET，仅限单文件下载)
    * @param fileName(下载文件的文件名称)
    * @param filePath(目标文件的相对路径)
    * @return (ResponseEntity<byte[]> 下载的文件流信息)
    */
    @ApiOperation(value = "下载文件接口，适用小文件",notes = "{字段说明【\n" +
            "fileName(下载的文件名带有后缀)\n" +
            "filePath(下载文件相对路径(qtwj/123.txt))\n" +
            "】")
    @GetMapping(value="/download")
    public ResponseEntity<byte[]> downloadSmallFile (@RequestParam(value = "fileName", required = true) String fileName , @RequestParam(value = "filePath",required = true) String filePath){
      
    	logger.info("下载文件，入参，下载文件名称[{}]，下载文件相对路径[{}]。",fileName,filePath);
    	ResponseEntity<byte[]> responseEntity = null;
    	try {
			if(filePath.lastIndexOf(".")>-1) {
				HttpHeaders headers = new HttpHeaders();
    	        headers.setContentDispositionFormData("attachment",new String(fileName.getBytes(Constants.UTF8),"ISO-8859-1"));
    	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    	        ExecuteResult<byte[]> outResult = fileHandlerService.downloadSmallFile(filePath);
    	        if("S".equalsIgnoreCase(outResult.getResultCode())) {
        	        return ResponseEntity
                            .ok()
                            .headers(headers)
                            .contentType(MediaType.parseMediaType("application/octet-stream"))
                            .body(outResult.getData());
    	        }else {
    	        	responseEntity = ResponseEntity.notFound().build();
    	        }
			}else {
				responseEntity = ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			logger.error("下载文件[{}]异常,详情：",filePath,e);
			responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    	return responseEntity;
    }
    
    /**
     * @Title downloadBigFile
 	 * @Description (单文件下载接口-GET，仅限单文件下载)
     * @param fileName(下载文件的文件名称)
     * @param filePath(目标文件的相对路径)
     * @return 
     */
     @ApiOperation(value = "下载文件接口，大小文件均适用",notes = "{字段说明【\n" +
             "fileName(下载的文件名带有后缀)\n" +
             "filePath(下载文件相对路径(qtwj/123.txt))\n" +
             "】")
     @GetMapping(value="/download2")
     public void downloadBigFile (HttpServletResponse response,@RequestParam(value = "fileName", required = true) String fileName , @RequestParam(value = "filePath",required = true) String filePath){
       
     	logger.info("下载文件，入参，下载文件名称[{}]，下载文件相对路径[{}]。",fileName,filePath);
     	try {
            // 实现文件下载
            fileHandlerService.downloadBigFile(response, fileName, filePath);
		} catch (Exception e) {
			logger.error("从文件服务器下载文件出现异常，详情：",e);
		}
     }
    
    
    /**
     * @Title uploadChunkFile
	 * @Description (文件块上传接口-POST，上传后的文件会被放到cgfp+文件md5码的目录中，并以分片号命名)
     * @param ChunkUploadParamsModel (文件块上传参数信息)
     * @return (接口的执行情况及上传后的文件信息)
     */
    @ApiOperation(value = "上传文件块")
    @PostMapping(value = "/chunk/upload")
    public ExecuteResult<ChunkTaskModel> uploadChunkFile(ChunkUploadParamsModel model) {
    	
        return fileHandlerService.uploadChunkFile(model);
    }
    
    /**
     * @Title checkChunkUpload
	 * @Description (分片上传的文件块信息查询接口，根据文件md5码获取上传的进度信息)
     * @param fileMd5 (文件md5码)
     * @return (接口的执行情况及上传后的文件分块上传进度等信息)
     */
    @ApiOperation(value = "检查文件块")
    @GetMapping("/chunk/check")
    public ExecuteResult<List<ChunkTaskModel>> checkChunkUpload(@RequestParam(value = "fileMd5",required = true) String fileMd5) {
    	
    	return fileHandlerService.checkChunkUpload(fileMd5);
    }
    
    
    /**
     * @Title mergeChunk
	 * @Description (分片合并接口-POST，该接口基于sftp协议，会将合并完的文件上传到文件服务器指定位置)
     * @param model (ChunkMergeParamsModel 分片合并参数)
     * @return (接口的执行情况及合并上传后的文件信息)
     */
    @ApiOperation(value = "合并文件")
    @PostMapping(value = "/chunk/merge")
    public ExecuteResult<Map<String, String>> mergeChunk(@RequestBody ChunkMergeParamsModel model) {
    	
    	return fileHandlerService.mergeChunk(model);
    }
    
}
