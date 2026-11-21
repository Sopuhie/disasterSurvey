package com.santech.module.biz.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;


public class SignUtil {
		private static Logger logger = LoggerFactory.getLogger(SignUtil.class);
	
		
		/**
		 * @Title getAuthenticationSign
		 * @Description (此工具方法用于根据传入的盐及有效期，生成自定义的认证签名，采用JWT方式生成，加密算法HS256)
		 * @param salt(签名标识，加密的盐)
		 * @param expireTime(过期时间或有效时间，以生成签名的时间点计时，单位毫秒)
		 * @return (签名字符串)
		 */
		public static String getAuthenticationSign(String salt,long expireTime) {
			String sign = null;
			//过期时间
			Date date = new Date(System.currentTimeMillis()+expireTime );
			//秘钥及加密算法
			Algorithm algorithm = Algorithm.HMAC256(salt);
			//设置头部信息
			Map<String,Object> header = new HashMap<String,Object>(2);
			header.put("type","JWT");
			header.put("alg","HS256");
			//附带用户信息，生成token
			try {
				sign = JWT.create().
						withHeader(header)
						//.withClaim("username", userModel.getUsername())//存放数据-待定
						.withExpiresAt(date)//过期时间
						.sign(algorithm);
			} catch (JWTCreationException je) {
				logger.error("生成签名失败，报JWTCreationException异常，详情：",je);
				return null;
			}catch (Exception e) {
				logger.error("生成签名失败，详情：",e);
				return null;
			}
			return sign;
		}
		
		/**
		 * @Title verifySign
		 * @Description (此工具方法用于根据传入的盐及签名，来验证签名的有效性，是否符合本系统的约定)
		 * @param salt(签名标识，用于解密认证的盐)
		 * @param sign(签名)
		 * @return (签名字符串)
		 */
		public static Boolean verifySign(String salt,String sign) {
	     try {
			   JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(salt))
        		//.withClaim("username",username)
				.build();
			   DecodedJWT jwt = jwtVerifier.verify(sign);
			   logger.info("签名验证通过：过期时间：【{}】",jwt.getExpiresAt());
			} catch (JWTVerificationException e) {
				//如果令牌有一个无效的签名，或者没有满足Claim要求，那么将会抛出JWTVerificationException异常
				//当验证一个令牌时，时间验证会自动发生,在值无效时亦会抛出一个JWTVerificationException
				logger.error("验证签名失败，报JWTVerificationException异常，详情：",e);
				return false;
			} catch (IllegalArgumentException  e){
				logger.error("验证签名失败，报IllegalArgumentException异常，详情：",e);
				return false;
			} catch(Exception e) {
				logger.error("验证签名失败，详情：",e);
				return false;
			}
	        return true;
		}
		
		
}
