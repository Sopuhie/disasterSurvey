package com.santech.module.biz.utils;

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
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
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
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.vo.ShpTableFieldsVo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShpHandlerUtil1 {
	private static final Logger logger = LoggerFactory.getLogger(ShpHandlerUtil1.class);
	/**
     * @param shpFile
     * @Description:shp转换为Geojson
     * @return:
     */
    public static Map<String, Object> shapeToGeojson(File shpFile) throws Exception {
        FeatureJSON fjson = new FeatureJSON();
        ShapefileDataStore shpDataStore = null;
        HashMap<String, Object> map = new HashMap<>();
        try {
            shpDataStore = buildDataStore(shpFile.getPath(),"UTF-8");
            //设置编码
            //Charset charset = Charset.forName("GBK");
            //shpDataStore.setCharset(charset);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
            SimpleFeatureCollection result = featureSource.getFeatures();
            SimpleFeatureIterator itertor = result.features();
            String geoType = "";
            //遍历写入json
            JSONArray array = new JSONArray();
            List<JSONObject> fields = new ArrayList<JSONObject>();
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                //获取字段信息
                if(fields.size()<1) {
                	 Collection<Property> properties = feature.getProperties();
                     for (Property property : properties) {
                    	 JSONObject field = new JSONObject();
                         String name = property.getName().getLocalPart().toLowerCase(Locale.ROOT);
                         field.put("name", name);
                         field.put("alias", name);
                         field.put("type", property.getType().getBinding().getSimpleName());
                         fields.add(field);
                         if(ObjectUtil.isEmpty(geoType)) {
                         	geoType = convertShpGeomFieldType(property.getType().getBinding());
                         }
                     }
                }
                
                StringWriter writer = new StringWriter();
                fjson.writeFeature(feature, writer);
                JSONObject json = JSONObject.parseObject(writer.toString());
                //判断是否中空
               /* GeometryJSON geometryJson = new GeometryJSON();
                Geometry rangeGeo = geometryJson.read(json.getString("geometry"));
                boolean hollow = isHollow(rangeGeo);
                if (!hollow) {
                    throw new Exception("上传的文件为多部件图形，不符合上传条件");
                }*/
                array.add(json);
            }
            itertor.close();
            
            //获取空间参考id
            SimpleFeatureType schema = shpDataStore.getSchema();
            CoordinateReferenceSystem coordinateReferenceSystem = schema.getCoordinateReferenceSystem();
            int code = 0;
            if (!ObjectUtil.isEmpty(coordinateReferenceSystem)) {
                String wkt = coordinateReferenceSystem.toWKT();
                CoordinateReferenceSystem crsTarget = CRS.parseWKT(wkt);
                code = CRS.lookupEpsgCode(crsTarget, false);
            }

            map.put("fields", fields);
            map.put("features", array);
            map.put("wkid", code);
            map.put("fileName", shpFile.getName());
            map.put("geoType", geoType);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            shpDataStore.dispose();
        }
        return map;
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
            String outCrs = crs;
            if(!crs.contains("EPSG:")) {
            	outCrs = "EPSG:" + crs;
            }
            CoordinateReferenceSystem crs84 = CRS.decode(outCrs, true);
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
     
 /**
  * @Title createShpAndwriteFeatureByCatcheSeriJSONData 
  * @Description (此方法用来将ESRI转换为Geo后缓存到txt文件后，从缓存文件读取要素并将要素信息写入空白shp文件中，如果shp不存在则创建对应shp)
  * @param shpfile (空白shp所在文件夹路径)
  * @param outSR (空间参考，如EPSG:4326或直接4326，有做兼容)
  * @param esriGeoType (esri数据的集合类型)
  * @param esriFieldArr (esri数据的属性字段信息)
  * @param catcheFiles (图层要素或记录数据缓存的文件路径信息)
  * @param shapefileName (目标shp或图层名称，不带后缀)
  * @param encode (字符编码类型)
  */
  public static void createShpAndwriteFeatureByCatcheSeriJSONData(String shpfilePath,String outSR, String esriGeoType,
		  String shapefileName,String encode,JSONArray esriFieldArr, List<String> catcheFiles) throws Exception{
      try {
    	  //形成shp的geo字段类型形式
    	  List<ShpTableFieldsVo> attrKeys  = new ArrayList<ShpTableFieldsVo>();
    	  if(esriFieldArr!= null) {
    		  for (int i = 0; i < esriFieldArr.size(); i++) {
    			  JSONObject jsonObject = esriFieldArr.getJSONObject(i);
                  String name = jsonObject.getString("name");
                  String type = jsonObject.getString("type");
                  String alias = jsonObject.getString("alias");
                  //由于shape创建时如果存在字段名称带着下划线的会创建失败，故把带下划线的字段也进行剔除
                  if(!name.toLowerCase().contains("objectid") && !name.toLowerCase().contains("shape") && !"fid".equals(name.toLowerCase())&& !name.contains("_")){
                	  ShpTableFieldsVo targetField = new ShpTableFieldsVo();
                	  targetField.setName(name);
                	  targetField.setAliasName(alias);
                      String fieldsType = getFieldTypeByEsriString(type);
                      targetField.setType(fieldsType);
                      attrKeys.add(targetField);
                  }
              }
    	  }
    	  //整理数据并创建和写入shp
       	   for(String catcheFilePath:catcheFiles) {
       		    List<String> features = MapFileUtil.parseBaseInfoTXTContent(new File(catcheFilePath),encode);
       		    //形成shp的数据存储格式
       		    List<Map<String, Object>> datalist = new ArrayList<Map<String, Object>>();
	       		for (int i = 0; i < features.size(); i++) {
	       			Map<String, Object> targetData = new HashMap<String, Object>();
	                 String data = features.get(i);
	                 JSONObject feature = JSONObject.parseObject(data);
	                 //获取属性信息
	                 JSONObject attrs = feature.getJSONObject("properties");
	                 if(attrs!= null&&attrKeys.size()>0) {
	                	 for(ShpTableFieldsVo vo:attrKeys) {
		                	 targetData.put(vo.getName(), attrs.get(vo.getName()));
		                 }
	                 }
	                 
	                 //设置几何信息
	                 String geometryJSON = feature.getString("geometry");
	                 GeometryJSON geometryJson = new GeometryJSON();
	                 Geometry rangeGeo = geometryJson.read(geometryJSON);
	                 targetData.put("geom", rangeGeo.toString());
	                 datalist.add(targetData);
	      	   }
	       		if(new File(shpfilePath).exists()) {
					//往shp中插入数据
					boolean flag = addFeature(shpfilePath, encode, datalist, "geom");
					if(!flag) {
						throw new RuntimeException("往shp写入结果数据异常");
					}
				}else {
					//创建shp并插入对应数据
					String geoType = getGeometryClassByEsriGeoType(esriGeoType);
					boolean flag = write2Shape(shpfilePath, encode, shapefileName, geoType, outSR, "geom", attrKeys, datalist);
					if(!flag) {
						throw new RuntimeException("创建并往shp写入结果数据异常");
					}
				}
       	   }
		}finally {
           //对缓存文件进行删除
           MapFileUtil.deleteCatcheFileMx(catcheFiles);
		}
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
          	if(!"the_geom".equalsIgnoreCase(attr.getLocalName())) {
          		fileds.add(attr.getLocalName());
          	}
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
     * 根据esri几何的类型获取shp几何类
     * @param geoType
     * @return
     */
    private static String getGeometryClassByEsriGeoType(String esriType) {
       String geoClass;
         switch (esriType) {
            case "esriGeometryPolygon":
            	geoClass = "Polygon";
                break;
            case "esriGeometryPoint":
            	geoClass = "Point";
                break;
            case "esriGeometryMultipoint":
            	geoClass = "MultiPoint";
                break;
            case "esriGeometryPolyline":
            	geoClass = "LineString";
                break;
            default:
                throw new RuntimeException("Geometry中没有该类型：" + esriType);
        }
        return geoClass;
    }
  
  private static String getFieldTypeByEsriString(String fieldType) {
      String fieldTypeClass = "String";
      switch (fieldType) {
          case "esriFieldTypeOID":
              fieldTypeClass = "Integer";
              break;
          case "esriFieldTypeString":
              fieldTypeClass = "String";
              break;
          case "esriFieldTypeDouble":
              fieldTypeClass = "Double";
              break;
          case "esriFieldTypeInteger":
              fieldTypeClass = "Integer";
              break;
          case "esriFieldTypeDate":
              fieldTypeClass = "Date";
          default:
              break;
      }
      return fieldTypeClass;
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
    
    /**
     * convert shpFileType to db field type
     * 字段类型有：短整型 长整型 浮点型 双精度 文本 日期
     *
     * @param value
     * @return
     */
    private static String convertShpFieldType2H2GISOrPG(Class value) throws Exception {
    	
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
}

