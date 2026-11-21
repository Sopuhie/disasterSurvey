package com.santech.module.biz.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Title: FileHandlerUtils.java 
 * @Description：txt文件操作工具类
 * @Version： V1.0
 * @Modify：   
 */
public class TxtFileHandlerUtils{
	
	private static final Logger logger = LoggerFactory.getLogger(TxtFileHandlerUtils.class);
	
    public static List<String> parseBaseInfoTXTContent(File txtFile){
	    logger.info("按行解析txt文件内容， TargetPath: {}", txtFile.getAbsolutePath());
	    List<String> result = new ArrayList<String>();
		InputStream input = null;
		InputStreamReader read = null;
		BufferedReader bufferedReader = null;
	    try {
	    	input = new FileInputStream(txtFile);
	        if(input!=null) {
	        	 read = new InputStreamReader(input);
	             bufferedReader = new BufferedReader(read);
	             String lineTxt = null;
	             while((lineTxt = bufferedReader.readLine()) != null){
	            	 result.add(lineTxt.trim().replaceAll("\uFEFF",""));
	             }
	        }
	    } catch (Exception e) {
	    	logger.error("按行解析txt文件内容【{}】出现异常，详情：", txtFile.getAbsolutePath(), e);
	    	result.clear();//如果出现异常，则清空已存的数据
	    } finally {
	    	try {
	    		if(bufferedReader!=null) {
		    		bufferedReader.close();
		    	}
	    		if(read!=null) {
		    		read.close();
		    	}
		    	if(input!=null) {
		    		input.close();
		    	}
		    	
		    	
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return result;
    }
    
    public static String parseBaseInfoTXTContentToStringArray(File txtFile,int lineNumber){
	    //logger.info("读取txt文件指定行【{}】内容， TargetPath: {}", lineNumber, txtFile.getAbsolutePath());
		String lineTxt = "";
	    InputStream input = null;
	    InputStreamReader read = null;
		LineNumberReader reader = null;
		Stream<String> stream = null;
	    try {
	        input = new FileInputStream(txtFile);
	        if(input!=null) {
	        	 read = new InputStreamReader(input);
	             reader = new LineNumberReader(read);
	             stream = Files.lines(Paths.get(txtFile.getAbsolutePath()));
	             long totalLine = stream.count();
	             if(lineNumber < 0 || lineNumber > totalLine){
	            	 throw new RuntimeException("指定行【"+lineNumber+"】不在文件"+txtFile.getName()+"行数范围内");
	             }
	             
	             int line = 1;
	             reader.setLineNumber(lineNumber);
	             int i = reader.getLineNumber();
	             
	             while((lineTxt = reader.readLine()) != null){
		             if(i == line){
		            	lineTxt = lineTxt.trim().replaceAll("\uFEFF","");
		            	break;
		             }
		             line++;
	             }
	        }
	    } catch (Exception e) {
	    	logger.error("读取txt文件指定行【{}】内容并转成字符串数组【{}】出现异常，详情：", lineNumber, txtFile.getAbsolutePath(), e);
	    } finally {
	    	try {
	    		if(stream!=null) {
	    			stream.close();
	    		}
	    		if(reader!=null) {
		    		reader.close();
		    	}
	    		if(read!=null) {
		    		read.close();
		    	}
	    		if(input!=null) {
		    		input.close();
		    	}
		    	
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return lineTxt;
    }
    
    public static  int getTxtTotalLineNum(String txtAbsFilePath){
		int totalLine = 0;
		Stream<String> stream = null;
		try {
			stream = Files.lines(Paths.get(txtAbsFilePath));
			totalLine =  (int) stream.count();
			logger.info("读取txt文件总行数为【{}】", totalLine);
	    } catch (Exception e) {
	    	logger.error("读取txt文件总行数【{}】出现异常，详情：",txtAbsFilePath, e);
	    } finally {
	    	if(stream!=null) {
    			stream.close();
    		}
	    }
	    return totalLine;
    }
    public static   void writeInfoTotxtByLine(String path, String contentLine,boolean appendflag) {
        FileOutputStream  fileOut = null;
        OutputStreamWriter writer = null;
        BufferedWriter bw = null;
        try {
        	//logger.info("按行写入txt文件内容， TargetPath: {}", path);
            //将写入转化为流的形式
        	File file = new File(path);
        	//路径目录不存在时创建目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        	if(file.exists()&&!appendflag){
        		//如果文件存在，且不再该文件追加，则先删除文件
                 file.delete();
            }
            fileOut = new FileOutputStream(file, true);
            writer = new OutputStreamWriter(fileOut,"UTF-8");
            bw = new BufferedWriter(writer);
            //一次写一行
            bw.write(contentLine);
            bw.newLine();  //换行用
            bw.flush();
        } catch (Exception e) {
        	logger.error("按行写入txt文件内容【{}】出现异常，详情：", path, e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (writer != null) {
                	writer.close();
                }
                if (fileOut != null) {
                	fileOut.close();
                }
                //写入完毕后将数据置空
                contentLine = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
}
