package com.santech.module.biz.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.XmlUtil;
import com.santech.module.biz.entity.dto.ShpFileContentQueryDto;
import com.santech.module.biz.entity.dto.ShpFileExportImageDto;
import com.santech.module.biz.service.ShpDataHandlerService;
import com.santech.module.biz.service.ShpStyleService;
import com.santech.module.biz.service.WMS;
import com.santech.module.biz.utils.ShpHandlerUtil;

@Service
public class ShpDataHandlerServiceImpl implements ShpDataHandlerService {
	private static final Logger logger = LoggerFactory.getLogger(ShpDataHandlerServiceImpl.class);
	
	@Resource
 	private FileBasicConfig fileBasicConfig;

	@Resource
    private RedisUtil redisUtil;
    
    @Resource
	private ShpStyleService shpStyleService;
    

	@Override
	public ExecuteResult<List<Map<String, Object>>> queryShpTableTargetRecordGemoetryByFid(ShpFileContentQueryDto dto) {
		String filePath = dto.getFilePath();
    	try {
    		 logger.info("查询shp文件目标id的属性记录的几何图形数据信息，入参【{}】", dto);
             //请求参数
             if(StringUtil.isEmpty(filePath)) {
            	return ExecuteResult.error("shp文件路径不能为空",null);
             }
             if(StringUtil.isEmpty(dto.getFid())) {
             	return ExecuteResult.error("shp记录id不能为空",null);
             }
             File shpFile = new File(getFileRootPath(fileBasicConfig)+filePath);
             if(!shpFile.exists()) {
            	 return ExecuteResult.error("未找到目标shp文件",null);
             }
             List<Map<String, Object>> datas = ShpHandlerUtil.getTableTargetRecordGeometryByFid(shpFile.getAbsolutePath(),dto.getEncode(), dto.getFid());
             return ExecuteResult.success("查询成功", datas);
         } catch (Exception e) {
             logger.error("查询shp文件【{}】目标id的属性记录的几何图形数据信息异常，失败原因【{}】,异常详情：",filePath, e.getMessage(),e);
             return ExecuteResult.error("查询失败",null);
         }
	}
	
	
	@Override
	public ExecuteResult<Map<Object, Object>> queryShpTableTotalPropertiesRecord(ShpFileContentQueryDto dto) {
		String filePath = dto.getFilePath();
    	try {
    		 logger.info("查询shp文件全量数据信息（含几何图形），入参【{}】", dto);
             //请求参数
             if(StringUtil.isEmpty(filePath)) {
            	return ExecuteResult.error("shp文件路径不能为空",null);
             }
             File shpFile = new File(getFileRootPath(fileBasicConfig)+filePath);
             if(!shpFile.exists()) {
            	 return ExecuteResult.error("未找到目标shp文件",null);
             }
             Map<Object, Object> cacheDatas = redisUtil.hmget("total_"+filePath);
 	    	 if(cacheDatas!=null&&cacheDatas.size()>0) {
 	    		return ExecuteResult.success("查询成功", cacheDatas);
 	    	 }
             Map<Object, Object> datas = ShpHandlerUtil.shapeToGeojson(shpFile.getAbsolutePath(),dto.getEncode(), dto.getFid());
     	     redisUtil.hmset("total_"+filePath, datas, 8*60*60);//将查询结果放入缓存并设置缓存时效1小时
             return ExecuteResult.success("查询成功", datas);
         } catch (Exception e) {
             logger.error("查询shp文件【{}】全量数据信息（含几何图形）异常，失败原因【{}】,异常详情：",filePath, e.getMessage(),e);
             return ExecuteResult.error("查询失败",null);
         }
	}
	

    @Override
    public void getShpWms(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OutputStream os = null;
        InputStream is = null;
        String filePath = StringUtil.isNotEmpty(request.getParameter("filePath")) ? request.getParameter("filePath") : "";
        File shpFile = new File(getFileRootPath(fileBasicConfig)+filePath);
        String absoluteFilePath = shpFile.getAbsolutePath();
        WMS wms = null;
        try {
            String layerName = request.getParameter("layername"),
                    WIDTH = request.getParameter("WIDTH"),
                    HEIGHT = request.getParameter("HEIGHT"),
                    BBOX = request.getParameter("BBOX");
            String bboxSr = request.getParameter("bboxSr");

            String symbol = layerName;
            if(!checkSymbol(layerName)) {
            	//首先通过图层名进行验证是否存在具体样式，没有则读接口传来的参数
            	symbol = request.getParameter("symbol");
            }
            String type = request.getParameter("type");
            if (WIDTH == null || HEIGHT == null) {
                return;
            }
            if (WIDTH.equals("") || HEIGHT.equals("")) {
                return;
            }
            int _w = Integer.parseInt(WIDTH), _h = Integer.parseInt(HEIGHT);
            String[] BBOXS = BBOX.split(",");
            double[] doubles = new double[BBOXS.length];
            boolean flag1 = false;
            for (int i = 0; i < BBOXS.length; i++) {
                doubles[i] = Double.parseDouble(BBOXS[i]);
                if (doubles[i] > 180) {
                    flag1 = true;
                    break;
                }
            }
            String bbox = "";
            if (flag1) {
                bbox = BBOXS[0] + "," + BBOXS[1] + "," + BBOXS[2] + "," + BBOXS[3];
            } else {
                bbox = BBOXS[1] + "," + BBOXS[0] + "," + BBOXS[3] + "," + BBOXS[2];
            }
            wms = new GeoWmsImp(absoluteFilePath);
            //**********geotools读取shp中文名,需要把中文名转义***********************
            layerName = URLEncoder.encode(layerName, "UTF-8");
            if (StringUtil.isEmpty(symbol)) {
                symbol = layerName;
            }
            String stylePath = getSldConfigPath(shpFile.getParentFile().getParentFile().getAbsolutePath(),symbol);
            BufferedImage img = wms.getMap(layerName, symbol, bbox, bboxSr, _w, _h,null,stylePath,null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean flag = ImageIO.write(img, "png", out);
            byte[] wmsByte = out.toByteArray();

            os = response.getOutputStream();
            is = new ByteArrayInputStream(wmsByte);

            int count = 0;
            byte[] buffer = new byte[1024 * 4];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
            out.close();
        } catch (IOException e) {
        	logger.error("shp快显出现异常，详情：",e);
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (wms != null) {
                wms.close();
            }

        }
    }
    
    
    private boolean checkSymbol(String symbolKey) {
    	boolean checkResult = false;
    	try {
    		ExecuteResult<String> styleXmlResult = shpStyleService.queryShpStyleXmlInfoByName(symbolKey);
    		if("S".equalsIgnoreCase(styleXmlResult.getResultCode())&&StringUtil.isNotEmpty(styleXmlResult.getData())) {
    			checkResult = true;
    		}
		} catch (Exception e) {
			logger.error("根据key或图层名称[{}]获取地图样式异常，详情：",symbolKey,e);
		}
    	return checkResult;
    }
    
    private String getSldConfigPath(String cacheDirPath,String name){
    	String sldPath = Paths.get(cacheDirPath , name + ".sld").toString();
        try {
            if (!new File(sldPath).exists()) {
            	ExecuteResult<String> styleXmlResult = shpStyleService.queryShpStyleXmlInfoByName(name);
        		if("S".equalsIgnoreCase(styleXmlResult.getResultCode())&&StringUtil.isNotEmpty(styleXmlResult.getData())) {
        			String sldConfig = styleXmlResult.getData();
                    XmlUtil.createXml(sldConfig, cacheDirPath, name + ".sld", true);
        		}else {
                    sldPath = "";
                }
            }
            
        } catch (Exception e) {
            logger.error("生成临时样式文件【{]】异常，详情：",name + ".sld",e);
        }
        return sldPath;
    }


	@Override
	public void exportShpToImage(HttpServletResponse response, ShpFileExportImageDto dto){
		OutputStream os = null;
        InputStream is = null;
        String filePath = StringUtil.isNotEmpty(dto.getFilePath())?dto.getFilePath() : "";
        File shpFile = new File(getFileRootPath(fileBasicConfig)+filePath);
        String absoluteFilePath = shpFile.getAbsolutePath();
        String imageType = StringUtil.isEmpty(dto.getImageType())?"png":dto.getImageType();
        WMS wms = null;
        try {
            String layerName = dto.getLayerName();
            Integer width = dto.getWidth();
            Integer height =dto.getHeight();
            String BBOX = dto.getBbox();
            String bboxSr = StringUtil.isEmpty(dto.getBboxSr())?"EPSG:4326":dto.getBboxSr();

            String symbol = layerName;
            if(!checkSymbol(layerName)) {
            	//首先通过图层名进行验证是否存在具体样式，没有则读接口传来的参数
            	symbol = dto.getSymbol();
            }
            if(StringUtil.isEmpty(BBOX)) {
            	Map<String,Double> bounds = ShpHandlerUtil.getShpBounds(absoluteFilePath, "UTF-8");
            	if (bounds.get("minX") > 180) {
            		BBOX = bounds.get("minX")+","+bounds.get("minY")+","+bounds.get("maxX")+","+bounds.get("maxY");
                }else {
                	BBOX = bounds.get("minY")+","+bounds.get("minX")+","+bounds.get("maxY")+","+bounds.get("maxX");
                }
            	
            }
            if (StringUtil.isEmpty(width)|| StringUtil.isEmpty(height)) {
            	Map<String,Double> bounds = ShpHandlerUtil.getShpBounds(absoluteFilePath, "UTF-8");
            	width = 1024;
            	height = (int) (width * bounds.get("height") / bounds.get("width"));
            }
            int _w = width, _h = height;
            String[] BBOXS = BBOX.split(",");
            double[] doubles = new double[BBOXS.length];
            boolean flag1 = false;
            for (int i = 0; i < BBOXS.length; i++) {
                doubles[i] = Double.parseDouble(BBOXS[i]);
                if (doubles[i] > 180) {
                    flag1 = true;
                    break;
                }
            }
            String bbox = "";
            if (flag1) {
                bbox = BBOXS[0] + "," + BBOXS[1] + "," + BBOXS[2] + "," + BBOXS[3];
            } else {
                bbox = BBOXS[1] + "," + BBOXS[0] + "," + BBOXS[3] + "," + BBOXS[2];
            }
           
            //**********geotools读取shp中文名,需要把中文名转义***********************
            String cacheDirPath = shpFile.getParentFile().getParentFile().getAbsolutePath();
            String imagePath = cacheDirPath+File.separator+layerName+"."+imageType;
            File imageFile = new File(imagePath);
            OutputStream out = null;
            if(!imageFile.exists()) {
            	layerName = URLEncoder.encode(layerName, "UTF-8");
                if (StringUtil.isEmpty(symbol)) {
                    symbol = layerName;
                }
            	String stylePath = getSldConfigPath(cacheDirPath,symbol);
            	if("tiff".equalsIgnoreCase(imageType)) {
            		 boolean flag = ShpHandlerUtil.exportShapeToTiff(absoluteFilePath, "UTF-8", stylePath, imagePath, _w,_h);
            		 if(!flag) {
                     	return;
                     }
            	}else {
            		wms = new GeoWmsImp(absoluteFilePath);
                    BufferedImage img = wms.getMap(layerName, symbol, bbox, bboxSr, _w, _h,null,stylePath,imageType);
                    out = new FileOutputStream(imageFile);
                    boolean flag = ImageIO.write(img, imageType, out);
                    out.flush();
                    out.close();
                    if(!flag) {
                    	return;
                    }
            	}	
            	
            }
            //获取响应输出流
            response.reset();
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(layerName+"."+imageType, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            os = response.getOutputStream();
            is = new FileInputStream(imageFile);

            int count = 0;
            byte[] buffer = new byte[1024 * 4];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
        } catch (Exception e) {
        	logger.error("shp导出图片出现异常，详情：",e);
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	try {
        		 if (is != null) {
                     is.close();
                 }
                 if (os != null) {
                     os.close();
                 }
                 if (wms != null) {
                     wms.close();
                 }
			} catch (Exception e2) {
				logger.error("流关闭异常，详情：",e2);
			}
        }
	}
	
	/**
     * 获取分片数据本地存储根目录
     * @return
     */
    private String getFileRootPath(FileBasicConfig commonConfig) {
    	String names= System.getProperty("os.name");
    	String baseFilePath;
    	 if(names.toLowerCase().startsWith("win")){
         	baseFilePath = commonConfig.getRootForWin();
         }else{
         	baseFilePath = commonConfig.getRootForLiunx();
         }
    	 return baseFilePath;
    }

}
