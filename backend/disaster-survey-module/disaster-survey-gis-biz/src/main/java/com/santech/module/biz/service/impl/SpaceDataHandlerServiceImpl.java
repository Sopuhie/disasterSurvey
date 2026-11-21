package com.santech.module.biz.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.JsonToShpDto;
import com.santech.module.biz.entity.vo.OverLayDataVo;
import com.santech.module.biz.entity.vo.txt.TxtContentInfoVo;
import com.santech.module.biz.service.SpaceDataHandlerService;
import com.santech.module.biz.utils.MapFileUtil;
import com.santech.module.biz.utils.ShpHandlerUtil;
import com.santech.module.biz.utils.TxtToGeoJsonUtil;

import cn.hutool.core.io.FileUtil;

/**
 * @Title: SpaceDataHandlerServiceImpl.java 
 * @Description：空间数据加工相关功能接口的业务层处理类 ，实现SpaceDataHandlerService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class SpaceDataHandlerServiceImpl implements SpaceDataHandlerService {
	
	private static final Logger logger = LoggerFactory.getLogger(SpaceDataHandlerServiceImpl.class);
	
	@Value("${service.rootPath:null}")
	private String rootPath;
	
	@Value("${service.encode:UTF-8}")
	private String dataEncode;
	

	
	@Override
	public ExecuteResult<List<OverLayDataVo>> getGeoJsonBySpaceDataFile(MultipartFile[] spaceDataFiles) {
		try {
			//logger.info("发布数据服务，发布数据服务使用的SRS类型【{}】",srs);
			//校验参数
			if(spaceDataFiles.length<1) {
				return ExecuteResult.error("上传文件不能为空", null);
			}
			//将文件下载并解压到目标目录
			//1.写入目标文件夹
			String targetDirPath = rootPath+File.separator+"overlay"+File.separator+File.separator+MapFileUtil.currentTimeMillis();
			//遍历上传文件并写入目标文件夹
			for (MultipartFile file : spaceDataFiles) {
                String fileName = file.getOriginalFilename();
                String suffix = FileUtil.getSuffix(fileName).toLowerCase();
                //String pathname = targetDirPath + File.separator + fileName;
                File uploadFile = MapFileUtil.multipartFileToFile(file, targetDirPath);
                boolean b;
                if ("zip".equals(suffix)) {
                    b = MapFileUtil.unZip(uploadFile, targetDirPath+File.separator+"data");
                    if (!b) {
                        throw new RuntimeException("解压ZIP【"+fileName+"】失败");
                    }
                }
            }
			//获取目标文件
			 List<File> files = MapFileUtil.getTargetFiles(new ArrayList<File>(), targetDirPath,  Arrays.asList("dwg", "txt", "shp"));
			 if(files.size()<1) {
				 return ExecuteResult.error("上传文件暂不支持解析（当前支持dwg、txt、shp类型数据）", null);
			 }
			 List<OverLayDataVo> resultList = new ArrayList<OverLayDataVo>();
			 for (File file : files) {
				 	OverLayDataVo vo = new OverLayDataVo();
	                String fileSuffixName = FileUtil.getSuffix(file).toLowerCase();
	                if (("txt").equals(fileSuffixName)) {
	                	TxtContentInfoVo content = TxtToGeoJsonUtil.readTxtFile(file);
	                	Map<String, Object> data = TxtToGeoJsonUtil.convertTxtInfoModelToGeoJSONModel(content);
	                	vo.setTxtData(data);
	                	resultList.add(vo);
	                } else if (("shp").equals(fileSuffixName)) {
	                    Map<Object, Object> data = ShpHandlerUtil.shapeToGeojson(file.getPath(),dataEncode,null);
	                    vo.setShpData(data);
	                	resultList.add(vo);
	                } 
	            }
			return ExecuteResult.success("数据解析成功",resultList);
		} catch (Exception e) {
			logger.error("对上传的空间数据文件进行空间数据解析并返回出现异常，原因：【{}】,详情：",e.getMessage(),e);
			return ExecuteResult.error("数据解析失败【"+e.getMessage()+"】！", null);
		}
	}
    
    
	@Override
	public void exportShpFile(HttpServletResponse response, JsonToShpDto dto) {
    	ZipOutputStream zos =null;
		try {
			logger.info("将数据以shp导出，入参，下载文件名称[{}]，shp属性字段[{}]。",dto.getFileName(),dto.getAttrFields());
			//校验参数
			String checkErr = checkExportShpFileParams(dto);
   			if(StringUtil.isNotEmpty(checkErr)) {
       			throw new RuntimeException("参数校验不通过，详情："+checkErr);
       		}
			
			//存放路径
			String shpDirPath = rootPath+File.separator+"export"+File.separator+"SHP"+System.currentTimeMillis();
			String shpName = dto.getFileName()+".shp";
			String absolutePath = shpDirPath+File.separator+shpName;
			//写出shhp
			boolean createResult = ShpHandlerUtil.write2Shape(absolutePath,dto.getEncode(), dto.getFileName(), dto.getGeoType(),dto.getCrs(), dto.getGeometryKey(), dto.getAttrFields(), dto.getFeatures());
			if(!createResult) {
				throw new RuntimeException("生成shp失败");
			}
			//判断并写出文件
			response.setContentType("application/zip; charset=UTF-8");
	        response.setDateHeader("Expires", 0);
	        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(dto.getFileName(), "UTF-8") + ".zip");
	        response.setHeader("Pragma", "no-cache");
	        response.setHeader("Cache-Control", "no-cache");
	        response.setCharacterEncoding("UTF-8");
			zos = new ZipOutputStream(response.getOutputStream());
			byte[] buffer = new byte[4096];
			boolean result = MapFileUtil.getZIP(new File(shpDirPath), zos, dto.getFileName(), true, buffer);
			if(!result) {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		} catch (Exception e) {
			logger.error("将请求中的空间数据信息以shp导出出现异常，详情：",  e);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}finally {
			if(zos!=null) {
				try {
					zos.flush();
					zos.close();
				} catch (IOException e) {
					logger.error("关闭压缩文件流异常");
				}
			}
	    }
		
	}
    
    
	 //校验新增的目录信息
  	 private String checkExportShpFileParams(JsonToShpDto model){
  		 StringBuilder checkInfo = new StringBuilder("");
  		 if(StringUtil.isEmpty(model)) {
  			 checkInfo.append("JsonToShpDto不能为空;");
  			 return checkInfo.toString();
  		 }
  		 if(StringUtil.isEmpty(model.getFileName())) {
  			checkInfo.append("图层名称【fileName】不能为空;");
  	     }
  		 if(StringUtil.isEmpty(model.getGeometryKey())) {
  			 checkInfo.append("要素数据中的几何图形字段的key【geometryKey】不能为空;");
  		 }
  		 if(StringUtil.isEmpty(model.getGeoType())) {
 			 checkInfo.append("几何图形类型【geoType】不能为空;");
 		 }
  		 if(StringUtil.isEmpty(model.getCrs())) {
			 checkInfo.append("空间参考【crs】不能为空;");
		 }
  		 if(StringUtil.isEmpty(model.getAttrFields())) {
 			 checkInfo.append("属性字段【attrFields】不能为空;");
 		 }
  		 if(StringUtil.isEmpty(model.getFeatures())) {
 			 checkInfo.append("要素数据【features】不能为空;");
 		 }
  		 if(StringUtil.isEmpty(model.getLoginUser())) {
  			checkInfo.append("当前登录人【loginUser】不能为空;");
  		 }
  		 return checkInfo.toString();
  	 }
    
    /*public static void main(String[] args) {
    	QueryStatisticsExpParamsDto params = new QueryStatisticsExpParamsDto();
    	params.setExportType("shp");
    	params.setGeometryType("esriGeometryPolygon");
    	params.setServiceUrl("http://10.17.135.189/arcgis14/rest/services/GK_SQSX/GK_SQSX_CZKFBJ_CSCG39_WP/MapServer/0/query");
    	params.setSpatialReference("4527");
    	params.setTargetFiled("OBJECTID");
    	params.setTargetFiledValues("218,219");
    	String shpPath = "D:\\home\\lyyzt_files"+File.separator+"statistic"+File.separator+"shp"+File.separator+"查询统计结果.shp";
		try {
			boolean result = MapServiceToShpExportUtil.exportToShp(params,shpPath, "GBK");
			System.out.println(result);
			String shpZipPath = "D:\\home\\lyyzt_files"+File.separator+"statistic" +File.separator+"shp"+File.separator;
			File shpFile = Paths.get(shpPath).toFile();
			File parentfile = shpFile.getParentFile();
			File[] listFiles = parentfile.listFiles();
	        List<String> listFile = new ArrayList<>();
	        for (int i = 0; i < listFiles.length; i++) {
	            File zFile = listFiles[i];
	            if(zFile.getName().endsWith(".lock")) {
	            	continue;
	            }
	            listFile.add(zFile.getAbsolutePath());
	        }
	        MapFileUtil.compressFiles(shpZipPath, "查询统计结果.zip", listFile);
	        String zipPath = shpZipPath + "查询统计结果.zip";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}*/
    
    
}
