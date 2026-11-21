package com.santech.framework.utils.component.encrypt.kindcrypto;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kind.crypto.crypto.CryptoService;
import com.kind.crypto.entity.CryptResult;
import com.kind.crypto.entity.SignResult;
import com.kind.crypto.sign.SignService;
import com.santech.framework.utils.tool.StringUtil;

import cn.hutool.core.codec.Base64;

public class KindCryptoUtils {
	private static Logger logger = LoggerFactory.getLogger(KindCryptoUtils.class);
	
	//keyIndex默认3
	public static String encryptSM4(String data,int keyIndex,boolean enable) {
		String encrypt = "";
		if(!enable) {
			return data;
		}
		try {
			if(keyIndex==0) {
				keyIndex = 3;
			}
			CryptResult cryptResult = new CryptoService().encryptSM4FixIndex(keyIndex, data.getBytes());
			if(cryptResult.getSuccess()){
				encrypt = new String(cryptResult.getData());
	        }else{
	        	logger.error("数据进行SM4加密失败，报错编码以及原因【{}】:",cryptResult.getMsg());
	        }
		} catch (Exception e) {
			logger.error("数据进行SM4加密异常，异常详情：",e);
		}
		return encrypt;
	}
	
	public static String decryptSM4(String encryptData,int keyIndex,boolean enable) {
		String decryptData = "";
		try {
			if(!enable) {
				return encryptData;
			}
			if(StringUtil.isEmpty(encryptData)) {
				return decryptData;
			}
			if(keyIndex==0) {
				keyIndex = 3;
			}
			byte[] encryptDataByte = encryptData.getBytes();
			CryptResult cryptResult = new CryptoService().decryptSM4FixIndex(keyIndex, encryptDataByte);
			if(cryptResult.getSuccess()){
				decryptData = new String(cryptResult.getData());
	        }else{
	        	logger.error("数据进行SM4解密失败，报错编码以及原因【{}】:",cryptResult.getMsg());
	        }
		} catch (Exception e) {
			logger.error("数据进行SM4解密异常，异常详情：",e);
		}
		return decryptData;
	}
	
	public static Map<String,String> signData(String appName,String data,boolean enable) {
		String sign = "";
		String pubKeyCert = "";
		Map<String,String> result = new HashMap<String,String>();
		try {
			if(!enable) {
				result.put("sign", "");
				result.put("pubKeyCert", "");
				return result;
			}
			// 验签
            SignResult signResult = new SignService().signData(appName, data);

            if(signResult.getSuccess()){
            	sign = Base64.encode(signResult.getSign());//签名
            	pubKeyCert = Base64.encode(signResult.getPubKeyCert());//签名证书
            }else{
                logger.error("数据进行签名失败，报错编码以及原因【{}】:",signResult.getMsg());
            }
		} catch (Exception e) {
			logger.error("数据进行签名异常，异常详情：",e);
		}
		result.put("sign", sign);
		result.put("pubKeyCert", pubKeyCert);
		return result;
	}
	public static boolean verifySignData(String appName,String data,String sign,String pubKeyCert,boolean enable) {
		boolean result = true;//默认未篡改
		try {
			if(!enable) {
				return result;
			}
			// 验签
			 SignResult signResult = new SignService().verifySignedData(data, sign,
					 pubKeyCert,appName);
			 result = signResult.getSuccess();
		} catch (Exception e) {
			logger.error("数据进行签名验签异常，异常详情：",e);
		}
		return result;
	}

//    public static void main(String[] args) {
//    	 String data = "1991/02/24";
//    	 Map<String,String> result = signData("web",data);
//    	 System.out.println(result.get("sign")) ;
//    	 System.out.println(result.get("pubKeyCert")) ;
    	 String data1 = "1991/02/25";
//    	 Map<String,String> result1 = signData("web",data);
//    	 System.out.println(result1.get("sign")) ;
//    	 System.out.println(result1.get("pubKeyCert")) ;
//    	 
//    	  
//    	 System.out.println(verifySignData("web",data1,result.get("sign"),result.get("pubKeyCert"))) ;
//    }





}