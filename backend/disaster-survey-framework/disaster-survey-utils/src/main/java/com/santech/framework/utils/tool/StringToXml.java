package com.santech.framework.utils.tool;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class StringToXml {

    public static String stringtoxml(String text){
        String s = null;
        try {
            Document document = DocumentHelper.parseText(text);
            OutputFormat format = OutputFormat.createPrettyPrint();
            StringWriter writer = new StringWriter();
            // 格式化输出流
            XMLWriter xmlWriter = new XMLWriter(writer, format);
            // 将document写入到输出流
            xmlWriter.write(document);
            xmlWriter.close();
            s = writer.toString();
            s = s.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
        } catch (DocumentException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return s;
    }
}
