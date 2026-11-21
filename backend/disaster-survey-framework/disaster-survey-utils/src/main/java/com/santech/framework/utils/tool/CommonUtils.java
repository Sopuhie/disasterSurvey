package com.santech.framework.utils.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.StringUtils;

import com.santech.framework.utils.tool.StringUtil;
/**
 * @Title: CommonUtils.java 
 * @Description：公共方法工具API
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
public class CommonUtils {
	
    
    /**
     * bean转map
     * @param bean
     * @return
     */
    public static <T> Map<String, ?> beanToMap(T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        Map<String, Object> map = new HashMap<>();
        beanMap.forEach((key, value) -> {
            map.put(String.valueOf(key), value);
        });
        return map;
    }
    /**
     * map转bean
     * @param map
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T mapToBean(Map<String, ?> map, Class<T> clazz)
            throws IllegalAccessException, InstantiationException {
        T bean = clazz.newInstance();
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }
    /**
     * bean集合转map集合
     * @param objList
     * @return
     */
    public static <T> List<Map<String, ?>> objectsToMaps(List<T> objList) {
        List<Map<String, ?>> list = new ArrayList<>();
        if (objList != null && objList.size() > 0) {
            Map<String, ?> map = null;
            T bean = null;
            for (int i = 0, size = objList.size(); i < size; i++) {
                bean = objList.get(i);
                map = beanToMap(bean);
                list.add(map);
            }
        }
        return list;
    }
    /**
     * map集合转bean集合
     * @param maps
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz)
            throws InstantiationException, IllegalAccessException {
        List<T> list = new ArrayList<>();
        if (maps != null && maps.size() > 0) {
            Map<String, ?> map = null;
            for (int i = 0, size = maps.size(); i < size; i++) {
                map = maps.get(i);
                T bean = mapToBean(map, clazz);
                list.add(bean);
            }
        }
        return list;
    }
    
    
    /**
     * 拼接where语句中的条件sql ，公司封装的数据库链接
     * @param request
     * @param orderSql  排序sql
     * @param otherConditionSql  其他自定义的条件sql
     * @return
     */
	public static Map<String, Object> getWhereSearchSql(Map<String, Object> requestParams, String orderSql,
			String otherConditionSql) {
		StringBuilder sbSql = new StringBuilder();
		Map<String, Object> paramsMap = new HashMap<>();
		if (requestParams.size() > 0 || StringUtil.isNotEmpty(otherConditionSql)) {
			sbSql.append(" 1=1 ");
			if(requestParams.size() > 0) {
				for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (key.startsWith("like_") && !StringUtils.isEmpty(value)) {
						sbSql.append(" and " + key.substring(5) + " like :" + key.substring(5) + " ");
						paramsMap.put(key.substring(5), "%" + value + "%");
					}
					if (key.startsWith("eq_") && !StringUtils.isEmpty(value)) {
						sbSql.append(" and " + key.substring(3) + " = :" + key.substring(3) + " ");
						paramsMap.put(key.substring(3), value);
					}
					// 前端传数组数据
					if (key.startsWith("array_") && !StringUtils.isEmpty(value)) {
						sbSql.append(" and " + key.substring(6) + " in (" + getInSql(value.toString()) + ") ");
					}
				}
			}
			if (StringUtil.isNotEmpty(otherConditionSql)) {
				sbSql.append(otherConditionSql);
			}
		}
		if (!StringUtils.isEmpty(orderSql)) {
			sbSql.append(" order by " + orderSql);
		}
		Map<String, Object> resultmap = new HashMap<>();
		resultmap.put("sql", sbSql.toString());
		resultmap.put("paramsMap", paramsMap);
		return resultmap;
	}
	/**
     * 拼接where语句中的条件sql ，公司封装的数据库链接，带着主表别名前缀
     * @param request
     * @param orderSql  排序sql
     * @param otherConditionSql  其他自定义的条件sql
     * @param tableAlisa  主表默认前缀
     * @return
     */
	public static Map<String, Object> getWhereSearchSqlWithTableAlisa(Map<String, Object> requestParams, String orderSql,
			String otherConditionSql,String tableAlisa) {
		StringBuilder sbSql = new StringBuilder();
		Map<String, Object> paramsMap = new HashMap<>();
		if (requestParams.size() > 0 || StringUtil.isNotEmpty(otherConditionSql)) {
			sbSql.append(" 1=1 ");
			for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (key.startsWith("like_") && !StringUtils.isEmpty(value)) {
					sbSql.append(" and "+tableAlisa+"." + key.substring(5) + " like :" + key.substring(5) + " ");
					paramsMap.put(key.substring(5), "%" + value + "%");
				}
				if (key.startsWith("eq_") && !StringUtils.isEmpty(value)) {
					sbSql.append(" and "+tableAlisa+"." + key.substring(3) + " = :" + key.substring(3) + " ");
					paramsMap.put(key.substring(3), value);
				}
				// 前端传数组数据
				if (key.startsWith("array_") && !StringUtils.isEmpty(value)) {
					sbSql.append(" and "+tableAlisa+"." + key.substring(6) + " in (" + getInSql(value.toString()) + ") ");
				}
			}
			if (StringUtil.isNotEmpty(otherConditionSql)) {
				sbSql.append(otherConditionSql);
			}
		}
		if (!StringUtils.isEmpty(orderSql)) {
			sbSql.append(" order by " + orderSql);
		}
		Map<String, Object> resultmap = new HashMap<>();
		resultmap.put("sql", sbSql.toString());
		resultmap.put("paramsMap", paramsMap);
		return resultmap;
	}
    /**
     * 获取指定JSON字段里的字段作为查询条件的sql语句
     * @param targetJSONColumn
     * @param requestParams
     * @return
     */
    public static Map<String, Object> getJSONQuerySql(String targetJSONColumn, Map<String,Object> requestParams) {
    	String sql = "";
         Map<String, Object> paramsMap = new HashMap<String, Object>();
         if(requestParams.size()>0){
             for (String key : requestParams.keySet()) {
                 if(key.startsWith(targetJSONColumn+"_like_") && !StringUtils.isEmpty(requestParams.get(key)) ){
                     sql += " and "+targetJSONColumn+"::json->>'"+key.substring(targetJSONColumn.length()+6)+"' like :" + key.substring(targetJSONColumn.length()+6) + " ";
                     paramsMap.put(key.substring(targetJSONColumn.length()+6), "%"+requestParams.get(key)+"%");
                 }
                 if(key.startsWith(targetJSONColumn+"_eq_") && !StringUtils.isEmpty(requestParams.get(key)) ){
                     sql += " and "+targetJSONColumn+"::json->>'"+key.substring(targetJSONColumn.length()+4)+"' = :" + key.substring(targetJSONColumn.length()+4) + " ";
                     paramsMap.put(key.substring(targetJSONColumn.length()+4), requestParams.get(key));
                 }
                 // 前端传数组数据
                 if (key.startsWith(targetJSONColumn+"_array_") && !StringUtils.isEmpty(requestParams.get(key)) ) {
                     sql += " and "+targetJSONColumn+"::json->>'"+key.substring(targetJSONColumn.length()+7)+"' in ("+ getInSql(requestParams.get(key).toString()) + ") ";
                 }
             }
         }
         Map<String, Object>  resultmap = new HashMap<>();
         resultmap.put("sql",sql);
         resultmap.put("paramsMap",paramsMap);
         return resultmap;
    }
    /**
     * 拼接IN条件值 in ('xx','xx')
     * @param str
     * @return
     */
    public static String getInSql(String str) {
        String[] strArr = str.split(",");
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < strArr.length; i++){
            if(i == strArr.length-1){
                sb.append("'").append(strArr[i]).append("'");
            }else{
                sb.append("'").append(strArr[i]).append("'").append(",");
            }
        }
        return sb.toString();
    }

    
    /**
     * 获取指定时间格式的时间字符串
     * @param date
     * @param format
     * @return
     */
    public static String  getFormatDateString(Date date ,String format) {
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	return sdf.format(date);
    }
    /**
     * 获取指定时间格式的时间字符串
     * @param date
     * @param format
     * @return
     */
    public static Date getDateByString(String dateStr ,String format) {
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	Date date = null;
    	try {
    		if(StringUtil.isNotEmpty(dateStr)) {
    			date =  sdf.parse(dateStr);
    		}
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return date;
    }
    
    public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}
}
