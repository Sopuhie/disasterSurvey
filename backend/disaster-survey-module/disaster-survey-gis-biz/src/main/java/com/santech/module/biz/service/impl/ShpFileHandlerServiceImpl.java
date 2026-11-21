package com.santech.module.biz.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.FileUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.entity.dto.ShpFileInfoDto;
import com.santech.module.biz.service.ShpFileHandlerService;
import com.santech.module.biz.utils.ShpFileUtil;

@Service
public class ShpFileHandlerServiceImpl implements ShpFileHandlerService {
	private static final Logger logger = LoggerFactory.getLogger(ShpFileHandlerServiceImpl.class);
	
	@Resource
 	private FileBasicConfig fileBasicConfig;
    
	@Override
	public ExecuteResult<ShpFileInfoDto> uploadShpFile(MultipartFile file) {
		 try {
	    	if(file==null||file.getOriginalFilename()==null) {
	     		return ExecuteResult.error("文件或文件名称不能为空", null);
	     	}
		    //获取文件信息
	    	String root = getFileRootPath(fileBasicConfig);
		    long fileSize = file.getSize();
		    String realFileName = file.getOriginalFilename();
		    String suffix = realFileName.substring(realFileName.lastIndexOf(".")+1);//文件后缀
	        if(!"zip".equalsIgnoreCase(suffix)) {
	        	return ExecuteResult.error("暂不支持非zip格式的shp数据上传", null);
	        }
		    //存储文件
	        String absoluteDirPath = "shp"+ File.separator +"SHP"+FileUtil.currentTimeMillis();
	        String targetDirPath = root+absoluteDirPath;
	        File shpZip = ShpFileUtil.multipartFileToFile(file, targetDirPath);
	        List<String> layerPaths = ShpFileUtil.unzipAndReturnShpPath(shpZip.getAbsolutePath(), targetDirPath);
	        if(StringUtil.isEmpty(layerPaths)) {
	        	return ExecuteResult.error("上传的zip文件不含shp数据", null);
	        }
	        List<ShpFileInfoDto> layers = new ArrayList<ShpFileInfoDto>();
	        for(String layerPath:layerPaths) {
	        	File layerFile = new File(layerPath);
	        	String fileName = layerFile.getName();
	        	String layerName =fileName.substring(0, fileName.lastIndexOf("."));
	        	String fileAbsolutePath = layerFile.getAbsolutePath();
	        	boolean b = shapefileIntegrity(fileAbsolutePath);
                if (!b) {
                     return ExecuteResult.error("上传的shape的zip文件缺少[.shx],[.dbf]后缀文件", null);
                }
	        	ShpFileInfoDto layer = new ShpFileInfoDto();
	        	layer.setFilePath(fileAbsolutePath.replace(getFileRootPath(fileBasicConfig), ""));
	        	layer.setFileName(layerName);
	        	layer.setFileType("2");//1文件夹，2文件
	        	layer.setFileExt("layer");
	        	layers.add(layer);
	        }
	        //组织文件信息返回
	        ShpFileInfoDto fileInfo = new ShpFileInfoDto();
    	    fileInfo.setFilePath(absoluteDirPath+File.separator+realFileName);
    	    fileInfo.setFileName(realFileName);
    	    fileInfo.setFileType("2");//1文件夹，2文件
    	    fileInfo.setFileSize(fileSize+"");
    	    fileInfo.setFileExt(suffix);
    	    fileInfo.setLayers(layers);
    	    return ExecuteResult.success("上传成功", fileInfo);
	    } catch (Exception e) {
	    	logger.error("基于本地IO操作上传文件【{}】出现异常，异常详情：", file.getOriginalFilename(), e);
	    	return ExecuteResult.error("上传失败,请联系管理员", null);
	    } 
	}
	
  private boolean shapefileIntegrity(String path) {
        File file = Paths.get(path).toFile();
        File parent = file.getParentFile();
        boolean isShapefile = false;
        boolean shapefileSuffixShp = false;
        boolean shapefileSuffixShx = false;
        boolean shapefileSuffixdbf = false;
        for (File listFile : parent.listFiles()) {
            if (listFile.getName().endsWith(".shp")) {
                shapefileSuffixShp = true;
            }
            if (listFile.getName().endsWith(".shx")) {
                shapefileSuffixShx = true;
            }
            if (listFile.getName().endsWith(".dbf")) {
                shapefileSuffixdbf = true;
            }
        }
        if (shapefileSuffixShp && shapefileSuffixShx && shapefileSuffixdbf) {
            isShapefile = true;
        }
        return isShapefile;
    }
	
	@Override
	public ExecuteResult<String> deleteShpFile(String filePath) {
		try {
	    	if(filePath.lastIndexOf(".")<0) {
               return ExecuteResult.error("文件路径不合法！", null); 
	    	}
	    	String targetPath =  getFileRootPath(fileBasicConfig) + filePath;
	    	File shpZip = new File(targetPath);
	    	File parentFile = shpZip.getParentFile();
	    	if(parentFile.getName().startsWith("SHP")) {
	    		ShpFileUtil.deleteFolder(parentFile);
	    	}else {
	    		return ExecuteResult.success("删除的目标路径非shp数据文件的存储路径");
	    	}
	        return ExecuteResult.success("删除目标Shp文件成功");
        }catch (Exception e){
        	logger.error("基于本地IO操作删除目标shp文件【{}】出现异常，异常详情：", filePath, e);
        	return ExecuteResult.error("删除目标shp文件失败", null);
        }
	}
	@Override
	public ExecuteResult<byte[]> downloadShpFileByZip(String filePath) {
		InputStream input = null;
		ByteArrayOutputStream output =null;
		try {
			File file = new File(getFileRootPath(fileBasicConfig)+filePath);
	    	if(file.exists()&&file.isFile()) {
	    		input = new FileInputStream(file);
		        output = new ByteArrayOutputStream();
		        byte[] buffer = new byte[4096];
		        int n = 0;
		        while (-1 != (n = input.read(buffer))) {
		            output.write(buffer, 0, n);
		        }
		        return ExecuteResult.success("获取文件流二进制数组成功", output.toByteArray());
	    	}else{
	    		return ExecuteResult.error("未获取到目标文件", null);
	    	}
		} catch (Exception e) {
			logger.error("基于本地IO操作获取目标文件【{}】字节数组出现异常，详情：", filePath, e);
	        return ExecuteResult.error("获取文件流二进制数组失败", null);
		}finally {
	    	try {
	    		if(output!=null) {
		    		output.close();
		    	}
		    	if(input!=null) {
		    		input.close();
		    	}
			} catch (IOException e) {
				e.printStackTrace();
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
