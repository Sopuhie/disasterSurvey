package com.santech.module.biz.service.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.arcsde.data.ArcSDEDataStoreFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.ogr.jni.JniOGRDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.util.factory.Hints;
import org.geotools.xml.styling.SLDParser;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.santech.module.biz.service.AbstractWmsSupport;
import com.santech.module.biz.service.WMS;
import com.santech.module.biz.utils.ShpDirectoryDataStore;

import cn.hutool.core.util.ObjectUtil;

/**
 * 开源库geotools读取图片实现
 * WmsDataSourceTypeEnum shp,sde,postgis
 * connectionString 连接字符串，与第一个参数类型相关
 * - shp shp文件或目录
 * - sde dbtype=arcsde;server=192.168.0.2;port=5151;instance=orcl;user=DZJPGISDATA;password=DZJPGISDATA 注：键必须小写
 * - 其他(pg) 参考 geotools 连接参数拼接
 * SldStyleAccessor sld符号文件获取方法
 *
 */
public class GeoWmsImp extends AbstractWmsSupport implements WMS {
	private static final Logger logger = LoggerFactory.getLogger(GeoWmsImp.class);
    DataStore dataStore;
    String connString;
    String dataStoreString;


    /**
     * 自动识别连接字符串类型
     *
     * @param connString
     * @param sldStyleAccessor
     */
    public GeoWmsImp(String connString) {
        init(connString);
    }

    public GeoWmsImp(String connString, String shpEncode) {
        if (!connString.endsWith("shp")) {
            throw new RuntimeException("只支持shp设置编码");
        }
        init(connString);
        ((ShpDirectoryDataStore) dataStore).setCharset(shpEncode);
    }

    private void init(String connectionString) {
        try {
            this.dataStoreString = connectionString;
            connString = connectionString;
            if (connectionString.toLowerCase().contains(".shp")) {
                File shapefile = new File(connectionString);
                if (shapefile.isFile()) {
                    shapefile = shapefile.getParentFile();
                }
                Map<String,Object> params = new HashMap<String,Object>();
                params.put("url", shapefile.toURI().toURL());
                dataStore = new ShpDirectoryDataStore(shapefile, new ShapefileDataStoreFactory.ShpFileStoreFactory(new ShapefileDataStoreFactory(), params));
            } else if (connectionString.toLowerCase().contains(".sde")) {
            	Map<String,Object> params = connectString2Map(connectionString);
                DataStoreFactorySpi dataStoreFactorySpi = new ArcSDEDataStoreFactory();
                dataStore = dataStoreFactorySpi.createDataStore(params);
            } else if (connectionString.toLowerCase().contains(".gdb")) {
                OGRDataStoreFactory factory = new JniOGRDataStoreFactory();
                Map<String,Object> params = new HashMap<String,Object>();
                params.put("DriverName", "OpenFileGDB");
                params.put("DatasourceName", connectionString);
                dataStore = factory.createDataStore(params);
            } else if(connectionString.toLowerCase().contains(".tif")){
            	//此种类型不需要获取dataStore
            } else {
                throw new RuntimeException("无法识别数据源类型");
            }
        } catch (Exception ex) {
        	logger.error("根据资源链接地址【{}】初始化dataStore出现异常，异常详情：",connectionString,ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public BufferedImage getMap(String layerName, String symbolKey, String bbox, String bboxSr, int width, int height,String geoType,String stylePath,String imageType) {
        Layer targetLayer=null;
        String sourceSr="";
        CoordinateReferenceSystem sourceCr=null;
        SimpleFeatureCollection simpleFeatureCollection=null;
        SimpleFeatureIterator featureIterator=null;
        SimpleFeature feature=null;
        try {
            SimpleFeatureSource featureSource=null;
            //GeoToolFeatureSourcePool geoToolFeatureSourcePool = GeoToolFeatureSourcePool.getInstance();
            if (connString.toLowerCase().contains(".tif")) {
                targetLayer = openRasterLayer(connString, symbolKey, stylePath);
            } else {
                if (dataStore == null) {
                    throw new RuntimeException("数据源为空");
                }
                //featureSource= geoToolFeatureSourcePool.getFeatureSource(layerName, dataStoreString);
                //if (featureSource == null) {
                    featureSource = dataStore.getFeatureSource(layerName);
                    //geoToolFeatureSourcePool.addDataSource(layerName, dataStoreString, featureSource);
                //}
                targetLayer = createFeatureLayer(featureSource, symbolKey, stylePath,geoType);
            }
            double[] bboxArr = parseBbox(bbox);
            double x1 = bboxArr[0], y1 = bboxArr[1], x2 = bboxArr[2], y2 = bboxArr[3];

            //判断一下输入的bboxSr的坐标系和数据的坐标系是否一致，不一致的话就需要把输入的四至转换成数据的四至，这样才能截取到图片！
            //获取数据的坐标系
            // 要素集合
             simpleFeatureCollection = featureSource.getFeatures();
            // 获取要素迭代器
             featureIterator = simpleFeatureCollection.features();
            if(featureIterator.hasNext()){
                // 要素对象
                feature = featureIterator.next();
                sourceSr= String.valueOf(((Geometry)feature.getDefaultGeometry()).getSRID());
                sourceCr=feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();

            }
//            if(EmptyUtil.isEmpty(sourceSr)||EmptyUtil.isEmpty(sourceCr)){
//                throw new RuntimeException("获取数据的坐标系失败");
//            }
            //不一样就坐标转换
            if (ObjectUtil.isNotEmpty(sourceSr)&&!sourceSr.equals(bboxSr)){
                CoordinateReferenceSystem tarDecode = null;
                Envelope pixelBoundsEnv=null;
                Envelope transformEnp=null;
                try {
                    tarDecode = CRS.decode(bboxSr,true);
                    MathTransform mathTransform = CRS.findMathTransform(tarDecode,sourceCr);
                    pixelBoundsEnv = new Envelope(x1,x2,y1,y2);
                    transformEnp = JTS.transform(pixelBoundsEnv, mathTransform);
                    x1=transformEnp.getMinX();
                    y1=transformEnp.getMinY();
                    x2=transformEnp.getMaxX();
                    y2=transformEnp.getMaxY();
                    bboxSr=sourceSr;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            featureIterator.close();

            List<Layer> lsLayer = new ArrayList<>();
            lsLayer.add(targetLayer);
            BufferedImage baseImage = exportMap(lsLayer, x1, y1, x2, y2, bboxSr, width, height,imageType);
            return baseImage;
        } catch (IOException ex) {
        	logger.error("获取图层【{}】wms预览图片出现异常，异常详情：",layerName,ex);
            throw new RuntimeException(ex);
        } finally {
            if(targetLayer != null) {
                targetLayer.preDispose();
                targetLayer.dispose();
            }
        }
    }

    @Override
    public BufferedImage getMap(List<String> layerNames, List<String> symbolKeys, String bbox, String bboxSr,
    		int width, int height,String stylePath) {
        return getMapByFilter(layerNames, symbolKeys, null, bbox, bboxSr, width, height,stylePath);
    }

    @Override
    public BufferedImage getMapByFilter(List<String> layerNames, List<String> symbolKeys, Map<String, String> layerFilter, 
    		String bbox, String bboxSr, int width, int height,String stylePath) {
        List<Layer> lsLayer = new ArrayList<>();
        String sourceSr="";
        CoordinateReferenceSystem sourceCr=null;
        SimpleFeatureCollection simpleFeatureCollection=null;
        SimpleFeatureIterator featureIterator=null;
        SimpleFeature feature=null;
        try {
            //GeoToolFeatureSourcePool geoToolFeatureSourcePool = GeoToolFeatureSourcePool.getInstance();
            SimpleFeatureSource featureSource=null;
            for (int i = 0; i < layerNames.size(); i++) {
                final String layerName = layerNames.get(i);
                final String symbolKey = symbolKeys.get(i);
                Layer targetLayer;
                if (connString.toLowerCase().contains(".tif")) {
                    targetLayer = openRasterLayer(connString, layerName, stylePath);
                } else {
                    if (dataStore == null) {
                        throw new RuntimeException("数据源为空");
                    }

                    //featureSource = geoToolFeatureSourcePool.getFeatureSource(layerName, dataStoreString);
                    //if (featureSource == null) {
                        featureSource = dataStore.getFeatureSource(layerName);
                       // geoToolFeatureSourcePool.addDataSource(layerName, dataStoreString, featureSource);
                    //}

                    if (layerFilter != null && layerFilter.containsKey(layerName)) {
                        targetLayer = createFeatureLayer(new FilterFeatureSource(layerFilter.get(layerName), featureSource), symbolKey, stylePath,"");
                    } else {
                        targetLayer = createFeatureLayer(featureSource, symbolKey, stylePath,"");
                    }
                }

                lsLayer.add(targetLayer);
            }

            double[] bboxArr = parseBbox(bbox);
            double x1 = bboxArr[0], y1 = bboxArr[1], x2 = bboxArr[2], y2 = bboxArr[3];
            //判断一下输入的bboxSr的坐标系和数据的坐标系是否一致，不一致的话就需要把输入的四至转换成数据的四至，这样才能截取到图片！
            //获取数据的坐标系
            sourceSr= featureSource.getInfo().getCRS().getCoordinateSystem().getIdentifiers().toString();
            sourceSr=sourceSr.substring(1,sourceSr.length()-1);
            sourceCr= CRS.decode(sourceSr,true);
            //不一样就坐标转换
            if (!sourceSr.equals(bboxSr)){
                CoordinateReferenceSystem tarDecode = null;
                Envelope pixelBoundsEnv=null;
                Envelope transformEnp=null;
                try {
                    tarDecode = CRS.decode(bboxSr,true);
                    MathTransform mathTransform = CRS.findMathTransform(tarDecode,sourceCr);
                    pixelBoundsEnv = new Envelope(x1,x2,y1,y2);
                    transformEnp = JTS.transform(pixelBoundsEnv, mathTransform);
                    x1=transformEnp.getMinX();
                    y1=transformEnp.getMinY();
                    x2=transformEnp.getMaxX();
                    y2=transformEnp.getMaxY();
                    bboxSr=sourceSr;
                } catch (FactoryException | TransformException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

//            ISpatialReferenceFactory spatialReferenceEnvironment = new SpatialReferenceEnvironment();
//            ISpatialReference geographicCoordinateSystem = spatialReferenceEnvironment
//                    .createGeographicCoordinateSystem(4490);
//            point.setSpatialReferenceByRef(geographicCoordinateSystem);
//
//            ISpatialReference projectedCoordinateSystem = spatialReferenceEnvironment
//                    .createProjectedCoordinateSystem(4526);
//            IGeoTransformation iGeoTransformation = (IGeoTransformation) spatialReferenceEnvironment
//                    .createGeoTransformation(esriSRGeoTransformationType.esriSRGeoTransformation_NAD1927_To_WGS1984_5);
//
//            ISpatialReference spatialReference = null,spatialReference2 = null;
//            spatialReference = spatialReferenceFactory.createGeographicCoordinateSystem(4490);
//            spatialReference2=((ISpatialReferenceFactory2) spatialReferenceFactory).createSpatialReference(4526);
//
//            com.esri.arcgis.geometry.Point point=new com.esri.arcgis.geometry.Point();
//            point.setSpatialReferenceByRef(spatialReference);
//            point.putCoords(x1,y1);
//            point.projectEx(spatialReference2);




            BufferedImage baseImage = exportMap(lsLayer, x1, y1, x2, y2, bboxSr, width, height,null);

            return baseImage;
        } catch (Exception ex) {
        	logger.error("获取过滤后的图层【原：{}】【后：{}】wms预览图片出现异常，异常详情：",layerNames,lsLayer,ex);
            throw new RuntimeException(ex);
        } finally {
            for (int i = 0; i < lsLayer.size(); i++) {
                if(lsLayer.get(i) != null) {
                    lsLayer.get(i).preDispose();
                    lsLayer.get(i).dispose();
                }
            }
        }
    }

    BufferedImage exportMap(List<Layer> layers, double x1, double y1, double x2, double y2, String bboxSr, int width, int height,String imageType) {
        MapContent map = new MapContent();
        try {
            layers.forEach(layer -> {
                map.addLayer(layer);
            });

            CoordinateReferenceSystem crs = null;
            try {
                crs = CRS.parseWKT(bboxSr);
            } catch (Exception e) {
                try {
                    // longitudeFirst为true是xy坐标系，false为经纬坐标系
                    boolean longitudeFirst = Math.abs(x1) > 180 ? true : false;
                    crs = CRS.decode(bboxSr, longitudeFirst);
                } catch (Exception ex) {
                    //输入坐标系不符合规范则取地图坐标系"；
                    crs = map.getCoordinateReferenceSystem();
                }
            }

            ReferencedEnvelope mapArea = new ReferencedEnvelope(x1, x2, y1, y2, crs);
            // 初始化渲染器
            StreamingRenderer sr = new StreamingRenderer();
            sr.setMapContent(map);
            // 初始化输出图像
            BufferedImage bi = null;
            if("jpeg".equalsIgnoreCase(imageType)) {
            	bi = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
            }else {
            	bi = new BufferedImage(width, height,
                         BufferedImage.TYPE_INT_ARGB);
            }
            Graphics g = bi.getGraphics();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Rectangle rect = new Rectangle(0, 0, width, height);
            // 绘制地图
            sr.paint((Graphics2D) g, rect, mapArea);
            map.layers().clear();
            return bi;
        } catch (Exception ex) {
        	logger.error("将图层【{}】转成预览图片出现异常，异常详情：",layers,ex);;
            throw new RuntimeException(ex);
        } finally {
            map.dispose();
        }
    }

    Layer openRasterLayer(String rasterfile, String layerName, String stylePath) {
        try {
            File file = new File(rasterfile);
            if (!file.exists()) {
                throw new Exception("栅格文件不存在");
            }
            String filePath = Paths.get(rasterfile).getParent().toString();
            File[] files = new File(filePath).listFiles();
            if (ObjectUtil.isNotEmpty(files)) {
                Arrays.stream(files).filter(s -> s.getName().toLowerCase().endsWith(".ovr")).forEach(s -> s.delete());
            }
            GridCoverage2DReader reader;
            AbstractGridFormat format = GridFormatFinder.findFormat(file);
            // this is a bit hacky but does make more geotiffs work
            Hints hints = new Hints();
            if (format instanceof GeoTiffFormat) {
                hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
            }
            reader = format.getReader(file, hints);
            Style style = getStyleBySld(stylePath);
            if (style == null) {
                StyleFactory sf = CommonFactoryFinder.getStyleFactory();
                style = SLD.wrapSymbolizers(sf.getDefaultRasterSymbolizer());
            }
            return new GridReaderLayer(reader, style);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    Layer createFeatureLayer(SimpleFeatureSource featureSource, String symbolKey, String stylePath,String geoType) {
        try {
            Style style = getStyleBySld(stylePath);
            if (style == null) {
                // 简单方式
                if (ObjectUtil.isNotEmpty(geoType)){
                    switch (geoType){
                        case "Polygon":
                            style= SLD.createPolygonStyle(Color.BLACK,Color.BLACK,0.5F);
                            SLD.setPolyColour(style, new Color(201, 242, 208));
                            break;
                        case "LineString":
                            style= SLD.createLineStyle(Color.BLACK,1.0F);
                            SLD.setLineColour(style, Color.BLACK);
                            break;
                        case "Point":
                            style= SLD.createPointStyle("Circle",Color.BLACK,Color.BLACK,0.5F,3.0F);
                            SLD.setPointColour(style, Color.RED);
                            break;
                    }

                }else{
                    style = SLD.createSimpleStyle(featureSource.getSchema());
                    SLD.setPolyColour(style, new Color(201, 242, 208));
                    SLD.setLineColour(style, Color.BLACK);
                    SLD.setPointColour(style, Color.RED);
                }
            } else {
                // 处理:移除点线面配置里面的额外非对应几何的图形
                Class<?> binding = featureSource.getSchema().getGeometryDescriptor().getType().getBinding();
                style.featureTypeStyles().forEach(featureTypeStyle -> {
                    featureTypeStyle.rules().forEach(rule -> {
                        if (binding.equals(Polygon.class) || binding.equals(MultiPolygon.class)) {
                            rule.symbolizers().removeIf(symbolizer -> symbolizer instanceof PointSymbolizer || symbolizer instanceof LineSymbolizer);
                        } else if (binding.equals(LineString.class) || binding.equals(MultiLineString.class)) {
                            rule.symbolizers().removeIf(symbolizer -> symbolizer instanceof PointSymbolizer || symbolizer instanceof PolygonSymbolizer);
                        } else if (binding.equals(Point.class) || binding.equals(MultiPoint.class)) {
                            rule.symbolizers().removeIf(symbolizer -> symbolizer instanceof LineSymbolizer || symbolizer instanceof LineSymbolizer);
                        }
                    });
                });
            }
            Layer layer = new FeatureLayer(featureSource, style);
            return layer;
        } catch (Exception ex) {
        	logger.error("渲染样式创建目标要素图层对象出现异常，异常详情：",ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 创建sld样式
     *
     * @param sldPath
     * @return
     */
    Style getStyleBySld(String sldPath) {
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

    Map<String, Object> connectString2Map(String connectString) {
    	Map<String, Object> map = new HashMap<String, Object>();
        String[] keyPairArr = connectString.split(";");
        for (String keypair : keyPairArr) {
            String[] tempArr = keypair.split("=");
            if (tempArr.length != 2) {
                throw new RuntimeException(String.format("连接字符串[%s]格式不正确", connectString));
            }
            map.put(tempArr[0], tempArr[1]);
        }
        return map;
    }

    @Override
    public void close() throws IOException {
        if(dataStore != null) {
            dataStore.dispose();
        }
    }

    static class FilterFeatureSource implements SimpleFeatureSource {
        SimpleFeatureSource simpleFeatureSource;
        Filter filter;

        public FilterFeatureSource(String filter, SimpleFeatureSource simpleFeatureSource) throws CQLException {
            this.simpleFeatureSource = simpleFeatureSource;
            this.filter = CQL.toFilter(filter);
        }

        @Override
        public SimpleFeatureCollection getFeatures() throws IOException {
            return simpleFeatureSource.getFeatures(filter);
        }

        @Override
        public SimpleFeatureType getSchema() {
            return simpleFeatureSource.getSchema();
        }

        @Override
        public ReferencedEnvelope getBounds() throws IOException {
            return simpleFeatureSource.getBounds();
        }

        @Override
        public ReferencedEnvelope getBounds(Query query) throws IOException {
            return simpleFeatureSource.getBounds(query);
        }

        @Override
        public int getCount(Query query) throws IOException {
            return simpleFeatureSource.getCount(query);
        }

        @Override
        public Set<RenderingHints.Key> getSupportedHints() {
            return simpleFeatureSource.getSupportedHints();
        }

        @Override
        public Name getName() {
            return simpleFeatureSource.getName();
        }

        @Override
        public ResourceInfo getInfo() {
            return simpleFeatureSource.getInfo();
        }

        @Override
        public DataAccess<SimpleFeatureType, SimpleFeature> getDataStore() {
            return simpleFeatureSource.getDataStore();
        }

        @Override
        public QueryCapabilities getQueryCapabilities() {
            return simpleFeatureSource.getQueryCapabilities();
        }

        @Override
        public void addFeatureListener(FeatureListener featureListener) {
            simpleFeatureSource.addFeatureListener(featureListener);
        }

        @Override
        public void removeFeatureListener(FeatureListener featureListener) {
            simpleFeatureSource.removeFeatureListener(featureListener);
        }

        @Override
        public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
            return simpleFeatureSource.getFeatures(this.filter);
        }

        @Override
        public SimpleFeatureCollection getFeatures(Query query) throws IOException {
            return simpleFeatureSource.getFeatures(this.filter);
        }
    }
}
