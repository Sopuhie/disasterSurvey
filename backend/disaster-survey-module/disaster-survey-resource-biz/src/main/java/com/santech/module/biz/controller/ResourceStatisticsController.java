package com.santech.module.biz.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ResourceStatisticsParamsDto;
import com.santech.module.biz.entity.vo.ResourceStatisticsResultVo;
import com.santech.module.biz.service.FileResourceService;
import com.santech.module.biz.service.ServiceResourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/web-disaster-resource/web/disaster/resource/statistics")
@Api(tags = "Resource-资源统计相关功能接口")
public class ResourceStatisticsController {

    public static final Logger logger = LoggerFactory.getLogger(ResourceStatisticsController.class);
    
    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获ResourceStatisticsController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
	@ExceptionHandler(Exception.class)
	public ExecuteResult<String> exceptionHandler(Exception e){
	   	logger.error("ResourceStatisticsController中捕获的其他异常，异常详情：",e);
	    return ExecuteResult.error(e.getMessage(),null);
	}
	@Resource
	private ServiceResourceService serviceResourceService;
	@Resource
    private FileResourceService fileResourceService;
    
    /**
	  * @Title statisticsResourceForLeftTab 
	  * @Description (资源列表左侧全局统计菜单查询接口)
	  * @param query (ResourceStatisticsParamsDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
    @PostMapping("/lefttab/query")
	@ApiOperation(value = "资源列表左侧全局统计菜单查询接口", notes = "{字段说明【\n" +
			"服务管理左侧树入参示例：{\"actionName\": \"平台服务\",\"dictType\": \"\",\"groupColumn\": \"\",\"resourceType\": \"service\"}"+
			"附件管理左侧树入参示例：{\"actionName\": \"\",\"dictType\": \"zygl_wjzylx\",\"groupColumn\": \"TYPE\",\"resourceType\": \"file\"}"+
			"接口管理左侧树入参示例：{\"actionName\": \"\",\"dictType\": \"zygl_apibusinesstype\",\"groupColumn\": \"BUSINESSTYPE\",\"resourceType\": \"api\"}"+
	         "】")
    public ExecuteResult<List<ResourceStatisticsResultVo>> statisticsResourceForLeftTab(HttpServletRequest request,@RequestBody ResourceStatisticsParamsDto query){
    	ExecuteResult<List<ResourceStatisticsResultVo>> result = null;
    	if(StringUtil.isEmpty(query.getAdminFlag())) {
    		query.setAdminFlag((String) request.getAttribute(Constants.ADMIN_FLAG));//request.getHeader(Constants.ADMIN_FLAG)
    	}
    	switch(query.getResourceType()) {
    	case "service":
    		result = serviceResourceService.statisticsServiceResourceForLeftTab(query.getActionName());
    		break;
    	case "file":
    		result = fileResourceService.statisticsFileResourceForLeftTab(query.getAdminFlag(),query.getGroupColumn(), query.getDictType());
    		break;
		default:
			result = ExecuteResult.error("不能识别的统计业务类型", null);
			break;
    	}
    	return result;
    	
    }
	
	
	
    


}
