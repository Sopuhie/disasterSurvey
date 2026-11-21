package com.santech.module.biz.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.styling.SLDParser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import org.opengis.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.vo.ShpTableFieldsVo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;

public class ShpHandlerUtil {
	private static final Logger logger = LoggerFactory.getLogger(ShpHandlerUtil.class);
	
	/**
	 * @param getShpTableFields
     * @Description:获取目标shp的字段信息
     * @return:
     */
	 public static Map<String,Double> getShpBounds(String shpFilePath,String encode) throws Exception {
	        ShapefileDataStore shpDataStore = null;
	        Map<String,Double> boundsData = new HashMap<String,Double>();
	        try {
	            shpDataStore = buildDataStore(shpFilePath,encode);
	            String typeName = shpDataStore.getTypeNames()[0];
	            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
	            ReferencedEnvelope bounds = featureSource.getBounds();
	            boundsData.put("minX", bounds.getMinX());
	            boundsData.put("maxX", bounds.getMaxX());
	            boundsData.put("minY", bounds.getMinY());
	            boundsData.put("maxY", bounds.getMaxY());
	            boundsData.put("height", bounds.getHeight());
	            boundsData.put("width", bounds.getWidth());
	        } catch (Exception e) {
	        	logger.error("获取shp【{}】的边界或四至信息出现异常，详情：",shpFilePath,e);
	            throw new Exception(e.getMessage());
	        } finally {
	        	if(shpDataStore!=null) {
	        		shpDataStore.dispose();
	        	}
	        }
	        return boundsData;
	    }
	
	/**
	 * @param getShpTableFields
     * @Description:获取目标shp的字段信息
     * @return:
     */
    public static List<ShpTableFieldsVo> getShpTableFields(String shpFilePath,String encode) throws Exception {
        ShapefileDataStore shpDataStore = null;
        List<ShpTableFieldsVo> fields = new ArrayList<ShpTableFieldsVo>();
        try {
            shpDataStore = buildDataStore(shpFilePath,encode);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
            List<AttributeDescriptor> attrList = featureSource.getSchema()
                    .getAttributeDescriptors();
            for (AttributeDescriptor attr : attrList) {
            	ShpTableFieldsVo field = new ShpTableFieldsVo();
            	field.setName(attr.getLocalName());
                field.setType(attr.getType().getBinding().getSimpleName());
                fields.add(field);
            }
        } catch (Exception e) {
        	logger.error("获取shp【{}】的字段信息出现异常，详情：",shpFilePath,e);
            throw new Exception(e.getMessage());
        } finally {
            shpDataStore.dispose();
        }
        return fields;
    }
	
    /**
	 * @param getTableCommonPropertiesRecords
     * @Description:获取目标shp的属性记录信息，不含空间几何图形数据
     * @return:
     */
    public static List<Map<String, Object>> getTableCommonPropertiesRecords(String shpFilePath,String encode) throws Exception {
        ShapefileDataStore shpDataStore = null;
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        try {
            shpDataStore = buildDataStore(shpFilePath,encode);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
            SimpleFeatureCollection result = featureSource.getFeatures();
            SimpleFeatureIterator itertor = result.features();
            //遍历写入json
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                Map<String, Object> data = new HashMap<String, Object>();
                //获取属性字段数据信息
                String fid = feature.getID();
                data.put("fid", fid);
            	Collection<Property> properties = feature.getProperties();
                for (Property property : properties) {
                     String name = property.getName().getLocalPart().toLowerCase(Locale.ROOT);
                     if(!"the_geom".equalsIgnoreCase(name)) {
                    	 data.put(name, property.getValue());
                     }
                }
                datas.add(data);
            }
            itertor.close();
        } catch (Exception e) {
        	logger.error("获取shp【{}】的属性记录信息出现异常，详情：",shpFilePath,e);
            throw new Exception(e.getMessage());
        } finally {
            shpDataStore.dispose();
        }
        return datas;
    }
    
    /**
	 * @param getTableTargetRecordGeometry
     * @Description:获取目标shp的属性记录的空间几何图形数据
     * @return:
     */
    public static List<Map<String, Object>> getTableTargetRecordGeometryByFid(String shpFilePath,String encode, String fid) throws Exception {
    	ShapefileDataStore shpDataStore = null;
    	List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        try {
            shpDataStore = buildDataStore(shpFilePath,encode);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
            //方式1
           /* String queryCql= "ID IN ('"+fid+"')";
            Filter filter = ECQL.toFilter(queryCql);*/
            //方式2
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
            FeatureId id = ff.featureId(fid);
            Id idFilter = ff.id(id);
            SimpleFeatureCollection result = featureSource.getFeatures(idFilter);
            SimpleFeatureIterator itertor = result.features();
            //遍历写入json
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                Map<String, Object> data = new HashMap<String, Object>();
                //获取属性字段数据信息
                Geometry rangeGeo = (Geometry) feature.getDefaultGeometry();
                data.put("geometry",rangeGeo.toString());
                datas.add(data);
            }
            itertor.close();
        } catch (Exception e) {
        	logger.error("根据FID【{}】获取shp【{}】对应记录的几何图形信息出现异常，详情：",fid,shpFilePath,e);
            throw new Exception(e.getMessage());
        } finally {
            shpDataStore.dispose();
        }
        return datas;
    }
    
    /**
     * @param shpFile
     * @param 目标记录的ID
     * @Description:shp解析并转换为Geojson
     * @return:
     */
    public static Map<Object, Object> shapeToGeojson(String shpFilePath,String encode,String fid) throws Exception {
        FeatureJSON fjson = new FeatureJSON();
        ShapefileDataStore shpDataStore = null;
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        try {
            shpDataStore = buildDataStore(shpFilePath,encode);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
            //获取shp字段信息
            List<ShpTableFieldsVo> fields = new ArrayList<ShpTableFieldsVo>();
            SimpleFeatureType schema = shpDataStore.getSchema();
            List<AttributeDescriptor> attrList = schema.getAttributeDescriptors();
            String geoType = "";
            for (AttributeDescriptor attr : attrList) {
            	ShpTableFieldsVo field = new ShpTableFieldsVo();
            	field.setName(attr.getLocalName());
                field.setType(attr.getType().getBinding().getSimpleName());
                fields.add(field);
                if(StringUtil.isEmpty(geoType)) {
                	geoType = convertShpGeomFieldType(attr.getType().getBinding());
                }
            }
            
            //获取要素信息
            SimpleFeatureCollection result = null;
            if(StringUtil.isNotEmpty(fid)) {
            	 FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
                 FeatureId id = ff.featureId(fid);
                 Id idFilter = ff.id(id);
                 result = featureSource.getFeatures(idFilter);
            }else {
            	 result = featureSource.getFeatures();
            }
            SimpleFeatureIterator itertor = result.features();
            JSONArray array = new JSONArray();
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                StringWriter writer = new StringWriter();
                fjson.writeFeature(feature, writer);
                JSONObject json = JSONObject.parseObject(writer.toString());
                //判断是否中空
               /* GeometryJSON geometryJson = new GeometryJSON();
                Geometry rangeGeo = geometryJson.read(json.getString("geometry"));
                boolean hollow = isHollow(rangeGeo);
                if (!hollow) {
                    throw new Exception("目标文件为多部件图形，不符合可用的shp图形信息");
                }*/
                array.add(json);
            }
            itertor.close();
            
            //获取空间参考id
            CoordinateReferenceSystem coordinateReferenceSystem = schema.getCoordinateReferenceSystem();
            int code = 0;
            if (!StringUtil.isEmpty(coordinateReferenceSystem)) {
                String wkt = coordinateReferenceSystem.toWKT();
                CoordinateReferenceSystem crsTarget = CRS.parseWKT(wkt);
                code = CRS.lookupEpsgCode(crsTarget, false);
            }

            map.put("fields", fields);
            map.put("features", array);
            map.put("wkid", code);
            map.put("fileName", typeName);
            map.put("geoType", geoType);
        } catch (Exception e) {
        	logger.error("获取shp【{}】的全量信息出现异常，详情：",shpFilePath,e);
            throw new Exception(e.getMessage());
        } finally {
            shpDataStore.dispose();
        }
        return map;
    }
    
    /**
	 * 向shp文件添加数据
	 * 
	 * @param shpPath 文件路径
	 * @param encode 文件编码格式
	 * @param datalist 空间及属性数据,如[{'name':'dfs','geom': 'POINT(108.555 34.645)'}]
	 * @param geometryKey 参数datalist中记录的空间属性字段的key
	 */
	public static boolean addFeature(String shpPath,String encode, List<Map<String, Object>> datalist,String geometryKey) {

		ShapefileDataStore dataStore = null;
		Transaction transaction = new DefaultTransaction("Adding");
		try {
			dataStore = buildDataStore(shpPath,encode);
			String typeName = dataStore.getTypeNames()[0];

			SimpleFeatureStore store = (SimpleFeatureStore) dataStore.getFeatureSource(typeName);
			store.setTransaction(transaction);
			SimpleFeatureType featureType = store.getSchema();
			// 获取字段列表
			List<String> fileds = new ArrayList<String>();
			List<AttributeDescriptor> attrList = featureType.getAttributeDescriptors();
            for (AttributeDescriptor attr : attrList) {
                fileds.add(attr.getLocalName());
            }
			// 创建Features
			List<SimpleFeature> list = new ArrayList<SimpleFeature>();
			WKTReader reader = new WKTReader();
			for (Map<String, Object> data : datalist) {
				SimpleFeatureBuilder build = new SimpleFeatureBuilder(featureType);
				if (data.get(geometryKey) != null) {
					Geometry geometry = reader.read(data.get(geometryKey).toString());
					build.add(geometry);
				}else {
					build.add(null);
				}
				for (String filed : fileds) {
					Object object = data.get(filed);
					build.add(object);
				}
				SimpleFeature feature = build.buildFeature(null);
				list.add(feature);
			}

			SimpleFeatureCollection collection = new ListFeatureCollection(featureType, list);
			store.addFeatures(collection);
			transaction.commit();
			return true;
		} catch (Exception e) {
			try {
				transaction.rollback();
			} catch (IOException ee) {
				ee.printStackTrace();
			}
			logger.error("往shp【{}】新增记录出现异常，异常详情：",shpPath,e);
			return false;
		}finally {
            if(dataStore!=null) {
            	dataStore.dispose();
            }
        }

	}

    
    
    /**
	 * 根据ID，移除shp中的数据
	 * @param shpPath shp文件路径
	 * @param fids 记录id,多个以英文逗号分割
	 * @param encode shp文件编码
	 */
	public static boolean removeFeature(String shpPath, String fids,String encode){
		ShapefileDataStore dataStore = null;
		Transaction transaction = new DefaultTransaction("Remove");
		try {
			dataStore = buildDataStore(shpPath,encode);
			String typeName = dataStore.getTypeNames()[0];
			SimpleFeatureStore store = (SimpleFeatureStore) dataStore.getFeatureSource(typeName);    
			store.setTransaction(transaction);      
			String[] fidArr = fids.split(",");
			for(int i=0;i<fidArr.length;i++) {
				 //设置过滤条件
	            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
	            FeatureId id = ff.featureId(fidArr[i]);
	            Id idFilter = ff.id(id);
	            store.removeFeatures(idFilter);
			}
			transaction.commit(); 
			return true;
		} catch (Exception e) {
			try {
				transaction.rollback();
			} catch (IOException ee) {
				ee.printStackTrace();
			}
			logger.error("根据fid【{}】对shp【{}】目标记录进行删除出现异常，详情：",fids,shpPath,e);
			return false;
		}finally {
        	if(dataStore!=null) {
        		dataStore.dispose();
            }
        }
    
	}

    /**
    * 生成或导出shape文件
    * @param shpPath  生成shape文件路径（包含文件名称） filepath
    * @param encode   shp文件编码
    * @param fileName   导出shp文件名,不含.shp后缀
    * @param geoType  图幅类型，Point和Polygon
    * @param crs  空间参考，如EPSG:4527
    * @param geometryKey   data中几何图形的key  如geom
    * @param attrKeys 属性key集合 keylist
    * @param data     图幅和属性集合 datalist
    * let param={code: "utf-8", 
        datalist: [ 
        {name:"地方",type:0,heigh:2.5,geom:"POINT(109.588 34.645)"}, 
        {name:"煽动反",type:2,heigh:3.2,geom:"POINT(109.588 34.645)"} 
        ], 
        filepath: "D:gisdata/gp/aaa/sx_test.shp", 
        geometryKey: "geom", 
        attrKeys: [ 
        { 
            fieldname: "name", 
            type: "string" 
        },{ 
            fieldname: "type", 
            type: "int" 
        },{ 
            fieldname: "heigh", 
            type: "double" 
        } 
        ], 
        geoType: "Point" 
        }
    */
    public static boolean write2Shape(String shpPath,String encode, String fileName, String geoType, String crs,String geometryKey, List<ShpTableFieldsVo> attrKeys, List<Map<String, Object>> data) {
           WKTReader reader = new WKTReader();
           ShapefileDataStore ds =null;
           FeatureWriter<SimpleFeatureType, SimpleFeature> writer = null;
       try {
           //创建shape文件对象
           ds = buildDataStore(shpPath,encode);
           //定义图形信息和属性信息
           SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
           //设置坐标系
           CoordinateReferenceSystem crs84 = CRS.decode(crs, true);
           tb.setCRS(crs84);
           //设置图层名
           tb.setName(fileName);
           //设置几何类型
           tb.add("the_geom", getClass(geoType));
           //设置属性字段
           for (ShpTableFieldsVo field : attrKeys) {
               tb.add(field.getName(), getClass(field.getType()));
           }
           ds.createSchema(tb.buildFeatureType());
           
           //设置Writer
           writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
           //写入文件信息
           for (int i = 0; i < data.size(); i++) {
               SimpleFeature feature = writer.next();
               Map<String, Object> row = data.get(i);
               Geometry geom = reader.read(row.get(geometryKey).toString());
               feature.setAttribute("the_geom", geom);
               for (ShpTableFieldsVo field : attrKeys) {
                   if (row.get(field.getName()) != null) {
                       feature.setAttribute(field.getName(), row.get(field.getName()));
                   } else {
                       feature.setAttribute(field.getName(), null);
                   }
               }
           }
           writer.write();
          return true;
       } catch (Exception e) {
    	   logger.error("将数据写出到shp出现异常，详情：",e);
           return false;
       } finally {
    	   try {
               if (writer != null) {
                   writer.close();
               }
               if(ds!=null) {
            	   ds.dispose();
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
    	  
       }
    }
    
    public static boolean writeGeoJSON2Shape(String shpPath,String encode, String fileName, String geoType, String crs,String geometryKey, List<ShpTableFieldsVo> attrKeys, JSONArray data) {
        ShapefileDataStore ds =null;
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = null;
    try {
        //创建shape文件对象
        ds = buildDataStore(shpPath,encode);
        //定义图形信息和属性信息
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        //设置坐标系
        CoordinateReferenceSystem crs84 = CRS.decode(crs, true);
        tb.setCRS(crs84);
        //设置图层名
        tb.setName(fileName);
        //设置几何类型
        tb.add("the_geom", getClass(geoType));
        //设置属性字段
        for (ShpTableFieldsVo field : attrKeys) {
            tb.add(field.getName(), getClass(field.getType()));
        }
        ds.createSchema(tb.buildFeatureType());
        
        //设置Writer
        writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
        //写入文件信息
        for (int i = 0; i < data.size(); i++) {
            SimpleFeature feature = writer.next();
            JSONObject row = data.getJSONObject(i);
            //row是geojson格式
            GeometryJSON geometryJson = new GeometryJSON();
            Geometry geom = geometryJson.read(row.get(geometryKey).toString());
            feature.setAttribute("the_geom", geom);
            for (ShpTableFieldsVo field : attrKeys) {
                if (row.get(field.getName()) != null) {
                    feature.setAttribute(field.getName(), row.get(field.getName()));
                } else {
                    feature.setAttribute(field.getName(), null);
                }
            }
        }
        writer.write();
       return true;
    } catch (Exception e) {
 	   logger.error("将数据写出到shp出现异常，详情：",e);
        return false;
    } finally {
 	   try {
            if (writer != null) {
                writer.close();
            }
            if(ds!=null) {
         	   ds.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
 	  
    }
 }
    
    /**
     * 将shp图形转为栅格导出
     * @param shpFilePath
     * @param encode
     * @param stylePath
     * @param outTiffPath
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static boolean exportShapeToTiff(String shpFilePath,String encode,String stylePath,String outTiffPath,int imageWidth,int imageHeight) {
    	ShapefileDataStore shpDataStore = null;
    	MapContent mapContent = null;
    	GeoTiffWriter writer = null;
        try {
            shpDataStore = buildDataStore(shpFilePath,encode);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
           
            // 创建地图内容
            mapContent = new MapContent();
            mapContent.setTitle("Shapefile to GeoTIFF");

            // 创建样式
            Style style = getStyleBySld(stylePath);
            if (style == null) {
                style =  SLD.createSimpleStyle((SimpleFeatureType) featureSource.getSchema());
            }
            Layer layer = new FeatureLayer(featureSource, style);
            mapContent.addLayer(layer);

            // 获取要素集合的范围
            ReferencedEnvelope bounds = featureSource.getBounds();
            if (bounds == null || bounds.isEmpty()) {
                bounds = featureSource.getFeatures().getBounds();
            }

            // 渲染图像
            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            StreamingRenderer renderer = new StreamingRenderer();
            renderer.setMapContent(mapContent);
            renderer.paint(image.createGraphics(), new java.awt.Rectangle(0, 0, imageWidth, imageHeight), bounds);
            
            // 保存为GeoTIFF文件
            GridCoverageFactory f = new GridCoverageFactory();
            GridCoverage2D coverage = f.create("Raster",image, bounds);
            File outputFile = new File(outTiffPath);
            writer = new GeoTiffWriter(outputFile);
            writer.write(coverage,null);
            // 清理
            writer.dispose();
            return true;
        } catch (Exception e) {
        	logger.error("根据shp【{}】导出对应的栅格图出现异常，详情：",shpFilePath,e);
        	return false;
        } finally {
        	if(writer!=null) {
        		writer.dispose();
        	}
        	if(mapContent!=null) {
        		mapContent.dispose();
        	}
        	if(shpDataStore!=null) {
        		shpDataStore.dispose();
        	}
           
        }
        
    }
    
    /**
     * 创建sld样式
     *
     * @param sldPath
     * @return
     */
    private static Style getStyleBySld(String sldPath) {
        try {
            Style style = null;
            if (!ObjectUtil.isEmpty(sldPath)) {
                //SLD的方式
                File sldFile = new File(sldPath);
                StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
                SLDParser stylereader = new SLDParser(styleFactory, sldFile);
                Style[] stylearray = stylereader.readXML();
                try {
                    style = stylearray[0];
                } catch (Exception e) {
                    style = null;
                }
            }
            return style;
        } catch (Exception ex) {
        	logger.error("根据sld文件获取样式对象出现异常，异常详情：",ex);;
            throw new RuntimeException(ex);
        }
    }
    /**
     * @param shpFilePath shape文件路径。
     * @Description:构建ShapeDataStore对象。
     * @return:
     */
    private static ShapefileDataStore buildDataStore(String shpFilePath,String encode) {
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        try {
        	 File file = new File(shpFilePath);
             if (!file.getParentFile().exists()) {
                 FileUtil.mkdir(file.getParentFile());
             }
            ShapefileDataStore dataStore = (ShapefileDataStore) factory
                    .createDataStore(file.toURI().toURL());
            if (dataStore != null) {
                dataStore.setCharset(Charset.forName(StringUtil.isNotEmpty(encode)?encode:"UTF-8"));
            }
            return dataStore;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    /**
     * convert shpFileType to db field type
     * 字段类型有：短整型 长整型 浮点型 双精度 文本 日期
     *
     * @param value
     * @return
     */
   /* private static String convertShpFieldType2H2GISOrPG(Class value) throws Exception {
    	
        if (value.equals(String.class)) {//文本
            return "varchar";
        }
        if (value.equals(Integer.class)) {//短整型
            return "int";
        }
        if (value.equals(Long.class)) {//长整型
            return "bigint";
        }
        if (value.equals(Double.class) || value.equals(Float.class)) {//浮点型 双精度 保留精度，比如在金币上运算更安全
            return "numeric";
        }
        if (value.equals(Date.class)) {
            return "TIMESTAMP WITH TIME ZONE ";//日期, 使用包含时区的来处理
        }
        if (Geometry.class.isAssignableFrom(value)) {
            return "geometry";
        }
        //除了上述，还有点、线、面等空间类型，其他遇到再说
        return "";
    }*/
    
    /**
     * @param geometry geometry
     * @Description:识别环洞
     * @return:
     */
    private static boolean isHollow(Geometry geometry) throws Exception {
        int num = 0;
        try {
            GeometryCollection rangeGeo1 = (GeometryCollection) geometry;
            int count = rangeGeo1.getNumGeometries();
            for (int i = 0; i < count; i++) {
                Geometry geometry1 = rangeGeo1.getGeometryN(i);
                double area = geometry1.getArea();
                if (area < 0) {
                    num++;
                } else {
                    continue;
                }
            }
            if (num == count - 1) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    
    //根据geotype获取geo数据类型类
    private static Class<?>  getClass(String geoType) throws Exception {
         if ("String".equalsIgnoreCase(geoType)) {
     		 return String.class;
         } else if ("Integer".equalsIgnoreCase(geoType)) {
        	 return Integer.class;
         } else if ("Long".equalsIgnoreCase(geoType)) {
        	 return Long.class;
         } else if ("Double".equalsIgnoreCase(geoType)) {
        	 return Double.class; 
         } else if ("Float".equalsIgnoreCase(geoType)) {
        	 return Float.class; 
         } else if ("Date".equalsIgnoreCase(geoType)) {
        	 return Date.class;
         } else if ("Polygon".equalsIgnoreCase(geoType)) {
    		return Polygon.class;
         } else if ("MultiPolygon".equalsIgnoreCase(geoType)) {
            return MultiPolygon.class;
         } else if ("Point".equalsIgnoreCase(geoType)) {
        	return Point.class;
         } else if ("MultiPoint".equalsIgnoreCase(geoType)) {
        	return MultiPoint.class;
         } else if ("LineString".equalsIgnoreCase(geoType)) {
        	return LineString.class;
         } else if ("MultiLineString".equalsIgnoreCase(geoType)) {
        	return MultiLineString.class;
         } else {
            throw new Exception("未定义的类型：" + geoType);
         }
    }
    
    private static String convertShpGeomFieldType(Class value) {
        if (Geometry.class.isAssignableFrom(value)) {
        	if (value.equals(Point.class)) {
                return "Point";
            }
            if (value.equals(MultiPoint.class)) {
                return "MultiPoint";
            }
            if (value.equals(LineString.class)) {
                return "LineString";
            }
            if (value.equals(MultiLineString.class)) {
                return "MultiLineString";
            }
            if (value.equals(Polygon.class)) {
                return "Polygon";
            }
            if (value.equals(MultiPolygon.class)) {
                return "MultiPolygon";
            } 
        }
        return "";
    }
}

