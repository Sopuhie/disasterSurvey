package com.santech.module.biz.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.vo.txt.FieldVo;
import com.santech.module.biz.entity.vo.txt.SpatialReferenceVo;
import com.santech.module.biz.entity.vo.txt.TxtContentInfoVo;
import com.santech.module.biz.entity.vo.txt.TxtCoordPointVo;
import com.santech.module.biz.entity.vo.txt.TxtPropertiesInfoVo;
import com.santech.module.biz.entity.vo.txt.TxtSegmentVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TxtToGeoJsonUtil {
	
	 public static final String PROPERTIES_DESCRIPTION = "[属性描述]";

     public static final String BLOCK_COORDS = "[地块坐标]";

     public static TxtContentInfoVo readTxtFile(File txtFile) throws Exception{
         if (txtFile==null||!txtFile.exists()) {
             throw new RuntimeException("未发现目标TXT文件");
         }
         TxtContentInfoVo txtModel = new TxtContentInfoVo();
         TxtPropertiesInfoVo propModel = new TxtPropertiesInfoVo();
         List<TxtSegmentVo> segs = new ArrayList<TxtSegmentVo>();
         boolean readPropertiesStart = false;
         boolean readCoordStart = false;
         Reader reader = null;
         BufferedReader bufferReader = null;
         try {
        	 String filecharset = getFilecharset(txtFile);
        	 reader = new InputStreamReader(new FileInputStream(txtFile), filecharset);
        	 bufferReader = new BufferedReader(reader);
             String line = null;
    		 TxtSegmentVo seg = null;
    		 List<TxtCoordPointVo> points = null;
             // 下面读取文本数据，只对 ring 数据进行了读取，因为其他用不到
    		 while (StringUtil.isNotEmpty(line = bufferReader.readLine())){
                 if ( PROPERTIES_DESCRIPTION.equals(line)) {
                     readPropertiesStart = true;
                     readCoordStart = false;
                     continue;
                 }
                 if (BLOCK_COORDS.equals(line)){
                     readPropertiesStart = false;
                     readCoordStart = true;
                     continue;
                 }
                 if (readPropertiesStart){
                     // 读取属性描述信息
                     String[] propInfos = line.split("=");
                     if(propInfos.length<2) {
                    	 continue;
                     }
                     switch(propInfos[0]) {
                     case "格式版本号":
                    	 propModel.setVersion(propInfos[1]);
                    	 break;
                     case "数据产生单位":
                    	 propModel.setUnit(propInfos[1]);
                    	 break;
                     case "数据产生日期":
                    	 propModel.setDate(propInfos[1]);
                    	 break;
                     case "坐标系":
                    	 propModel.setGeoCoordSystem(propInfos[1]);
                    	 break;
                     case "几度分带":
                    	 propModel.setDegrees(Integer.parseInt(propInfos[1]));
                    	 break;
                     case "投影类型":
                    	 propModel.setProjection(propInfos[1]);
                    	 break;
                     case "计量单位":
                    	 propModel.setLinearUnit(propInfos[1]);
                    	 break;
                     case "带号":
                    	 propModel.setDegreeNumber(Integer.parseInt(propInfos[1]));
                    	 break;
                     case "精度":
                    	 propModel.setPrecision(Double.parseDouble(propInfos[1]));
                    	 break;
    	             case "转换参数":
    	            	 propModel.setTransformFactor(propInfos[1]);
    	            	 break;
    	             }
                 }
                 if (readCoordStart){
                     if (line.indexOf("@") > -1){
                         if (seg != null){
                             // 没错出现 @ 符号，表示上一个 ring 已经闭合
                        	 seg.setPoints(points);
                        	 segs.add(seg);
                         }
                         seg = new TxtSegmentVo();
                         seg.setBlockInfo(line);
                         points = new ArrayList<TxtCoordPointVo>();
                     }else{
                         // 坐标串格式是：点号,圈号，X坐标，Y坐标
                         String[] coordInfos = line.split(",");
                         TxtCoordPointVo point = new TxtCoordPointVo();
                         point.setPointNumber(coordInfos[0]);
                         point.setRingNumber(Integer.parseInt(coordInfos[1]));
                         point.setX(Double.parseDouble(coordInfos[3]));
                         point.setY(Double.parseDouble(coordInfos[2]));
                         points.add(point);
                     }
                 }
    		 }
    		seg.setPoints(points);
             // 将最后一个 ring 放入到集合中
            segs.add(seg);
		} finally {
			try {
				if(bufferReader!=null) {
					bufferReader.close();
				}
				if(reader!=null) {
					reader.close();
				}
				if(txtFile!=null&&txtFile.exists()) {
					//解析完就删除
					txtFile.delete();
				}
			} catch (Exception e) {
				log.error("解析txt空间数据，关闭流及删除文件是出现异常，详情：",e);
			}
		}
        txtModel.setProperties(propModel);
        txtModel.setSegs(segs);
        return txtModel;
     }
	
    /**
     * @Description: 判断txt文本编码类型
     * @params: sourceFile 文件
     * @return:java.lang.String
     */
    private static String getFilecharset(File sourceFile) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
            	bis.close();
                return charset; //文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF
                    && first3Bytes[1] == (byte) 0xFE) {

                charset = "UTF-16LE"; //文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {

                charset = "UTF-16BE"; //文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {

                charset = "UTF-8"; //文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {

                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {

                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        // 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {

                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            log.error("获取txt文件编码类型出现异常，详情：",e);
        }
        return charset;
    }
    
    public static Map<String,Object> convertTxtInfoModelToGeoJSONModel(TxtContentInfoVo txtModel){
        if (txtModel == null) {
            throw new RuntimeException("txt内容对象为空");
        }
        if (txtModel.getProperties() == null) {
            throw new RuntimeException("input argument 'txtModel' property 'Properties' must do not null");
        }
        if (txtModel.getSegs() == null || txtModel.getSegs().size() <1) {
            throw new RuntimeException("input argument 'txtModel' property 'Egs' list count must do not less zone");
        }
        int wkid = getWKIDByProjectionInfo(txtModel.getProperties().getDegrees(), txtModel.getProperties().getDegreeNumber());
        Map<String,Object> data = createDataModel(wkid);

        // 创建 features
        List<String> features = new ArrayList<String>();
        List<TxtSegmentVo> segVos = txtModel.getSegs();
        for (int index = 0; index < segVos.size(); index++) {
        	TxtSegmentVo seg = segVos.get(index);
        	//设置属性
            Map<String,Object> attributes = new HashMap<String,Object>();
            attributes.put("FID", index);
            attributes.put("Id", index);
            //按圈号进行分组
            List<TxtCoordPointVo> points = seg.getPoints();
            Map<Integer,List<TxtCoordPointVo>> groups = points.stream().collect(Collectors.groupingBy(TxtCoordPointVo::getRingNumber));
            //设置几何信息
            List<List<List<Double>>> ringList = new ArrayList<List<List<Double>>>();
            for(Entry<Integer,List<TxtCoordPointVo>> entity:groups.entrySet()) {
            	List<List<Double>> ring = new ArrayList<List<Double>>();
            	for(TxtCoordPointVo vo:entity.getValue()) {
            		List<Double> point = new ArrayList<Double>();
                	point.add(vo.getX());
                	point.add(vo.getY());
                	ring.add(point);
            	}
            	ringList.add(ring);
            }
            //定义一个子要素
            String geoFeatureJson = String.format("{\"geometry\":{\"coordinates\":%s,\"type\":\"%s\"},\"properties\": %s}", ringList,"Polygon", JSON.toJSONString(attributes));
            features.add(geoFeatureJson);
        }
        data.put("features", features);
        return data;
    }
    
    private static  Map<String,Object> createDataModel(int wkid) {
    	 Map<String,Object> data = new  HashMap<String,Object>();
        // 创建 fieldAliases 
    	Properties dic = new Properties();
    	dic.put("FID", "FID");
    	dic.put("Id", "Id");
    	data.put("fieldAliases", dic);
        // 创建 spatialReference
    	SpatialReferenceVo spatialReference = new SpatialReferenceVo();
    	spatialReference.setWkid(wkid);
    	spatialReference.setLatestWkid(wkid);
    	data.put("spatialReference", spatialReference);
        // 创建 fields
    	List<FieldVo> fields = new ArrayList<>();
    	FieldVo fieldVo1 = new FieldVo();
    	fieldVo1.setName("FID");
    	fieldVo1.setAlias("FID");
    	fieldVo1.setType("esriFieldTypeOID");
    	fields.add(fieldVo1);
    	FieldVo fieldVo2 = new FieldVo();
    	fieldVo2.setName("Id");
    	fieldVo2.setAlias("Id");
    	fieldVo2.setType("esriFieldTypeInteger");
    	fields.add(fieldVo2);
    	data.put("fields", fields);
        return data;
    }
    
    public static int getWKIDByProjectionInfo(int bandZone, int degreeZone){
        // 默认度带号，2000国家大地坐标系
        int zoneNum = 4490;
        if (bandZone == 6){
            zoneNum = 4509;  // CGCS2000 / Gauss-Kruger CM 117E
        }else{
            if (degreeZone == 38)
                zoneNum = 4526;
            if (degreeZone == 39)
                zoneNum = 4527;
            if (degreeZone == 40)
                zoneNum = 4528;
        }
        return zoneNum;
    }
}

