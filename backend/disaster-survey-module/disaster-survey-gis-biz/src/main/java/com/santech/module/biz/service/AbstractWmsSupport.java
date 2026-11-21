package com.santech.module.biz.service;

/**
 * 生成Wms图片支持类
 * 方法 parseBbox 解析bbox 字符串(x1;y1;x2;y2)
 *      返回： 长度为4的double数组
 * 方法 mixImage 过滤图片1内容，只显示图片2相交的内容。
 *   ***注：图片2的底色必须为透明，不然起不到过滤效果
 */
public abstract class AbstractWmsSupport {
    public double[] parseBbox(String bBox){
        String[] boxArr = bBox.split(",");
        if(boxArr.length != 4) {
            throw new RuntimeException("BBOX格式不正确");
        }
        double x1 = Double.parseDouble(boxArr[0]), y1 = Double.parseDouble(boxArr[1]),
                x2 = Double.parseDouble(boxArr[2]), y2 = Double.parseDouble(boxArr[3]);
        return new double[] {x1, y1, x2, y2};
    }
}
