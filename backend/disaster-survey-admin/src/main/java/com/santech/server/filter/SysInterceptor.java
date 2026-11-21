package com.santech.server.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.linewell.hsfjsso.util.HsfjSsoUtil;
import com.linewell.sso.SsoUtil;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.JWTUtil;
import com.santech.framework.utils.tool.ResponseUtil;
import com.santech.framework.utils.tool.StringUtil;

/**
 * 系统拦截器
 */
@Component
public class SysInterceptor implements HandlerInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(SysInterceptor.class);
	private static final String INVALID = "invalid";//无效 
	private static final String VALID = "valid";//有效
	private static String SSOFLAG = "2";//单点登录标识，1一级单点，2二级单点
	@Value("${single.login.enable:false}")
	private boolean singleLogin;
	
	//政务中台版是否启用-二级登录
	@Value("${sso.enable:true}")
	protected boolean ssoEnable;
	// 福建省数据中心单点是否启用-一级登录
	@Value("${yjsso.enabled:false}")
	protected boolean yjssoEnable;
	//海上福建版是否启用
	@Value("${hsfjsso.enable:false}")
	protected boolean hsfjssoEnable;
	@Resource
	private SsoUtil ssoUtil;
	@Resource
	private HsfjSsoUtil hsfjSsoUtil;
	@Resource
    private RedisUtil redisUtil;
	
    /**
     * 在Controller处理之前进行调用
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //CROS复杂请求时会首先发送一个OPTIONS请求做嗅探，所以得通过
        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String requestURI = request.getRequestURI()==null?"":request.getRequestURI();
       //单点登录相关接口不需拦截
        if (requestURI.contains("/web/disaster/authentication/user/verify/sso")) {
        	if(requestURI.contains("/web/disaster/authentication/user/verify/sso/zwzt/login")) {
        		SSOFLAG = "2";
        	}
            logger.info("二级单点登录相关接口【{}】,暂时无需进行token拦截验证 ",requestURI);
            return true;
        }
        if (requestURI.contains("/web/disaster/authentication/user/verify/yjsso")) {
        	if(requestURI.contains("/web/disaster/authentication/user/verify/yjsso/ssoLogin")) {
        		SSOFLAG = "1";
        	}
            logger.info("一级单点登录相关接口【{}】,暂时无需进行token验证 ",requestURI);
            return true;
        }
       //进行指定token认证
       Map<String,String> result = null;
       if(ssoEnable||yjssoEnable||hsfjssoEnable) {
	    	 //单点登录的认证
    	    if(ssoEnable&&SSOFLAG.equals("2")) {
				// 二级单点
				result = zwztSSOCheckToken(request);
			} else if (yjssoEnable && SSOFLAG.equals("1")) {
				// 1级单点
				result = yjSSOCheckToken(request);
			} else {
				result = hsfjSSOCheckToken(request);
			}
	       	if(INVALID.equalsIgnoreCase(result.get("state"))) {
	       		//认证未通过
	           	logger.info("单点登录token认证，请求路径【{}】,token【{}】验证失败或不通过",request.getRequestURI(),result.get("token"));
	           	ResponseUtil.print(HttpStatus.UNAUTHORIZED.value(),response, ExecuteResult.error("R", "token auth fail", null));
	           	return false;
	       	}
       }else {
	    	 //自有登录认证
	       	result = localCheckToken(request);
	       	if(INVALID.equalsIgnoreCase(result.get("state"))) {
	           	//认证未通过
	           	logger.info("本地登录token认证，请求路径【{}】,token【{}】验证失败",request.getRequestURI(),result.get("token"));
	           	ResponseUtil.print(HttpStatus.UNAUTHORIZED.value(),response, ExecuteResult.error("R", "token auth fail", null));
	           	return false;
	         }
       }
		// 设置一些属性信息
		String username = StringUtil.isEmpty(result.get("username")) ? "" : result.get("username");
		try {
			request.setAttribute(Constants.HEADER_USERNAME, URLEncoder.encode(username, Constants.UTF8));
		} catch (UnsupportedEncodingException e) {
			logger.error("当前登录人放进请求属性中失败，用户名【{}】", username, e);
		}
		request.setAttribute(Constants.HEADER_USERID, result.get("userid"));// 用户id
		request.setAttribute("xzqdm", result.get("xzqdm"));// 用户所属行政区代码，预留
		request.setAttribute("token", result.get("token"));// token信息
		request.setAttribute(Constants.ADMIN_FLAG, result.get(Constants.ADMIN_FLAG));// 管理员标识
        return true;
    }
  //根据对接要求，政务中台单点登录的接口认证业务具体如下
    //要求每次请求都要到认证中心检查当前用户的登录态，如果认证中心返回的登录为有效，则继续业务操作，
    //如果返回的为失效则需将当前应用用户session销毁完成登出操作。通过此种方式才能实现用户在一个系统退出，
    //全部退出的效果。需要注意的是在请求用户登录态的接口时，同一个用户在同一个时间内没有必要发送多次请求，
    //在程序内部可以通过加锁的方式进行控制，从而降低认证中心的压力，甚至可以设计用户维度几秒以内的缓存，
    //通常登出后几秒内的	延迟也在接受范围内
    private Map<String,String> zwztSSOCheckToken(HttpServletRequest request) {
    	Map<String,String> result = new HashMap<String,String>();
    	 //1) 从请求中获取token
        String accessToken = request.getHeader("access_token");
        String peopleId = request.getHeader("peopleId");
        String username = request.getHeader("username");
        result.put("state", VALID);//默认有效
        result.put("token", accessToken);
         //2) 判断（如果没有token，认为用户还没有登录）
        if(StringUtil.isEmpty(accessToken)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("单点登录token验证，请求路径【{}】,未获取到token ",request.getRequestURI());
        	return result;
        }
        //3) 判断（如果没有peopleId，认为用户还没有登录）
        if(StringUtil.isEmpty(peopleId)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("单点登录token验证，请求路径【{}】,未获取到用户id",request.getRequestURI());
        	return result;
        }
        //验证token有效性
        if(!ssoUtil.checkAccessToken(accessToken)) {
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("单点登录token验证，请求路径【{}】,token验证为失效",request.getRequestURI());
        	return result;
        }
        //验证用户登录状态
        if(redisUtil.get(peopleId+"_checkHasLogin")==null) {
        	//认证通过后，为降低认证中心的压力，设置5秒的认证有效期
        	redisUtil.set(peopleId+"_checkHasLogin", accessToken, 5);
        }
        result.put("userid", peopleId);
        //result.put("accessToken", accessToken);
       
        result.put("username", username);
        result.put(Constants.ADMIN_FLAG, "1");//暂时都默认为1管理员
    	return result;
    }
    private Map<String,String> yjSSOCheckToken(HttpServletRequest request) {
    	Map<String,String> result = new HashMap<String,String>();
    	 //1) 从请求中获取token
        String ticket = request.getHeader("ticket");
        String username = request.getHeader("username");
        String peopleId = request.getHeader("peopleId");
        result.put("state", VALID);//默认有效
         //2) 判断（如果没有token，认为用户还没有登录）
        if(StringUtil.isEmpty(ticket)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("yjsso单点登录ticket验证，请求路径【{}】,未获取到ticket ",request.getRequestURI());
        	return result;
        }
        //3) 判断（如果没有peopleId，认为用户还没有登录）
        if(StringUtil.isEmpty(peopleId)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("yjsso单点登录ticket验证，请求路径【{}】,未获取到用户username",request.getRequestURI());
        	return result;
        }
        //验证token有效性
        String accessToken = String.valueOf(redisUtil.get("yjsso_"+ticket));
        if (StringUtil.isEmpty(accessToken) || "null".equals(accessToken)) {
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("yjsso单点登录ticket验证，请求路径【{}】,ticket验证为失效",request.getRequestURI());
        	return result;
        }
        result.put("userid", peopleId);
        result.put("token", accessToken);
        result.put("username", username);
        result.put(Constants.ADMIN_FLAG, "1");//暂时都默认为1管理员
    	return result;
    }
    //根据对接要求，海上福建单点登录的接口认证业务具体如下
    //要求每次请求都要到认证中心检查当前用户的登录态，如果认证中心返回的登录为有效，则继续业务操作，
    //如果返回的为失效则需将当前应用用户session销毁完成登出操作。通过此种方式才能实现用户在一个系统退出，
    //全部退出的效果。需要注意的是在请求用户登录态的接口时，同一个用户在同一个时间内没有必要发送多次请求，
    //在程序内部可以通过加锁的方式进行控制，从	而降低认证中心的压力，甚至可以设计用户维度几秒以内的缓存，
    //通常登出后几秒内的	延迟也在接受范围内
    private Map<String,String> hsfjSSOCheckToken(HttpServletRequest request) {
    	Map<String,String> result = new HashMap<String,String>();
    	 //1) 从请求中获取token
        String accessToken = request.getHeader("access_token");
        String peopleId = request.getHeader("peopleId");
        String username = request.getHeader("username");
        result.put("state", VALID);//默认有效
         //2) 判断（如果没有token，认为用户还没有登录）
        if(StringUtil.isEmpty(accessToken)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("单点登录token验证，请求路径【{}】,未获取到token ",request.getRequestURI());
        	return result;
        }
        //3) 判断（如果没有peopleId，认为用户还没有登录）
        if(StringUtil.isEmpty(peopleId)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("单点登录token验证，请求路径【{}】,未获取到用户id",request.getRequestURI());
        	return result;
        }
        //验证token有效性
        if(!hsfjSsoUtil.checkAccessToken(accessToken)) {
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("单点登录token验证，请求路径【{}】,token验证为失效",request.getRequestURI());
        	return result;
        }
        //验证用户登录状态
        if(redisUtil.get(peopleId+"_checkHasLogin")==null) {
        	if(!hsfjSsoUtil.checkHasLogin(peopleId)) {
            	result.put("state", INVALID);//invalid 无效|valid 有效
            	logger.info("单点登录token验证，请求路径【{}】,登录状态验证为无效",request.getRequestURI());
            	return result;
            }else {
            	//认证通过后，为降低认证中心的压力，设置5秒的认证有效期
            	redisUtil.set(peopleId+"_checkHasLogin", accessToken, 5);
            }
        }
        result.put("userid", peopleId);
        //result.put("accessToken", accessToken);
        result.put("token", accessToken);
        result.put("username", username);
        result.put(Constants.ADMIN_FLAG, "1");//暂时都默认为1管理员
    	return result;
    }
    
    //本地token认证机制
    private Map<String,String> localCheckToken(HttpServletRequest request) {
    	Map<String,String> result = new HashMap<String,String>();
    	 //1) 从请求中获取token
        String token = request.getHeader("access_token");//request.getHeader("Authentication-Token");
        result.put("token", token);
         //2) 判断（如果没有token，认为用户还没有登录）
        if(StringUtil.isEmpty(token)){
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("本地token认证，请求路径【{}】,未获取到token ",request.getRequestURI());
        	return result;
        }
        //验证该token是否已在黑名单
        if(singleLogin) {
        	Map<String,Object> dataMap = JWTUtil.getTokenMeta(token);
    		if(StringUtil.isNotEmpty(dataMap.get("tokenId"))) {
    			boolean expire = redisUtil.hasKey("expiretoken_"+dataMap.get("tokenId"));
    			if(expire) {
    				//已过期
    				result.put("state", INVALID);//invalid 无效|valid 有效
    	        	logger.info("本地token认证，请求路径【{}】,token【{}】已失效或已在黑名单",request.getRequestURI(),token);
    	        	return result;
    			}
    		}
        }
        
       //jwt解密验证
    	Map<String,String> jwt = JWTUtil.getDecodedToken(token);
    	//token解密有问题拒绝请求
        if (jwt==null) {
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("本地token认证，请求路径【{}】,token【{}】解密失败",request.getRequestURI(),token);
        	return result;
        }
    	String userId = jwt.get("userId");
    	String username = jwt.get("username");
    	String xzqdm = jwt.get("xzqdm");
        if (!JWTUtil.verifyToken(username, userId, token)) {
        	result.put("state", INVALID);//invalid 无效|valid 有效
        	logger.info("本地token认证，请求路径【{}】,token【{}】验证失败",request.getRequestURI(),token);
        }else{
        	result.put("state", VALID);//invalid 无效|valid 有效
        }
        result.put("username", username);
        result.put("userid", userId);
        result.put("xzqdm", xzqdm);
        result.put(Constants.ADMIN_FLAG, jwt.get(Constants.ADMIN_FLAG));
    	return result;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {}

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {}

}
