package com.santech.framework.utils.tool;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * requese工具类
 */
@Slf4j
public class RequestUtil {

    /**
     * 获取客户端请求参数的值
     *
     * @param searchName
     * @return
     */
    public static String getSearchNameValue(String searchName) {
        String result = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            for (String key : getAllRequestParam(request).keySet()) {
                if (key.equals(searchName)) {
                    result = getAllRequestParam(request).get(key);
                }
            }
        }
        return result;
    }

    /**
     * 获取客户端请求参数是否存在
     *
     * @param searchName
     * @return
     */
    public static boolean getSearchName(String searchName) {
        boolean result = false;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            for (String key : getAllRequestParamNull(request).keySet()) {
                if (key.equals(searchName)) {
                    result = true;
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * 获取客户端请求参数中所有的信息
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                // 如果字段的值为空，判断若值为空，则删除这个字段>
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }

    /**
     * 获取客户端请求参数中所有的信息(返回的是Map<String, Object>)
     *
     * @param request
     * @return
     */
    public static Map<String, Object> getAllRequestParamO(final HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = URLcoderUtil.encoder(request.getParameter(en));
                res.put(en, value);
                // 如果字段的值为空，判断若值为空，则删除这个字段>
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }

    /**
     * 获取客户端请求参数中所有的信息(允许空值)
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParamNull(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
            }
        }
        return res;
    }

    /***
     * @Description: 获取HttpServletRequest
     * @param
     * @return: javax.servlet.http.HttpServletRequest
     **/
    public static HttpServletRequest getHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attributes.getRequest();
        return req;
    }

    /***
     * @Description: 获取客户端请求参数中所有的信息，返回String
     * @return: java.lang.String
     **/
    public static String getAllRequestParamRS() {
        HttpServletRequest request = getHttpRequest();
        String result = "";
        String method = request.getMethod();

        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                if (!"_".equals(en)) {
                    String value = request.getParameter(en);
                    result += en + "=" + value + ",";
                }
            }
            if (StrUtil.isNotEmpty(result)) {
                result = result.substring(0, result.length() - 1);
            }
        }

        if ("POST".equalsIgnoreCase(method)) {
            if (StrUtil.isEmpty(result)) {
                result += readerAsStr(request);
            } else {
                if (StrUtil.isNotEmpty(readerAsStr(request))) {
                    result += "," + readerAsStr(request);
                }
            }
        }
        return result;
    }

    /**
     * 获取请求的头部信息
     **/
    public static Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> map = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }

    /**
     * @param name
     * @Description: 通过名称获取header
     * @return: java.lang.String
     **/
    public static String getHeaderByName(String name) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attributes.getRequest();
        return req.getHeader(name);
    }

    /***
     * @Description: 获取请求body
     * @param request
     * @return: java.lang.String
     **/
    public static String readerAsStr(HttpServletRequest request) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return sb.toString();
    }
}
