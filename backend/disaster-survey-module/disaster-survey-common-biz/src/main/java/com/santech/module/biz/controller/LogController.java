package com.santech.module.biz.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.entity.model.JcLogModel;
import com.santech.core.framework.entity.view.LogQueryDto;
import com.santech.core.framework.service.LogService;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.excel.easyexcel.EasyExcelUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Common-日志管理相关功能接口")
@RestController
@RequestMapping("/web-disaster-common/web/disaster/common/log")
public class LogController {
	private static final  Logger logger = LoggerFactory.getLogger(LogController.class);
    @Resource
	private FileBasicConfig fileBasicConfig;
    @Resource
    private LogService logService;

    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获LogController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
   @ExceptionHandler(Exception.class)
   public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("LogController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
   }
       
    /**
	  * @Title queryLogInfos 
	  * @Description (根据筛选条件批量查询日志录信息)
	  * @param query (LogQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
   @PostMapping("/query")
   @ApiOperation(value = "日志批量查询接口（可分页）", notes = "{字段说明【\n" +
           "】")
   public ExecuteResult<List<JcLogModel>> queryLogInfos(@RequestBody LogQueryDto query){
       return logService.queryLogInfos(query);
   }
    
   /**
	  * @Title saveLogeInfo 
	  * @Description (日志保存或新增接口)
	  * @param model (JcLogModel 所要保存的日志信息)
	  * @return (接口的执行情况)
	  */
   @PostMapping("/save")
   @ApiOperation(value = "日志保存或新增接口", notes = "{字段说明【\n" +
           "】")
   public ExecuteResult<String> saveLogeInfo(@RequestBody JcLogModel model) {
       return logService.saveLogeInfo(model);
   }
   
   /**
	  * @Title logExport 
	  * @Description (根据筛选条件进行日志查询导出)
	  * @param query (LogQueryDto,查询条件参数)
	  * @param response (HttpServletResponse，响应信息)
	  */
   @PostMapping("/export")
   @ApiOperation(value = "日志查询导出", notes = "{字段说明【\n" +
 		"】")
   public void logExport(HttpServletResponse response,@RequestBody LogQueryDto query) {
	 	query.setCurrent(0);
	   	query.setSize(0);
	   	//查询日志数据
	    ExecuteResult<List<JcLogModel>> result = logService.queryLogInfos(query);
	    //对数据写入excel
	    List<JcLogModel> datas = null;
	    if("S".equalsIgnoreCase(result.getResultCode())) {
	    	 datas = result.getData();
	    }else {
	    	 datas = new ArrayList<JcLogModel>();
	    }
	    //对数据写入excel
	    String rootPath = getFileCacheRootPath(fileBasicConfig);
	    File dir = new File(rootPath);
	    if(!dir.exists()) {
	    	dir.mkdirs();
	    }
	    String fileName = "系统日志"+System.currentTimeMillis() + ".xlsx";
	    String targetFilePath = rootPath+fileName;
	    //将生成的excel，进行写出
        FileInputStream fis = null;
        BufferedInputStream bis = null;
	   	try {
	   		boolean flag = EasyExcelUtils.writeExcelSingleSheetByAllFields(targetFilePath, "系统日志记录", JcLogModel.class,  datas);
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
