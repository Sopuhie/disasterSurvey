package com.santech.framework.global;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

    // 页面传参
    public static final Integer PAGE_SIZE = 10;
    public static final Integer CURRENT_PAGE = 1;
    public static final String PREFIX_PARAM = "search_";

    /**
     *  头部请求参数
     */
    public static final String HEADER_USERID = "loginUserId";
    public static final String HEADER_USERNAME = "loginUser";
    public static final String ADMIN_FLAG = "adminFlag";

    /**
     * 日志操作类型
     */
    public static final String LOG_TYPE_ADD = "新增";
    public static final String LOG_TYPE_UPDATE = "修改";
    public static final String LOG_TYPE_DELETE = "删除";
    public static final String LOG_TYPE_LOGIN = "登录";
    public static final String LOG_TYPE_DOWNLOAD = "下载";
    public static final String LOG_TYPE_EXPORT = "导出";
    public static final String LOG_TYPE_PUBLISH = "发布";
    public static final String LOG_TYPE_FEEDBACK = "反馈";
    public static final String LOG_TYPE_TO_BE_DETERMINED = "待定";
    
    /**
     * 日志操作状态
     */
    public static final String LOG_SUCCESS_STATUS = "成功";
    public static final String LOG_ABNORMAL_STATUS = "异常";
    
    /**
     * 行政区划数据缓存进redis
     */
    public static final String REGION_DATA = "REGION:REGION_DATA";
    
    /**
     * 字符集
     */
    public static final String UTF8 = "UTF-8";
    public static final String GBK = "GBK";

}
