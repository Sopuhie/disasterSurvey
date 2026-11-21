package com.santech.framework.utils.tool;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 编码工具类
 */
@Slf4j
public class URLcoderUtil {

    /**
     * 编码
     * @param content
     * @return
     */
    public static String encoder(String content){
        String result = "";
        if (StrUtil.isNotEmpty(content)) {
            try {
                result = URLEncoder.encode(content,"utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 解码
     * @param content
     * @return
     */
    public static String decode(String content){
        String result = "";
        if (StrUtil.isNotEmpty(content)) {
            try {
                result = URLDecoder.decode(content, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

}
