package com.santech.module.biz.service;

import org.springframework.web.multipart.MultipartFile;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.ShpFileInfoDto;

public interface ShpFileHandlerService {
	
	
	/**
     * @Title uploadFile
	 * @Description (此为Shp文件上传接口对应的业务层方法)
     * @param file(MultipartFile 上传的文件)
     * @return (接口的执行情况及文件信息)
     */
	ExecuteResult<ShpFileInfoDto> uploadShpFile(MultipartFile file);
	
	/**
     * @Title deleteFile
	 * @Description (此为Shp文件删除接口对应的业务层方法）
     * @param filePath(删除的目标文件路径)
     * @return (方法的执行情况，true删除成功，false删除失败)
     */
	ExecuteResult<String> deleteShpFile(String filePath);
	
	/**
     * @Title downloadSmallFile
	 * @Description (此为Shp文件下载接口对应的业务层方法）
     * @param filePath(下载的目标文件路径)
     * @return (方法的执行情况及二进制流)
     */
	ExecuteResult<byte[]> downloadShpFileByZip(String filePath);

}
