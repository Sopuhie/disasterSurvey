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
import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.DepartmentAddDto;
import com.santech.module.biz.entity.dto.DepartmentUpdateDto;
import com.santech.module.biz.entity.dto.DeptQueryDto;
import com.santech.module.biz.service.DepartmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: DepartmentController.java
 * @Description：部门或组织机构相关接口
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/department")
@Api(tags = "Auth-灾后调查评估系统用户部门或组织机构相关功能接口")
public class DepartmentController {
	
	private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    @Resource
    private DepartmentService departmentService;
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("DepartmentController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }

	/**
	  * @Title queryDepartmentInfos 
	  * @Description (根据筛选条件进行批量分页查询部门机构信息)
	  * @param model (DeptQueryDto,部门检索条件参数)
	  * @return (接口的执行情况及符合条件的部门记录信息)
	  */
    @PostMapping("/queryWithPage")
    @ApiOperation(value = "批量查询部门或机构信息接口", notes = "{字段说明【\n" +
    		"】")
    public ExecuteResult<List<QxDepartmentModel>> queryDepartmentInfos(@RequestBody DeptQueryDto model) {
    	
    	return departmentService.queryDepartmentInfos(model);
    }
    
    /**
	  * @Title queryDepartmentTreeInfos 
	  * @Description (查询部门或机构信息并组装成树结构返回)
	  * @return (接口的执行情况及部门树信息)
	  */
   @PostMapping("/tree")
   @ApiOperation(value = "查询部门或机构信息并组装成树结构返回的接口", notes = "{字段说明【\n" +
   		"】")
   public ExecuteResult<List<QxDepartmentModel>> queryDepartmentTreeInfos() {
   	
   	return departmentService.queryDepartmentTreeInfos();
   }
    
    /**
	 * @Title addDepartment
	 * @Description (此方法用于添加部门信息)
	 * @param dto(DepartmentAddDto,部门信息)
	 * @return (添加执行结果)
	 */
    @LogSystem(target = "部门新增", type = Constants.LOG_TYPE_ADD)
    @PostMapping("/add")
    @ApiOperation(value = "部门新增接口", notes = "{字段说明【\n" +
    		"】")
    public ExecuteResult<String> addDepartment(HttpServletRequest request,@RequestBody DepartmentAddDto dto){
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改部门信息", null);
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
    	return departmentService.addDepartment(dto);
    }
	
	
	/**
	 * @Title deleteDepartment
	 * @Description (此方法用于对部门进行删除)
	 * @param departmentId(部门ID)
	 * @return (删除的执行结果)
	 */
    @LogSystem(target = "部门删除", type = Constants.LOG_TYPE_DELETE)
    @GetMapping("/delete")
    @ApiOperation(value = "部门或机构删除接口", notes = "{字段说明【\n" +
    		"departmentId（部门id）\n" +
    		"】")
    public ExecuteResult<String> deleteDepartment(HttpServletRequest request,@RequestParam("departmentId")String departmentId){
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能删除部门信息", null);
		}
		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return departmentService.deleteDepartment(departmentId, userName);
    }
	
	
	/**
	 * @Title updateDepartment
	 * @Description (此方法用于对部门信息进行修改)
	 * @param dto(DepartmentUpdateDto,部门信息)
	 * @return (更新的执行结果)
	 */
    @LogSystem(target = "部门更新", type = Constants.LOG_TYPE_UPDATE)
    @PostMapping("/update")
    @ApiOperation(value = "部门信息更新接口", notes = "{字段说明【\n" +
    		"】")
    public ExecuteResult<String> updateDepartment(HttpServletRequest request,@RequestBody DepartmentUpdateDto dto){
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加或修改部门信息", null);
		}
    	if(StringUtil.isEmpty(dto.getZhgxr())) {
    		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
    		String userName = "";
    		try {
    			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
    			dto.setZhgxr(userName);
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
    	}
		return departmentService.updateDepartment(dto);
    }
    
    
}
