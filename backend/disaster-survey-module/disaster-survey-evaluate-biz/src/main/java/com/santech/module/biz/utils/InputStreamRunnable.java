package com.santech.module.biz.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jar包运行读取信息
 */
public class InputStreamRunnable extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(InputStreamRunnable.class);
    BufferedReader bReader = null;
    String type = null;
    public InputStreamRunnable(InputStream is, String _type) {
        try {
            bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "GBK"));
            type = _type;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void run() {
        String line;
        int lineNum = 0;
        StringBuilder errorInfo = new StringBuilder("");
        try {
            while ((line = bReader.readLine()) != null) {
                lineNum++;
                errorInfo.append(line);
                //System.out.println(type+":"+line);
            }
            if("Error".equalsIgnoreCase(type)) {
            	logger.error("脚本运行结果错误信息：【{}】",errorInfo.toString());
            }
            bReader.close();
        } catch (Exception ex) {
        	logger.error("解析脚本运行结果异常，详情",ex);
        }
    }
}
