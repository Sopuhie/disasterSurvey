package com.santech.core.framework.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.santech.core.framework.entity.view.ChunkMergeParamsModel;
import com.santech.core.framework.entity.view.ChunkTaskModel;
import com.santech.core.framework.entity.view.ChunkUploadParamsModel;
import com.santech.core.framework.entity.view.UploadFileVo;
import com.santech.framework.global.ExecuteResult;
/**
 * @Title: FileHandlerService.java 
 * @Description：文件业务层处理接口类 
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
public interface FileHandlerService {
	/**
     * @Title uploadFile
	 * @Description (此为单文件上传接口对应的业务层方法)
     * @param file(MultipartFile 上传的文件)
     * @return (接口的执行情况及文件信息)
     */
	ExecuteResult<UploadFileVo> uploadFile(MultipartFile file);
	
	/**
     * @Title deleteFile
	 * @Description (此为文件删除接口对应的业务层方法）
     * @param filePath(删除的目标文件路径)
     * @return (方法的执行情况，true删除成功，false删除失败)
     */
	ExecuteResult<String> deleteFile(String filePath);
	
	/**
     * @Title downloadSmallFile
	 * @Description (此为文件下载接口对应的业务层方法）
     * @param filePath(载的目标文件路径)
     * @return (方法的执行情况及二进制流)
     */
	ExecuteResult<byte[]> downloadSmallFile(String filePath);
	
	/**
     * @Title downloadSmallFile
	 * @Description (此为文件下载接口对应的业务层方法）
	 * @param response(响应)
	 * @param fileName(文件名称)
     * @param filePath(下载的目标文件路径)
     * @return 
     */
	void downloadBigFile(HttpServletResponse response,String fileName, String filePath) ;
	
	/**
     * @Title uploadChunkFile
	 * @Description (此为文件上传接口对应的业务层方法，将分片文件缓存到应用服务器指定目录)
     * @param model(文件分片信息)
     * @return (接口的执行情况及分片任务信息)
     */
	ExecuteResult<ChunkTaskModel> uploadChunkFile(ChunkUploadParamsModel model);
	
	
	
	/**
     * @Title checkChunkUpload
	 * @Description (此为检查文件分片上传进度的接口,根据文件的md5值获取已经上传的分片数信息)
     * @param fileMd5(文件唯一标识)
     * @return (已上传的分片信息)
     */
	ExecuteResult<List<ChunkTaskModel>> checkChunkUpload(String fileMd5);
	
	
	
	/**
     * @Title mergeChunk
	 * @Description (此为文件合并的接口,根据文件的md5值和总分片数进行文件合并形成目标文件并放到指定位置)
     * @param model(ChunkMergeParamsModel)
     * @return (已上传的分片信息)
     */
	ExecuteResult<Map<String, String>> mergeChunk(ChunkMergeParamsModel model);
}
