package com.santech.framework.utils.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Description: 获取配置文件的参数,热更新
 **/
@Component
public class PropertiesUtil {

    private static Environment env;

    @Autowired
    public void set(Environment env) throws IOException {
        PropertiesUtil.env = env;
    }

    /**
     * key不存在则返回null
     * @param key
     * @return
     */
    public static String getString(String key) {
        try {
            return env.getProperty(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /**
     * key不存在则返回默认值
     *
     * @param key
     * @return
     */
    public static String getString(String key, String defaultValue) {
        try {
            String value = env.getProperty(key, defaultValue);
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return value;
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        return Integer.parseInt(env.getProperty(key));
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(String key, int defaultValue) {
        String value = env.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @return
     */
    public static boolean getBoolean(String key) {
        String property = env.getProperty(key);
        return new Boolean(property);
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = env.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return new Boolean(value);
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @param  regex 分割符
     * @return
     */
    public static String[] getArray(String key, String regex) {
        String value = env.getProperty(key);
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.split(regex);
    }


    /**
     * 根据key获取值
     *
     * @param key
     * @param  regex 分割符
     * @return
     */
    public static List<String> getList(String key, String regex) {
        String value = env.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        return Arrays.asList(value.split(regex));
    }
}
