package com.santech.framework.utils.tool;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * @Title: UuidUtils.java 
 * @Description：自定义ID公共方法工具API
 * @Version： V1.0
 * @Modify：   
 */
public class UuidUtils {
	
	/**
	 * 生成时分秒毫秒3位随机数ID
	 * @param fix 前缀,如上传文件表以UF开头
	 * @return
	 */
	public static String buildID(String fix) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return fix + format.format(new Date()) + (String.valueOf(((int) ((1 + Math.random()) * 1000)))).substring(1, 4);
	}
    
    
}
