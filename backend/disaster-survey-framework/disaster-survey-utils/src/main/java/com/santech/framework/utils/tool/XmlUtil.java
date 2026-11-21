package com.santech.framework.utils.tool;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
  * @Description: xml工具类
  **/
@Slf4j
public class XmlUtil {

    /**
      * @Description: 获取xml字符串 根节点
      * @param xmlStr
      * @return: org.dom4j.Element
      **/
    public static Element getRootElement(String xmlStr) {
        //1.创建Reader对象
        SAXReader reader = new SAXReader();
        //2.加载xml
        Element element = null;
        try {
            Document document = reader.read(new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8)));
            element = document.getRootElement();
        } catch (DocumentException e) {
            log.error(e.getMessage(), e);
        }
        //3.获取根节点
        return element;
    }
    
    /**
     * 配置字符串是否已经进行了转义
     * @param config
     * @param outPath
     * @param xmlName
     * @param configIsEscape
     * @throws Exception
     */
    public static void createXml(String config,String outPath, String xmlName, boolean configIsEscape) {
        try {
            Document document = null;
            document = DocumentHelper.parseText(config);
            // 格式化输出格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");

            String xmlPath = outPath + File.separator + xmlName;
            File file = new File(xmlPath);
            Paths.get(outPath).toFile().mkdirs();
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            // 设置是否转义，默认使用转义字符
            writer.setEscapeText(configIsEscape);
            writer.write(document);
            writer.close();
            log.info("生成xml成功");
        }catch (Exception e) {
            e.printStackTrace();
            log.error("生成xml失败");
            throw new RuntimeException(e);
        }
    }
    
    public static void createXml(Map<String,String> maps,String outPath, String xmlName){
        try {
            File file1 = new File(outPath);
            if (!file1.exists()){
                file1.mkdirs();
            }
            // 1、创建document对象
            Document document = DocumentHelper.createDocument();
            // 2、创建根节点rss
            Element rss1 = document.addElement("class");
            // 3、向rss节点添加version属性
            Element rss = rss1.addElement("rss");
            rss.addAttribute("version", "2.0");
            // 4、生成子节点及子节点内容

            if (maps.size() > 0 ){
                for (String s : maps.keySet()){
                    Element s1 = rss.addElement(s);
                    s1.setText(maps.get(s));
                }
            }

            // 5、设置生成xml的格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置编码格式
            format.setEncoding("UTF-8");

            // 6、生成xml文件
            String xmlPath = outPath+ File.separator+xmlName;
            File file = new File(xmlPath);
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            // 设置是否转义，默认使用转义字符
            writer.setEscapeText(false);
            writer.write(document);
            writer.close();
            log.info("生成xml成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("生成xml失败");
        }
    }
}
