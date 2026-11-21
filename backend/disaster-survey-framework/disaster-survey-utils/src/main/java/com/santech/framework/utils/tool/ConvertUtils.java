package com.santech.framework.utils.tool;

import java.util.ArrayList;
import java.util.List;

public class ConvertUtils {

    /**
     * 将一组数分组，每组n个元素
     * @param source 数据源
     * @param n 每组n个元素
     */
    public static <T> List<List<T>> fixedGrouping(List<T> source, int n) {
        if (null == source || source.size() == 0 || n <= 0) {
            return null;
        }
        List<List<T>> result = new ArrayList<List<T>>();

        int sourceSize = source.size();
        int size = (source.size() / n) + 1;
        for (int i = 0; i < size; i++) {
            List<T> subset = new ArrayList<T>();
            for (int j = i * n; j < (i + 1) * n; j++) {
                if (j < sourceSize) {
                    subset.add(source.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }
}
