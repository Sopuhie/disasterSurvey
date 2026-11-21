package com.santech.framework.utils.tool;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class UrlXmlUtils {
    public static Document getDocument(String xmlUrl){
        Document doc = DocumentHelper.createDocument();
        try{
        URL url = new URL(xmlUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        InputStream stream = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuffer document = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            document.append(line+"\r\n");
        }
        doc = DocumentHelper.parseText(document.toString());
        conn.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return doc;
    }
    public static HashMap<String, String> getDocument(InputStream inputStream){
        //创建xml文档读取dom4j的对象
        SAXReader reader = new SAXReader();
        HashMap<String, String> map = new HashMap<>();
        Document doc = null;
        try {
            doc = reader.read(inputStream);
            Element rootElement = doc.getRootElement();
            List<Element> flows = rootElement.elements("add");
            map = new HashMap<>();
            for (Element flow : flows) {
                String id = flow.attributeValue("id");
                String name = flow.attributeValue("name");
                map.put(id,name);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return map;
    }
}
