package com.santech.core.framework.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.entity.view.ChunkMergeParamsModel;
import com.santech.core.framework.entity.view.ChunkTaskModel;
import com.santech.core.framework.entity.view.ChunkUploadParamsModel;
import com.santech.core.framework.entity.view.UploadFileVo;
import com.santech.core.framework.service.FileHandlerService;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.FileUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;

/**
 * @Title: LocalFileHandlerServiceImpl.java 
 * @Description：基于本地IO操作文件的业务层处理类 ，实现FileHandlerService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class FileLocalHandlerServiceImpl implements FileHandlerService {
	
	private static final Logger logger = LoggerFactory.getLogger(FileLocalHandlerServiceImpl.class);
	
    //文件分隔符
  	private static final String SEPARATOR= File.separator;
  	
  	@Resource
 	private FileBasicConfig fileBasicConfig;
  	
	@Resource
    private RedisUtil redisUtil;
  	
    @Override
	public ExecuteResult<UploadFileVo> uploadFile(MultipartFile file) {
	    try {
	    	if(file==null||file.getOriginalFilename()==null) {
	     		return ExecuteResult.error("文件或文件名称不能为空", null);
	     	}
		    //获取文件信息
	    	String root = getFileRootPath(fileBasicConfig);
		    long fileSize = file.getSize();
		    String realFileName = file.getOriginalFilename();
		    String suffix = realFileName.substring(realFileName.lastIndexOf(".")+1);//文件后缀
	        String newFileName = FileUtil.currentTimeMillis()+"."+suffix;//新文件名
	        String targetPath = suffix+ SEPARATOR + newFileName;///文件相对全路径（既不包含根路径的路径）
		    //存储文件
	        InputStream inputStream = file.getInputStream();
	        String filepath = root+targetPath;
	        File targetFile = new File(filepath);
	        File targetdir = targetFile.getParentFile();
	        if(!targetdir.exists()) {
	        	targetdir.mkdirs();
	        }
	        FileUtil.writeFromStream(inputStream, filepath);
	        //组织文件信息返回
    	    UploadFileVo fileInfo = new UploadFileVo();
    	    fileInfo.setFilePath(targetPath);
    	    fileInfo.setFileName(realFileName);
    	    fileInfo.setFileType(suffix);
    	    fileInfo.setFileSize(fileSize+"");
    	    return ExecuteResult.success("上传成功", fileInfo);
	    } catch (Exception e) {
	    	logger.error("基于本地IO操作上传文件【{}】出现异常，异常详情：", file.getName(), e);
	    	return ExecuteResult.error("上传失败,请联系管理员", null);
	    } 

	}

	  
	@Override
	public ExecuteResult<byte[]> downloadSmallFile(String filePath) {
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
	
	@Override
	public void downloadBigFile(HttpServletResponse response,String fileName, String filePath) {
		InputStream input = null;
		OutputStream output =null;
		try {
			File file = new File(getFileRootPath(fileBasicConfig)+filePath);
	    	input = new FileInputStream(file);
			//获取响应输出流
            response.reset();
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, Constants.UTF8));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding(Constants.UTF8);
            output = response.getOutputStream();
			if(input!=null) {
	            byte[] buffer = new byte[4096];
		        int n = 0;
		        while (-1 != (n = input.read(buffer))) {
		        	output.write(buffer, 0, n);
		        }
		        output.flush();
			}
		} catch (Exception e) {
			logger.error("基于本地IO操作下载目标文件【{}】出现异常，详情：", filePath, e);
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

	@Override
	public ExecuteResult<String> deleteFile(String filePath) {
	    try {
	    	if(filePath.lastIndexOf(".")<0) {
               return ExecuteResult.error("文件路径不合法！", null); 
	    	}
	    	String targetPath = getFileRootPath(fileBasicConfig) + filePath;
	        boolean result = FileUtil.delFile(targetPath);
	        if(result) {
	        	return ExecuteResult.success("删除目标文件成功");
	        }else {
	        	return ExecuteResult.error("删除目标文件失败", null);
	        }
        }catch (Exception e){
        	logger.error("基于本地IO操作删除目标文件【{}】出现异常，异常详情：", filePath, e);
        	return ExecuteResult.error("删除目标文件失败", null);
        }
	}
	
	
	@Override
	public ExecuteResult<ChunkTaskModel> uploadChunkFile(ChunkUploadParamsModel model) {
		InputStream ins = null;
	    OutputStream os = null;
	    String totalPath = "";
	    try {
	    	//参数校验
	    	String checkResult = checkChunkUploadParams(model);
	    	if (StringUtil.isNotEmpty(checkResult)) {
	            return ExecuteResult.error("分片上传参数校验不通过，详情："+checkResult,null);
	        }
	    	
	    	//获取缓存文件夹路径
	    	String cacheRootPath = getFileDownloadCachePath(fileBasicConfig);
	    	String relativePath = "cgfp"+SEPARATOR+model.getFileMd5();
	    	String absolutePath = cacheRootPath+SEPARATOR+relativePath;
	        //创建文件夹
	        File filedir = new File(absolutePath);
	        if(!filedir.exists()) {
	        	filedir.mkdirs();
	        }
	        //获取文件流
	        MultipartFile file = model.getUpfile();
	        totalPath = relativePath+SEPARATOR+model.getChunkNumber();
	        File targetFile = new File(cacheRootPath+SEPARATOR+totalPath);
	        if(targetFile.exists()) {
	        	targetFile.delete();
	        }
	        //进行文件写入
        	ins = file.getInputStream();
            os = new FileOutputStream(targetFile);
            int bytesRead = 0;
            byte[] buffer = new byte[2048];
            while ((bytesRead = ins.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            ChunkTaskModel taskModel = new ChunkTaskModel();
            taskModel.setId(UuidUtils.buildID("FP"));
            taskModel.setChunkNumber(model.getChunkNumber());
            taskModel.setChunkSize(model.getChunkSize());
            taskModel.setCurrentChunkSize(file.getSize());
            taskModel.setFilename(model.getFilename());
            taskModel.setTotalChunks(model.getTotalChunks());
            taskModel.setTotalSize(model.getTotalSize());
            taskModel.setFileMd5(model.getFileMd5());
            taskModel.setRelativePath(totalPath);
            taskModel.setCreateTime(new Date());
            taskModel.setUpdateTime(taskModel.getCreateTime());
            // 将分片进度信息存进redis备查，默认有效期24小时，以防后续未能成功合并清楚的情况
            redisUtil.sSetAndTime(model.getFileMd5(), 24*60*60, taskModel);
            return ExecuteResult.success("上传成功", taskModel);
	    } catch (Exception e) {
	    	logger.error("文件块写入【{}】异常，详情",totalPath,e);
	    } finally {
            try {
                if (null != os) {
                    os.close();
                }
                if (null != ins) {
                    ins.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
	    return ExecuteResult.error("上传失败", null);
	}
	
	@Override
	public ExecuteResult<List<ChunkTaskModel>> checkChunkUpload(String fileMd5) {
		
		try {
			Set<Object> tasks = redisUtil.sGet(fileMd5);//从redis里获取分片信息
			List<ChunkTaskModel> taskModels= JSON.parseArray(JSON.toJSONString(tasks), ChunkTaskModel.class);
			return ExecuteResult.success("获取分片数成功", taskModels);
		} catch (Exception e) {
			logger.error("根据文件md5值【{}】获取已上传的分片数信息出现异常，异常详情：",fileMd5, e);
			return ExecuteResult.error("获取分片数失败", null);
		}
		
	}

	@Override
	public ExecuteResult<Map<String, String>> mergeChunk(ChunkMergeParamsModel model) {
		RandomAccessFile in=null;
		RandomAccessFile out =null;
		String chunkPath = "";
		try {
			logger.info("文件分片合并参数【{}】",model);
			//参数校验
	    	String checkResult = checkMergeParams(model);
	    	if (StringUtil.isNotEmpty(checkResult)) {
	            return ExecuteResult.error("分片合并参数校验不通过，详情："+checkResult,null);
	        }
			
	    	//参数校验
	    	Set<Object> tasks = redisUtil.sGet(model.getFileMd5());
			if(tasks==null) {
				return ExecuteResult.error("根据文件md5值，未获取到上传信息", null);
			}
			if(tasks.size()!=model.getTotalChunks()) {
				return ExecuteResult.error("目标文件已上传的分片数与总分片数不一致，不能合并", null);
			}
			String mergeFileName = "";
			String fileSize = "";
			for(Object o:tasks) {
				ObjectMapper objectMapper=new ObjectMapper();
		        //转化为实体类ChunkTaskModel
				ChunkTaskModel task = objectMapper.convertValue(o, ChunkTaskModel.class);
				mergeFileName = task.getFilename()+"";
				chunkPath = task.getRelativePath()+"";
				fileSize = task.getTotalSize()+"";
				break;
			}
			String suffix = mergeFileName.substring(mergeFileName.lastIndexOf(".")+1);//文件后缀
	        String newFileName = FileUtil.currentTimeMillis()+"."+suffix;//新文件名
			//将文件合并，并放到目标位置
			String mergePath = chunkPath.substring(0, chunkPath.lastIndexOf(SEPARATOR)+1);
			String cacheRootPath = getFileDownloadCachePath(fileBasicConfig);//缓存根路径
			String rootPath = getFileRootPath(fileBasicConfig);//成果根路径
			String targetDirPath = rootPath + suffix;
			File dir = new File(targetDirPath);
			if(!dir.exists()){
				dir.mkdirs();
			}
			//合并后的存放文件路径
			String relativeFilePath = suffix+ SEPARATOR + newFileName;//相对路径
			String mergeFilePath = rootPath+relativeFilePath;//绝对路径
			out=new RandomAccessFile(mergeFilePath, "rw");
			for(int i=0;i<=model.getTotalChunks();i++) {
				for(Object o:tasks) {
					ObjectMapper objectMapper=new ObjectMapper();
			        //转化为实体类ChunkTaskModel
					ChunkTaskModel task = objectMapper.convertValue(o, ChunkTaskModel.class);
					int chunk = (int) task.getChunkNumber();
					if(i==chunk) {
						in=new RandomAccessFile(cacheRootPath+SEPARATOR+task.getRelativePath(), "r");
						int len=0;
					    byte[] bt=new byte[2048];
						while (-1!=(len=in.read(bt))) {
							out.write(bt, 0, len);
						}
						in.close();
						break;
					}
				}
			}
			
			File mergeFile = new File(mergeFilePath);
			if(!mergeFile.exists()) {
				throw new RuntimeException("获取合并后的文件失败");
			}
			//组织信息返回
			Map<String, String> rep = new HashMap<String, String>();
			rep.put("fileName", mergeFileName);//文件原名
  			rep.put("fileType", suffix);//文件扩展名
  			rep.put("fileSize", fileSize);//文件大小
  			rep.put("filePath", relativeFilePath);//相对路径不含定义的根路径(安全起见)
			//将本地分片数据进行删除
  			try {
				//删除缓存记录
				redisUtil.del(model.getFileMd5());
  				//将分片文件连同所在文件夹都删除
  				deleteChunkTempFile(new File(cacheRootPath+SEPARATOR+mergePath));
			} catch (Exception e) {
				logger.error("删除分片文件及记录失败，详情",e);
			}
			return ExecuteResult.success("合并成功", rep);
		} catch (Exception e) {
			logger.error("根据文件md5值【{}】进行文件合并出现异常，异常详情：",model.getFileMd5(), e);
			return ExecuteResult.error(e.getMessage(), null);
		}finally{
			try {
				if(in!=null){
					in.close();
				}
				if(out!=null){
					out.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
    //删除目标文件夹及其内文件
    private void deleteChunkTempFile(File file) throws IOException {
    	if(file.exists()&&file.isDirectory()&&file.getAbsolutePath().contains("cgfp")) {
    		File[] files = file.listFiles();
            //如果包含文件进行删除操作
            if (files!=null&&files.length>0){
                for (int i = 0; i <files.length; i++) {
                    if (files[i].isFile()){
                        //删除目标子文件
                    	files[i].delete();
                    }else if (files[i].isDirectory()){
                        //通过递归的方法找到子目录的文件
                    	deleteChunkTempFile(files[i]);
                    	logger.info("准备删除文件夹{}{}",files[i].getName(),files[i].listFiles()==null||files[i].listFiles().length<1);
                    	if(files[i].listFiles()==null||files[i].listFiles().length<1) {
                    		files[i].delete();//删除空文件夹
                    	}
                    }
                }
            }else {
            	//是空文件夹，则直接将文件夹删除
            	file.delete();
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
    
    /**
     * 获取分片数据本地存储根目录
     * @return
     */
    private String getFileDownloadCachePath(FileBasicConfig fileBasicConfig) {
    	String names= System.getProperty("os.name");
    	String baseFilePath;
    	 if(names.toLowerCase().startsWith("win")){
         	baseFilePath = fileBasicConfig.getLocalCachePathWin();
         }else{
         	baseFilePath = fileBasicConfig.getLocalCachePathLin();
         }
    	 return baseFilePath;
    }
    
    
    /**
     * 文件分片
     * @param file 要分片的文件
     * @param chunkFolder 分片目录
     * @param size 分片大小
     */
    public static void chunk(File file,String chunkFolder,long size) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos;
            FileChannel inputChannel = fis.getChannel();
            FileChannel outputChannel;
            if (!cn.hutool.core.io.FileUtil.exist(chunkFolder)) {
                cn.hutool.core.io.FileUtil.mkdir(chunkFolder);
            }
            // 分片总数
            int chunks = (int) Math.ceil(cn.hutool.core.io.FileUtil.size(file) / size);
            long startPoint = 0;
            for (int i = 0; i <= chunks; i++) {
                String chunkPath = chunkFolder + File.separator + i;
                File chunkFile = new File(chunkPath);
                boolean newFile = chunkFile.createNewFile();
                fos = new FileOutputStream(chunkPath);
                outputChannel = fos.getChannel();
                inputChannel.transferTo(startPoint,size,outputChannel);
                startPoint += size;
                outputChannel.close();
                fos.close();
            }
            inputChannel.close();
            fis.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    //校验分片上传接口接口的参数
    private String checkChunkUploadParams(ChunkUploadParamsModel model) {
        StringBuilder checkInfo = new StringBuilder("");
        if (StringUtil.isEmpty(model)) {
            checkInfo.append("ChunkUploadParamsModel不能为空;");
            return checkInfo.toString();
        }
        if (StringUtil.isEmpty(model.getChunkNumber())) {
            checkInfo.append("当前文件块编号【chunkNumber】不能为空;");
        }
        if (StringUtil.isEmpty(model.getChunkSize())) {
            checkInfo.append("分片大小【chunkSize】不能为空;");
        }
        if (StringUtil.isEmpty(model.getFilename())) {
            checkInfo.append("进行分片的文件名称【filename】不能为空;");
        }
        if (StringUtil.isEmpty(model.getFileMd5())) {
            checkInfo.append("文件md5标识【fileMd5】不能为空;");
        }
        if (StringUtil.isEmpty(model.getTotalChunks())) {
            checkInfo.append("总分片数【totalChunks】不能为空;");
        }
        if (StringUtil.isEmpty(model.getTotalSize())) {
            checkInfo.append("文件总大小【totalSize】不能为空;");
        }
        if (StringUtil.isEmpty(model.getUpfile())) {
            checkInfo.append("文件块文件【upfile】不能为空;");
        }
        return checkInfo.toString();
    }
    
    //校验分片上传接口接口的参数
    private String checkMergeParams(ChunkMergeParamsModel model) {
        StringBuilder checkInfo = new StringBuilder("");
        if (StringUtil.isEmpty(model)) {
            checkInfo.append("ChunkMergeParamsModel不能为空;");
            return checkInfo.toString();
        }
        if (StringUtil.isEmpty(model.getFileMd5())) {
            checkInfo.append("文件md5标识【fileMd5】不能为空;");
        }
        if (StringUtil.isEmpty(model.getTotalChunks())) {
            checkInfo.append("总分片数【totalChunks】不能为空;");
        }
        return checkInfo.toString();
    }
}
