package com.santech.module.biz.utils;

public class PwdCheckUtil {

    public static String SPECIAL_CHAR = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";


    /**
      * Description: 检测密码中字符长度
      * @param password 密码字符串
      * @param minNum 最小长度
      * @param maxNum 最大长度
      * @return: boolean 符合长度要求 返回true
      **/
    public static boolean checkPasswordLength(String password, Integer minNum, Integer maxNum) {
        boolean flag = false;
        if (null == maxNum)  {
            minNum = null == minNum ? 0 : minNum;
            if (password.length() >= minNum) {
                flag = true;
            }
        } else {
            minNum = null == minNum ? 0 : minNum;
            if (password.length() >= minNum && password.length() <= maxNum) {
                flag = true;
            }
        }
        return flag;
    }

    /**
      * Description: 检测密码至少包含最少一个字母、一个数字、一个特殊符号
      * @param password 密码
      * @return: boolean
      **/
    public static boolean checkPasswordContains(String password, Integer minNum, Integer maxNum) {
        boolean b = checkPasswordLength(password, minNum, maxNum);
        boolean b1 = checkContainDigit(password);
        boolean b2 = checkContainCase(password);
        boolean b3 = checkContainSpecialChar(password);
        return b && b1 && b2 && b3;
    }

    /**
     * @Description: 检测密码复杂度，包含字母、数字、特殊符号中的至少两种
     * @param password 密码
     * @param minNum 最小长度
     * @param maxNum 最大长度
     * @return: boolean
     **/
    public static boolean checkPasswordComplex(String password, Integer minNum, Integer maxNum){
        boolean b = checkPasswordLength(password, minNum, maxNum);
        boolean b1 = checkContainDigit(password);
        boolean b2 = checkContainCase(password);
        boolean b3 = checkContainSpecialChar(password);
        return b && ((b1 && b2) || (b1 && b3) || (b2 && b3));
    }

    /**
      * Description: 检测密码中是否包含数字
      * @param password  密码
      * @return: boolean 包含数字 返回true
      **/
    public static boolean checkContainDigit(String password) {
        char[] chPass = password.toCharArray();
        boolean flag = false;
        int num_count = 0;

        for (int i = 0; i < chPass.length; i++) {
            if (Character.isDigit(chPass[i])) {
                num_count++;
            }
        }
        if (num_count >= 1){
            flag = true;
        }
        return flag;
    }

    /**
      * Description: 检测密码中是否包含字母（不区分大小写）
      * @param password  密码
      * @return: boolean
      **/
    public static boolean checkContainCase(String password) {
        char[] chPass = password.toCharArray();
        boolean flag = false;
        int char_count = 0;

        for (int i = 0; i < chPass.length; i++) {
            if (Character.isLetter(chPass[i])) {
                char_count++;
            }
        }
        if (char_count >= 1) {
            flag = true;
        }
        return flag;
    }

    /**
      * Description: 检测密码中是否包含小写字母
      * @param password 密码
      * @return: boolean
      **/
    public static boolean checkContainLowerCase(String password) {
        char[] chPass = password.toCharArray();
        boolean flag = false;
        int char_count = 0;

        for (int i = 0; i < chPass.length; i++) {
            if (Character.isLowerCase(chPass[i])) {
                char_count++;
            }
        }
        if (char_count >= 1) {
            flag = true;
        }
        return flag;
    }


    /**
      * Description: 检测密码中是否包含大写字母
      * @param password
      * @return: boolean
      **/
    public static boolean checkContainUpperCase(String password) {
        char[] chPass = password.toCharArray();
        boolean flag = false;
        int char_count = 0;

        for (int i = 0; i < chPass.length; i++) {
            if (Character.isUpperCase(chPass[i])) {
                char_count++;
            }
        }
        if (char_count >= 1) {
            flag = true;
        }
        return flag;
    }

    /**
      * Description: 检测密码中是否包含特殊符号
      * @param password 密码
      * @return: boolean
      **/
    public static boolean checkContainSpecialChar(String password) {
        char[] chPass = password.toCharArray();
        boolean flag = false;
        int special_count = 0;

        for (int i = 0; i < chPass.length; i++) {
            if (SPECIAL_CHAR.indexOf(chPass[i]) != -1) {
                special_count++;
            }
        }

        if (special_count >= 1){
            flag = true;
        }
        return flag;
    }

}
