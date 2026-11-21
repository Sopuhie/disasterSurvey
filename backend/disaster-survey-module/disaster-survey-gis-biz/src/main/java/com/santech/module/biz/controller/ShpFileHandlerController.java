package com.santech.module.biz.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ShpFileInfoDto;
import com.santech.module.biz.service.ShpFileHandlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: ShpFileHandlerController.java
 * @Description：Shp文件操作相关接口
 * @Version： V1.0
 * @Modify：
 */

@RestController
@RequestMapping("/web-disaster-shp/web/disaster/shp/file")
@Api(tags = "Gis-Shp-文件操作相关接口")
public class ShpFileHandlerController {
	private static final Logger logger = LoggerFactory.getLogger(ShpFileHandlerController.class);
	

	@Resource
	private ShpFileHandlerService shpFileHandlerService;

	
	/**
     * @Title uploadFile
	 * @Description (此为单文件上传接口对应的业务层方法)
     * @param file(MultipartFile 上传的文件)
     * @return (接口的执行情况及文件信息)
     */
    @ApiOperation(value = "SHP文件上传接口",notes = "{字段说明【\n" +
            "file(MultipartFile 上传shp文件，必传)\n" +
            "】")
    @PostMapping("/singleUpload")
    public ExecuteResult<ShpFileInfoDto> uploadShpFile (@RequestPart(value = "file") MultipartFile file){
    	
        return shpFileHandlerService.uploadShpFile(file);
    }
    
    
    /**
     * @Title downloadShpFile
 	 * @Description (单文件下载接口-GET，仅限单文件下载)
     * @param fileName(下载文件的文件名称)
     * @param filePath(目标文件的相对路径)
     * @return (ResponseEntity<byte[]> 下载的文件流信息)
     */
     @ApiOperation(value = "下载SHP文件接口，适用小文件",notes = "{字段说明【\n" +
             "fileName(下载的文件名带有后缀)\n" +
             "filePath(下载文件相对路径(qtwj/123.txt))\n" +
             "】")
     @GetMapping(value="/download")
     public ResponseEntity<byte[]> downloadShpFile (@RequestParam(value = "fileName", required = true) String fileName , @RequestParam(value = "filePath",required = true) String filePath){
       
     	logger.info("下载文件，入参，下载文件名称[{}]，下载文件相对路径[{}]。",fileName,filePath);
     	ResponseEntity<byte[]> responseEntity = null;
     	try {
 			if(filePath.lastIndexOf(".")>-1) {
 				HttpHeaders headers = new HttpHeaders();
     	        headers.setContentDispositionFormData("attachment",new String(fileName.getBytes("UTF-8"),"ISO-8859-1"));
     	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
     	        ExecuteResult<byte[]> outResult = shpFileHandlerService.downloadShpFileByZip(filePath);
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
      * @Title deleteShpFile
 	  * @Description (单文件删除接口-GET，仅限单文件删除)
      * @param request (HttpServletRequest，用来获取当前登录人信息)
      * @param filePath(目标文件的相对路径)
      * @return (接口的执行情况)
      */
     @LogSystem(target = "物理磁盘SHP文件删除", type = Constants.LOG_TYPE_DELETE)
     @ApiOperation(value = "删除SHP文件",notes = "{字段说明【\n" +
             "filePath(目标删除文件相对路径，含文件名及后缀)\n" +
             "】")
     @GetMapping("/delete")
     public ExecuteResult<String> deleteShpFile (@RequestParam(value = "filePath",required = true) String filePath){
     	 
     	return shpFileHandlerService.deleteShpFile(filePath);
     }

}
