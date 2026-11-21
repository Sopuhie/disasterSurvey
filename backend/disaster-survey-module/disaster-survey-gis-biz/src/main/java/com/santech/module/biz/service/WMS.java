package com.santech.module.biz.service;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.Map;

/**
 * WMS 图片生成
 */
public interface WMS extends Closeable {

    /**
     * 根据范围生成wms图片
     * @param layerName 图层名
     * @param symbolKey 对应符号化的标识键
     * @param bbox x1;y1;x2;y2 格式字符串
     * @param bboxSr 类似格式：EPSG:4326,或WKT格式坐标系字符串
     * @param width 图片宽度
     * @param height 图片高度
     * @param stylePath sld样式文件绝对路径
     * @return
     */
    BufferedImage getMap(String layerName,String symbolKey, String bbox, String bboxSr ,int width, int height,String geoType,String stylePath,String imageType);
    /**
     * 根据范围生成wms图片
     * @param layerNames 图层名数组
     * @param symbolKeys 对应符号化的标识键数组
     * @param bbox x1;y1;x2;y2 格式字符串
     * @param bboxSr 类似格式：EPSG:4326,或WKT格式坐标系字符串
     * @param width 图片宽度
     * @param height 图片高度
     * @param stylePath sld样式文件绝对路径
     * @return
     */
    BufferedImage getMap(java.util.List<String> layerNames, java.util.List<String> symbolKeys, String bbox, String bboxSr , int width, int height,String stylePath);

    /**
     * 根据范围、过滤条件生成wms图片.
     * !!!注意: 对栅格过滤条件不生效
     * @param layerNames 图层名数组
     * @param symbolKeys 对应符号化的标识键数组
     * @param layerFilter 图层过滤条件,图层名为标识
     * @param bbox x1;y1;x2;y2 格式字符串
     * @param bboxSr 类似格式：EPSG:4326,或WKT格式坐标系字符串
     * @param width 图片宽度
     * @param height 图片高度
     * @param stylePath sld样式文件绝对路径
     * @return
     */
    BufferedImage getMapByFilter(java.util.List<String> layerNames, java.util.List<String> symbolKeys, Map<String, String> layerFilter , String bbox, String bboxSr , int width, int height,String stylePath);
}
