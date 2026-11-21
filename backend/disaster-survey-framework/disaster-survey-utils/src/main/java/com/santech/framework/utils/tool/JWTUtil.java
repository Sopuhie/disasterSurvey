package com.santech.framework.utils.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.santech.framework.global.ExecuteResult;



public class JWTUtil {
		private static Logger logger = LoggerFactory.getLogger(JWTUtil.class);
	
		public static final long EXPIRE_TIME = 10*60*60*1000;//token到期时间10小时
		//token秘钥,设置的复杂点这里用一串uuid，并用HMAC256加密的
		private static final String TOKEN_SECRET = "updcprojectsalt";
		
		/**
		 * 生成token
		 * @param userName
		 * @param userId
		 * @return
		 */
		public static String generatorToken(Map<String,String> userModel) {//QxUserModel
			String token = null;
			//过期时间
			Date date = new Date(System.currentTimeMillis()+EXPIRE_TIME );
			//秘钥及加密算法
			Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
			//设置头部信息
			Map<String,Object> header = new HashMap<String,Object>(2);
			header.put("type","JWT");
			header.put("alg","HS256");
			//附带用户信息，生成token
			try {
				token = JWT.create().
						withHeader(header)
						.withJWTId(UUID.randomUUID().toString())
						.withClaim("username", userModel.get("username"))//存放数据-用户编码
						.withClaim("userId", userModel.get("userId"))//存放数据-用户记录ID
						.withClaim("name", userModel.get("name"))//存放数据-用户昵称
						.withClaim("xzqdm", userModel.get("districtcode"))//存放数据-用户所属行政区编码
						.withClaim("adminFlag", userModel.get("adminFlag"))//存放数据-管理员标识
						.withExpiresAt(date)//过期时间
						.sign(algorithm);
			} catch (JWTCreationException je) {
				logger.error("生成token失败，报JWTCreationException异常，详情：",je);
				return null;
			}catch (Exception e) {
				logger.error("生成token失败，详情：",e);
				return null;
			}
			return token;
		}
		/**
		 * 验证token
		 * @param username
		 * @param userId
		 * @param token
		 * @return
		 */
		public static Boolean verifyToken(String username,String userId,String token) {
	     try {
			   JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET))
        		.withClaim("username",username)
				.withClaim("userId",userId).build();
			   DecodedJWT jwt = jwtVerifier.verify(token);
			   logger.info("认证通过：username:【{}】,过期时间:【{}】",jwt.getClaim("username"),jwt.getExpiresAt());
			} catch (JWTVerificationException e) {
				//如果令牌有一个无效的签名，或者没有满足Claim要求，那么将会抛出JWTVerificationException异常
				//当验证一个令牌时，时间验证会自动发生,在值无效时亦会抛出一个JWTVerificationException
				logger.error("验证token失败，报JWTVerificationException异常，详情：",e);
				return false;
			} catch (IllegalArgumentException  e){
				logger.error("验证token失败，报IllegalArgumentException异常，详情：",e);
				return false;
			} catch(Exception e) {
				logger.error("验证token失败，详情：",e);
				return false;
			}
	        return true;
		}
		
		
		public static Map<String,Object> getTokenMeta(String token) {
			Map<String,Object> dataMap = new HashMap<String,Object>();
			try {
				   JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET)).build();
				   DecodedJWT jwt = jwtVerifier.verify(token);
				   Date expireDate = jwt.getExpiresAt();
				   Date nowDate = new Date();
				   long expire = Math.max(expireDate.getTime()-nowDate.getTime(), 0);
				   dataMap.put("tokenId", jwt.getId());
				   dataMap.put("expire", expire/1000);//折算为秒
				} catch (JWTVerificationException e) {
					//如果令牌有一个无效的签名，或者没有满足Claim要求，那么将会抛出JWTVerificationException异常
					//当验证一个令牌时，时间验证会自动发生,在值无效时亦会抛出一个JWTVerificationException
					logger.error("验证token失败，报JWTVerificationException异常，详情：",e);
				} catch (IllegalArgumentException  e){
					logger.error("验证token失败，报IllegalArgumentException异常，详情：",e);
				} catch(Exception e) {
					logger.error("验证token失败，详情：",e);
				}
			return dataMap;
		}
		
		
		/**
		 * 解密token
		 * @return
		 */
		public static Map<String,String> getDecodedToken(String token) {
			try {
				Map<String,String> tokenParams = new HashMap<String,String>();
			    DecodedJWT jwt = JWT.decode(token);
			    String userId = jwt.getClaim("userId").asString();//用户记录ID
		    	String username = jwt.getClaim("username").asString();//用户名
		    	String name = jwt.getClaim("name").asString();//用户昵称
		    	String xzqdm = jwt.getClaim("xzqdm").asString();//用户所属行政区编码
		    	String adminFlag = jwt.getClaim("adminFlag").asString();//管理员标识
		    	tokenParams.put("userId", userId);
		    	tokenParams.put("username", username);
		    	tokenParams.put("name", name);
		    	tokenParams.put("xzqdm", xzqdm);
		    	tokenParams.put("adminFlag", adminFlag);
			    return tokenParams;
			} catch (JWTDecodeException exception){
				//如果令牌有无效的语法，或者消息头或有效负载不是JSONs，那么将会抛出JWTDecodeException异常。
				logger.error("解密token失败,报JWTDecodeException异常，详情：",exception);
			}catch(Exception e) {
				logger.error("解密token失败，详情：",e);
			}
			return null;
		}
		
		
		
		/**
		 * 验证单点登录token  预留
		 * @param username
		 * @param userId
		 * @param token
		 * @return
		 */
		public static ExecuteResult<JSONObject> checkSingleSignonToken(String token,String checkTokenUrl) {
	    	 //调用认证接口
	    	 Map<String,String> map = new HashMap<String,String>();
	    	 map.put("token", token);
	    	 try {
	    		//解析返回的数据
	 	 		String result = HttpUtil.httpGet(checkTokenUrl,JSON.toJSONString(map));
	 	 		logger.info("token认证接口，token【{}】认证结果【{}】",token,result);
	 	 		if (StringUtil.isNotEmpty(result)){
	 	 			JSONObject jsonRet = JSON.parseObject(result);
	 	 			//200 表示成功,400 表示失败,401 表示未认证
	 	 			if ("200".equals(jsonRet.getString("code"))){
	 	 				/*data:{
	 					 "state":状态（invalid 无效|valid 有效），
	 					 "username":用户名，
	 					 "userid":用户标识码，
	 					 "phone":手机号，
	 					 "redirect":重定向地址（未认证时重定向）
	 					}*/
	 	 				JSONObject data = jsonRet.getJSONObject("data");
	 	 				return ExecuteResult.success("验证成功", data);
	 	 			}else {
	 	 				/*"data": {
	 	 				"error": "验证失败:token无效！",
	 	 				"redirect": "http://ip:port/portal/index.html#/user/login",
	 	 				"state": "INVALID"
	 	 				}*/
	 	 				JSONObject data = jsonRet.getJSONObject("data");
	 	 				return ExecuteResult.error("用户认证未通过", data);
	 	 			}
	 	 		}else {
	 	 			throw new Exception("token认证接口请求异常");
	 	 		}
			} catch (Exception e) {
				logger.error("token认证接口，认证出现异常，详情:",e);
				JSONObject data = new JSONObject();
				data.put("state", "invalid");
				data.put("error", "调用验签接口异常");
				data.put("redirect", "");
				return ExecuteResult.error("用户认证异常", data);
			}
		}	
		
}
