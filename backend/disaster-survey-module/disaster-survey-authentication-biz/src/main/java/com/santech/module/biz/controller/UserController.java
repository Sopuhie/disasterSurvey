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
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.core.framework.entity.model.QxUserSimpleModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.UserAddDto;
import com.santech.module.biz.entity.dto.UserPasswordChangeDto;
import com.santech.module.biz.entity.dto.UserQueryDto;
import com.santech.module.biz.entity.dto.UserUpdateDto;
import com.santech.module.biz.entity.vo.UserGroupByDeptVo;
import com.santech.module.biz.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Title: UserController.java
 * @Description：用户相关接口
 * @Version： V1.0
 * @Modify：   
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/user")
@Api(tags = "Auth-灾后调查评估系统用户信息相关功能接口")
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;
    
    
    @ExceptionHandler(Exception.class)
    public ExecuteResult<String> handler(Exception e){
    	logger.error("UserController中捕获的其他异常，异常详情：",e);
        return ExecuteResult.error(e.getMessage(),null);
    }

    
    /**
	  * @Title addUser 
	  * @Description (新增用户信息，该接口为本地添加用户的接口)
	  * @param dto (UserAddDto添加的用户信息)
	  * @return (接口的执行情况)
	  */
    @LogSystem(target = "添加用户", type = Constants.LOG_TYPE_ADD)
    @ApiOperation(value = "添加用户信息接口")
    @PostMapping("/add")
    public ExecuteResult<String> addUser(HttpServletRequest request,@RequestBody UserAddDto dto) {
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		  if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能添加用户", null);
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
    	return userService.addUser(dto);
    }
    
    /**
	 * @Title changePasswordForFirstLogin
	 * @Description (此接口用于用户初次登录时的密码修改)
	 * @param model(UserPasswordChangeModel,用户密码更改信息)
	 * @return (执行结果)
	 */
    @LogSystem(target = "用户密码修改", type = Constants.LOG_TYPE_UPDATE)
    @ApiOperation(value = "用户密码修改接口")
    @PostMapping("/changePassword")
    public ExecuteResult<String> changePasswordForFirstLogin(HttpServletRequest request,@RequestBody UserPasswordChangeDto model){
    	if(StringUtil.isEmpty(model.getLoginUser())) {
    		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
    		String userName = "";
    		try {
    			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
    			model.setLoginUser(userName);
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
    	}
		 return userService.changePasswordForFirstLogin(model);
	}
	/**
	 * @Title deleteUser
	 * @Description (此方法用于对用户进行删除，为假删除)
	 * @param userid(删除的用户ID)
	 * @return (删除执行结果)
	 */
    @LogSystem(target = "删除用户", type = Constants.LOG_TYPE_DELETE)
    @ApiOperation(value = "删除用户信息接口")
    @GetMapping("/delete")
    public ExecuteResult<String> deleteUser(HttpServletRequest request,@RequestParam("userid") String userid){
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能删除用户信息", null);
		}
		String loginUser = (String) request.getAttribute(Constants.HEADER_USERNAME);//request.getHeader(Constants.HEADER_USERNAME);
		String userName = "";
		try {
			userName = StringUtil.isNotEmpty(loginUser)?URLDecoder.decode(loginUser, Constants.UTF8):"";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return userService.deleteUser(userid, userName);
	}
	
	
	/**
	 * @Title updateUser
	 * @Description (此方法用于对用户进行修改)
	 * @param dto(UserUpdateDto,用户信息)
	 * @return (更新执行结果)
	 */
    @LogSystem(target = "修改用户", type = Constants.LOG_TYPE_UPDATE)
    @ApiOperation(value = "用户信息修改接口")
    @PostMapping("/update")
    public ExecuteResult<String> updateUser(HttpServletRequest request,@RequestBody UserUpdateDto dto){
    	String adminFlag = (String) request.getAttribute(Constants.ADMIN_FLAG);//request.getHeader(Constants.ADMIN_FLAG);
		if(!"1".equals(adminFlag)) {
			  return ExecuteResult.error("非管理员不能修改用户信息", null);
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
		return userService.updateUser(dto);
	}
    
    
    
    /**
	  * @Title queryUserInfosWithPage 
	  * @Description (根据筛选条件进行批量分页查询用户信息)
	  * @param model (UserQueryDto,用户检索条件参数)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
   @PostMapping("/queryWithPage")
   @ApiOperation(value = "批量分页查询用户信息接口", notes = "{字段说明【\n" +
   		"】")
   public ExecuteResult<List<QxUserSimpleModel>> queryUserInfosWithPage(@RequestBody UserQueryDto model) {
   	
   	return userService.queryUserInfosWithPage(model);
   }
   	/**
	  * @Title queryUserInfosGroupByDept 
	  * @Description (根据部门进行分组分级查询全量有效用户)
	  * @return (接口的执行情况及符合条件的用户记录信息)
	  */
	 @GetMapping("/queryWithDept")
	 @ApiOperation(value = "批量查询用户信息接口-不分页-根据部门进行分组分级-无角色信息", notes = "{字段说明【\n" +
	 		"】")
	 public ExecuteResult<List<UserGroupByDeptVo>> queryUserInfosGroupByDept() {
	 	
	 	return userService.queryUserInfosGroupByDept();
	 }
	 
	 /**
	  * @Title queryUserInfoById 
	  * @Description (根据ID获取用户信息接口)
	  * @param userId (用户ID)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	 @GetMapping("/getById")
	 @ApiOperation(value = "根据ID获取用户信息接口", notes = "{字段说明【\n" +
			 "userId（用户id）\n" +
	         "】")
	 public ExecuteResult<QxUserModel> queryUserInfoById(@RequestParam("userId") String userId){
		 return userService.queryUserInfoById(userId);
	 }
   
}
