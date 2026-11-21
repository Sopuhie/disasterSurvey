package com.santech.module.biz.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpGet;
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
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.ZyServiceModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ServiceAddDto;
import com.santech.module.biz.entity.dto.ServiceResourceQueryDto;
import com.santech.module.biz.entity.dto.ServiceUpdateDto;
import com.santech.module.biz.entity.vo.ServiceResourceTreeVo;
import com.santech.module.biz.service.ServiceResourceService;
import com.santech.module.biz.utils.HttpClientUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/service")
@Api(tags = "Resource-数据服务资源运维管理相关功能接口")
public class ServiceResourceController {

    public static final Logger logger = LoggerFactory.getLogger(ServiceResourceController.class);
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ServiceResourceController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("ServiceResourceController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}

    @Resource
    private ServiceResourceService serviceResourceService;

    
    /**
	  * @Title queryServiceResourceInfosWithPage 
	  * @Description (根据筛选条件批量分页查询数据服务资源列表信息)
	  * @param query (ServiceResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/queryWithPage")
	 @ApiOperation(value = "数据服务资源列表分页查询接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<List<ZyServiceModel>> queryServiceResourceInfosWithPage(@RequestBody ServiceResourceQueryDto query){
		 return serviceResourceService.queryServiceResourceInfosWithPage(query);
	 }
	 
	 /**
	  * @Title queryServiceResourceTreeInfos 
	  * @Description (根据筛选条件批量查询数据服务资源列表信息并以树格式返回-不分页)
	  * @param query (ServiceResourceQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @PostMapping("/tree")
	 @ApiOperation(value = "数据服务资源树查询接口-不分页", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<ServiceResourceTreeVo> queryServiceResourceTreeInfos(@RequestBody ServiceResourceQueryDto query){
		 return serviceResourceService.queryServiceResourceTreeInfos(query);
	 }
	 
	 
	 /**
	  * @Title queryServiceResourceInfoById 
	  * @Description (根据ID获取数据服务信息接口)
	  * @param serviceId (服务ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getById")
	 @ApiOperation(value = "根据ID获取数据服务信息接口", notes = "{字段说明【\n" +
			 "serviceId（数据服务资源id）\n" +
	         "】")
	 public ExecuteResult<ZyServiceModel> queryServiceResourceInfoById(@RequestParam("serviceId") String serviceId){
		 return serviceResourceService.queryServiceResourceInfoById(serviceId);
	 }
    
	 /**
	  * @Title saveServiceResourceInfo 
	  * @Description (数据服务资源保存或新增接口)
	  * @param model (ServiceAddDto 新增数据服务资源所要保存的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "数据服务资源新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/add")
	 @ApiOperation(value = "数据服务资源保存或新增接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> saveServiceResourceInfo(HttpServletRequest request, @RequestBody ServiceAddDto model) {
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
			  return ExecuteResult.error("【"+model.getCjr()+"】非管理员不能添加数据服务资源", null);
		  }
	      return serviceResourceService.saveServiceResourceInfo(model);
	 }
    
	 /**
	  * @Title updateServiceResourceInfo 
	  * @Description (数据服务资源保存或新增接口)
	  * @param model (ZyServiceModel 新增数据服务资源所要更新的信息)
	  * @return (接口的执行情况)
	  */
	 @LogSystem(target = "数据服务资源更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/update")
	 @ApiOperation(value = "数据服务资源更新接口", notes = "{字段说明【\n" +
	           "】")
	 public ExecuteResult<String> updateServiceResourceInfo(HttpServletRequest request, @RequestBody ServiceUpdateDto model) {
		 String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改数据服务资源", null);
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
	      return serviceResourceService.updateServiceResourceInfo(model);
	 }
	/**
	 * @Title deleteServiceResourceInfo
	 * @Description (此方法用于对数据服务资源进行删除)
	 * @param serviceId(数据服务资源ID)
	 * @return (删除的执行结果)
	 */
	@LogSystem(target = "数据服务资源删除", type = Constants.LOG_TYPE_DELETE)
    @GetMapping("/delete")
    @ApiOperation(value = "数据服务资源删除接口", notes = "{字段说明【\n" +
    		"serviceId（数据服务资源id）\n" +
    		"】")
    public ExecuteResult<String> deleteServiceResourceInfo(HttpServletRequest request,@RequestParam("serviceId") String serviceId){
		String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能删除数据服务资源", null);
		}
		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return serviceResourceService.deleteServiceResourceInfo(serviceId, userName);
    }
	

  
	/**
     * 通过serviceName获取数据
     *
     * @param mtoken
     * @return
     */
    @RequestMapping("/proxy/forwarding/{serviceId}/**")
    @ApiOperation(value = "通过serviceName获取数据")
    public ResponseEntity<Object> getDataByMtoken(@PathVariable("serviceId") String serviceId, @RequestBody(required = false) Map<String, Object> map, HttpServletRequest servletRequest) throws UnsupportedEncodingException {
    	//获取请求url
        String requestURI = servletRequest.getRequestURI();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //通过mtoken判断是否可调用
        ExecuteResult<ZyServiceModel> serviceResult = serviceResourceService.queryServiceResourceInfoById(serviceId);
        if("F".equalsIgnoreCase(serviceResult.getResultCode())) {
        	return  new ResponseEntity<>(serviceResult, headers, HttpStatus.OK);
        }
        ZyServiceModel serviceModel = serviceResult.getData();
       /* if ("0".equals(serviceModel.getStatus())) {
            return new ResponseEntity<>(ExecuteResult.error("接口状态为不可用，请联系管理员", null), headers, HttpStatus.OK);
        }*/
        //真实服务注册地址
        String serviceUrl = serviceModel.getServiceUrl();
        //服务类型
        //String serviceType = serviceModel.getServiceType();

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        StringBuilder params = new StringBuilder("");
        if (StringUtil.isNotEmpty(parameterMap)) {
            Set<String> keys = parameterMap.keySet();
            for (String key : keys) {
                if ("".equals(params.toString())) {
                	if(serviceUrl.contains("?")) {
                		params.append("&").append(key).append("=").append(URLEncoder.encode(parameterMap.get(key)[0], Constants.UTF8));
                	}else {
                		params.append("?").append(key).append("=").append(URLEncoder.encode(parameterMap.get(key)[0], Constants.UTF8));
                	}
                } else {
                	params.append("&").append(key).append("=").append(URLEncoder.encode(parameterMap.get(key)[0], Constants.UTF8));
                }
            }
        }
        
        if (requestURI.contains("/web/disaster/resource/service/proxy/forwarding/")) {
        	logger.info("requestURI【{}】",requestURI);// TODO 确认拿到的地址是啥，然后再采用配置替换的方式
        	String replace = "";
        	if(requestURI.contains("/web-disaster-resource/")) {
        		if(requestURI.contains("/zuul/web-disaster-resource/")) {
        			replace = requestURI.replace("/zuul/web-disaster-resource/web/disaster/resource/service/proxy/forwarding/", "");
        		}else {
        			replace = requestURI.replace("/web-disaster-resource/web/disaster/resource/service/proxy/forwarding/", "");
        		}
        	}else {
        		replace = requestURI.replace("/web/disaster/resource/service/proxy/forwarding/", "");
        	}
            String substring = replace.substring(replace.indexOf("/"), replace.length());
            serviceUrl += substring;
        }else {
        	serviceUrl = requestURI;
        }
        serviceUrl = serviceUrl + params;
        if (StringUtil.isEmpty(map)) {
            HttpGet request = new HttpGet(serviceUrl);
            try {
                //请求返回的是字符串
                if (serviceUrl.indexOf("f=json") > -1) {
                    String result = HttpClientUtil.resultData(request);
                    return new ResponseEntity<>(result, headers, HttpStatus.OK);

                    //请求返回的是图片  图片、切片
                } else if (serviceUrl.indexOf("/export?") > -1 || serviceUrl.indexOf("/tile/") > -1) {
                    byte[] b = HttpClientUtil.resultDataByte(request);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
                    return new ResponseEntity<>(b, headers, HttpStatus.OK);
                } else {
                    String result = HttpClientUtil.resultData(request);
                    headers.add(HttpHeaders.CONTENT_TYPE, "text/html;charset=utf-8");
                    return new ResponseEntity<>(result, headers, HttpStatus.OK);
                }
            } catch (Exception e) {
                logger.error("转发请求【{}】出现异常，详情：",serviceUrl,e);
                return new ResponseEntity<>(ExecuteResult.error("请求出错，请联系管理员", null), headers, HttpStatus.OK);
            }
        } else {
            PrintWriter out = null;
            BufferedReader in = null;
            StringBuilder result = new StringBuilder("");
            try {
                URL realUrl = new URL(serviceUrl);
                // 打开和URL之间的连接
                URLConnection conn = realUrl.openConnection();
                // 设置通用的请求属性
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(map);
                // flush输出流的缓冲
                out.flush();
                // 定义BufferedReader输入流来读取URL的响应
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                logger.error("发送POST请求【{}】出现异常,详情：",serviceUrl,e);
                return new ResponseEntity<>(ExecuteResult.error("请求出错，请联系管理员", null), headers, HttpStatus.OK);
            }finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return new ResponseEntity<>(result.toString(), headers, HttpStatus.OK);
        }
    }
	
	

}
