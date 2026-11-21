package com.santech.framework.utils.tool;

import static com.santech.framework.global.Constants.CURRENT_PAGE;
import static com.santech.framework.global.Constants.PAGE_SIZE;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公共工具类
 */
public class SqlUtil {

    public static final Logger logger = LoggerFactory.getLogger(SqlUtil.class);


    /**
     *  当前页
     * @param current
     * @return
     */
    public static Integer getCurrentPage(Integer current){
        return current != null ? current : CURRENT_PAGE;
    }

    /**
     * 每页显示条数
     * @param size
     * @return
     */
    public static Integer getPageSize(Integer size){
        return size != null ? size : PAGE_SIZE;
    }


    /**
     * 拼接IN条件值 in ('xx','xx')
     * @param str
     * @return
     */
    public static String getInSql(String field, String str, Map<String, Object> paramsMap) {
        String[] strArr = str.split(",");
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < strArr.length; i++){
            if(i == strArr.length-1){
                sb.append(":").append(field+i);
            }else{
            	sb.append(":").append(field+i).append(",");
            }
            paramsMap.put(field+i, strArr[i]);
        }
        return sb.toString();
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
     * 拼接in  sql ，即 id in ( 1 , 2 ...)
     * @param id
     * @param idValue
     * @return
     */
    public static  Map<String, Object> getInSql2(String id,String idValue){
        Map<String, Object> paramsMap = new HashMap<>();
        String[] arr = idValue.split(",");
        String querySql = " " + id + " in ( ";
        for(int i=0;i<arr.length;i++){
            paramsMap.put(id+i, arr[i]);
            querySql += ":"+(id+i) + " , ";
        }
        if(arr.length>0){
            querySql = querySql.substring(0,querySql.length()-3);
        }
        querySql += " ) ";
        Map<String, Object>  resultmap = new HashMap<>();
        resultmap.put("sql",querySql);
        resultmap.put("paramsMap",paramsMap);
        return resultmap;
    }


    /***
      * @Description: 获取表列名sql
      * @param tableName
      * @return: java.lang.String
      **/
    public static String getTableColumnName(String tableName) {
    	//oracle语法
        //return "SELECT column_name FROM user_tab_columns where table_name = upper('" + tableName + "') order by column_name";
    	//pg语法
    	StringBuilder buffer = new StringBuilder("");
    	buffer.append("SELECT A.attname AS column_name FROM pg_catalog.pg_attribute A ")
    	.append(" WHERE A.attnum > 0 ")
    	.append(" AND NOT A.attisdropped ")
    	.append(" AND A.attrelid = ")
    	.append(" (SELECT C.oid FROM pg_catalog.pg_class C ")
    	.append(" LEFT JOIN pg_catalog.pg_namespace n ON n.oid = C.relnamespace ")
    	.append(" WHERE C.relname = '"+tableName.toLowerCase()+"' ")
    	.append(" AND pg_catalog.pg_table_is_visible ( C.oid )) ");
    	return buffer.toString();
    }

    /**
     * 判断一个bean是否包含某个属性,忽略大小写
     */
    public static Boolean hasField(Class c,String fieldName){
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
