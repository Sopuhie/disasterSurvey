package com.santech.framework.utils.tool;

import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;

public class BigDecimalUtil {

    /***
      * @Description: 与 0 相等，返回true
      * @param bigDecimal
      * @return: boolean
      **/
    public static boolean compareToZero(BigDecimal bigDecimal){
        boolean b = false;
        BigDecimal zero = new BigDecimal("0");
        if(bigDecimal.compareTo(zero)==0){
            b = true;
        }
        return b;
    }

    /**
      * @Description: a比b大，返回true
      * @param a
      * @param b
      * @return: boolean
      **/
    public static boolean aCompareToB(BigDecimal a, BigDecimal b){
        boolean flag = false;
        if(a.compareTo(b)==1){
            flag = true;
        }
        return flag;
    }

    /**
      * @Description: a比b大，返回高于，小则低于，等于则等于
      * @param a
      * @param b
      * @return: java.lang.String
      **/
    public static String getUpOrDown(String a, String b){
        String result = "";
        if(StrUtil.isNotEmpty(a) && StrUtil.isNotEmpty(b)){
            BigDecimal bigDecimalA = new BigDecimal(a);
            BigDecimal bigDecimalB = new BigDecimal(b);
            if(bigDecimalA.compareTo(bigDecimalB)==1){
                result = "高于";
            }else if(bigDecimalA.compareTo(bigDecimalB)==-1){
                result = "低于";
            }else if(bigDecimalA.compareTo(bigDecimalB)==0){
                result = "等于";
            }
        }
        return result;
    }

}
