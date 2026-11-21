package com.santech.framework.utils.tool;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	/* 常量和singleton。 */

	/**
	 * 空字符串。
	 */
	public static final String EMPTY_STRING = "";

	private static final int PAD_LIMIT = 8192;

	/**
	 * 如果字符串是null，则返回空字符串""，否则返回字符串本身。
	 */
	public static String defaultIfNull(String str) {
		return (str == null) ? EMPTY_STRING : str;
	}

	public static String defaultIfNull(String str, String defaultStr) {
		return (str == null) ? defaultStr : str;
	}

	public static boolean isBlank(String str) {
		int length;
		if (str != null && (length = str.length()) != 0) {
			for (int i = 0; i < length; ++i) {
				if (!Character.isWhitespace(str.charAt(i))) {
					return false;
				}
			}

			return true;
		} else {
			return true;
		}
	}

	public static boolean isNotBlank(String str) {
		int length;
		if (str != null && (length = str.length()) != 0) {
			for (int i = 0; i < length; ++i) {
				if (!Character.isWhitespace(str.charAt(i))) {
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}

	/***
	 * 除去字符串头尾部的空白
	 */
	public static String trim(String str) {
		return trim(str, null, 0);
	}

	public static String[] trimArray(String[] array) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] != null) {
					array[i] = trim(array[i]);
				}
			}
		}
		return array;
	}

	/***
	 * 除去字符串头尾部的指定字符
	 */
	public static String trim(String str, String stripChars) {
		return trim(str, stripChars, 0);
	}

	/**
	 * 除去字符串头部的空白
	 */
	public static String trimStart(String str) {
		return trim(str, null, -1);
	}

	/**
	 * aviyy 字符串首字母大写
	 */
	public static String upperFirst(String str) {
		char[] cs = str.toCharArray();
		if (cs[0] >= 'a' && cs[0] <= 'z') {
			cs[0] -= 32;
		}
		return String.valueOf(cs);
	}

	/**
	 * aviyy 字符串首字母小写
	 */
	public static String lowerFirst(String str) {
		char[] cs = str.toCharArray();
		if (cs[0] >= 'A' && cs[0] <= 'Z') {
			cs[0] += 32;
		}
		return String.valueOf(cs);
	}

	/**
	 * 除去字符串头尾部的指定字符
	 */
	private static String trim(String str, String stripChars, int mode) {
		if (str == null) {
			return null;
		}

		int length = str.length();
		int start = 0;
		int end = length;

		// 扫描字符串头部
		if (mode <= 0) {
			if (stripChars == null) {
				while ((start < end) && (Character.isWhitespace(str.charAt(start)))) {
					start++;
				}
			} else if (stripChars.length() == 0) {
				return str;
			} else {
				while ((start < end) && (stripChars.indexOf(str.charAt(start)) != -1)) {
					start++;
				}
			}
		}

		// 扫描字符串尾部
		if (mode >= 0) {
			if (stripChars == null) {
				while ((start < end) && (Character.isWhitespace(str.charAt(end - 1)))) {
					end--;
				}
			} else if (stripChars.length() == 0) {
				return str;
			} else {
				while ((start < end) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
					end--;
				}
			}
		}

		if ((start > 0) || (end < length)) {
			return str.substring(start, end);
		}
		return str;
	}

	public static boolean isEmpty(Object obj) {
		if(obj == null){
			return true;
		}
		if(obj == ""){
			return true;
		}
		if (obj instanceof String) {
			if (((String) obj).length() == 0 || "[]".equals(obj) || "null".equals(obj)||"".equals(((String) obj).trim())) {
				return true;
			}
		} else if (obj instanceof Collection) {
			if (((Collection) obj).size() == 0) {
				return true;
			}
		} else if (obj instanceof Map) {
			if (((Map) obj).size() == 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	public static String getString(Object value) {
		return value != null && !"null".equals(value) ? String.valueOf(value).trim() : "";
	}

	public static String rightPad(String str, int size, char padChar) {
		if (str == null) {
			return null;
		}
		int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (pads > PAD_LIMIT) {
			return rightPad(str, size, String.valueOf(padChar));
		}
		return str.concat(padding(pads, padChar));
	}

	public static String rightPad(String str, int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = " ";
		}
		int padLen = padStr.length();
		int strLen = str.length();
		int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1 && pads <= PAD_LIMIT) {
			return rightPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return str.concat(padStr);
		} else if (pads < padLen) {
			return str.concat(padStr.substring(0, pads));
		} else {
			char[] padding = new char[pads];
			char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return str.concat(new String(padding));
		}
	}

	private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
		if (repeat < 0) {
			throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
		}
		final char[] buf = new char[repeat];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = padChar;
		}
		return new String(buf);
	}

	public static String substringAfterLast(String str, String separator) {
		if (str != null && str.length() != 0) {
			if (separator != null && separator.length() != 0) {
				int pos = str.lastIndexOf(separator);
				return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length())
						: "";
			} else {
				return "";
			}
		} else {
			return str;
		}
	}

	public static String substring(String str, int num1, int num2) {
		String Str2 = str.trim();
		Str2 = Str2.substring(num1, num2);

		return new String(Str2);
	}

	// 方法 返回true为包含中文；false不包含
	public static boolean isContainsChinese(String str) {
		Pattern pat = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher matcher = pat.matcher(str);
		boolean flg = false;
		if (matcher.find()) {
			flg = true;
		}
		return flg;
	}

	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				// sb.append("0").append(str);// left+0
				sb.append(str).append("0");//right+0
				str = sb.toString();
				strLen = str.length();
			}
		}
		return str;
	}

	public static <T> List<T> castList(Object obj, Class<T> clazz)
	{
	    List<T> result = new ArrayList<T>();
	    if(obj instanceof List<?>)
	    {
	        for (Object o : (List<?>) obj)
	        {
	            result.add(clazz.cast(o));
	        }
	        return result;
	    }
	    return null;
	}
	
	public static Map<String, Object> getValue(Object thisObj) throws ClassNotFoundException, Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Class<?> c = Class.forName(thisObj.getClass().getName());
		Method[] m = c.getMethods();
		for (int i = 0; i < m.length; i++) {
			String method = m[i].getName();
			if (method.startsWith("get")) {
				Object value = m[i].invoke(thisObj);
				if (value != null && !"getClass".equals(method)) {
					String key = method.substring(3);
					key = key.substring(0, 1).toUpperCase() + key.substring(1);
					map.put(method.substring(3).toLowerCase(), value);
				}
			}
		}
		return map;
	}


    /**
     * 描述：获取字符串中被两个字符（串）包含的所有数据
     * @param str 处理字符串
     * @param start 起始字符（串）
     * @param end 结束字符（串）
     * @param isSpecial 起始和结束字符是否是特殊字符
     * @return Set<String>
     */
    public static Set<String> getStrContainData(String str, String start, String end, boolean isSpecial){
        Set<String> result = new HashSet<>();
        if(isSpecial){
            start = "\\" + start;
            end = "\\" + end;
        }
        String regex = start + "(.*?)" + end;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while(matcher.find()){
            String key = matcher.group(1);
            if(!key.contains(start) && !key.contains(end)){
                result.add(key);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Set<String> checkLObjSet = StringUtil.getStrContainData("【检查对象】是否符合#标准#中对该#检查条目#的要求", "【", "】", false);
        Set<String> checkParamSet = StringUtil.getStrContainData("【检查对象】是否符合#标准#中对该#检查条目#的要求", "#", "#", false);
        System.out.println(checkParamSet.size());
    }

}
