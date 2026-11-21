package com.santech.module.biz.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.dao.MappingTemplateDao;
import com.santech.core.framework.entity.model.MappingLogModel;
import com.santech.core.framework.entity.model.MappingTemplateModel;
import com.santech.core.framework.entity.view.UploadFileVo;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.FileUtil;
import com.santech.framework.utils.tool.RequestUtil;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.MappingLogAddDto;
import com.santech.module.biz.entity.dto.MappingLogQueryDto;
import com.santech.module.biz.entity.dto.MappingTemplateAddDto;
import com.santech.module.biz.entity.dto.MappingTemplateResourceQueryDto;
import com.santech.module.biz.entity.dto.MappingTemplateUpdateDto;
import com.santech.module.biz.service.MappingTemplateService;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class MappingTemplateServiceImpl implements MappingTemplateService {
	private static final  Logger logger = LoggerFactory.getLogger(MappingTemplateServiceImpl.class);
	
	@Resource
 	private FileBasicConfig fileBasicConfig;
	
    @Resource
    private MappingTemplateDao mappingTemplateResourceDao;
    
	@Override
	public ExecuteResult<List<MappingTemplateModel>> queryMappingTemplateResourceInfosWithPage(MappingTemplateResourceQueryDto query) {
		logger.info("查询专题出图模板资源列表信息，入参：【{}】", query);
        
        String loginUser = query.getLoginUser();
        try {
        	if(StringUtil.isEmpty(loginUser)) {
        		return ExecuteResult.error("当前登录人不能为空", null);
            }
        	PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            String otherCondition = "and cjr = '"+loginUser+"'";
            if(StringUtil.isEmpty(query.getEq_isshare())) {
            	//默认查询当前登录人创建的以及他人共享的
            	otherCondition = otherCondition +" or isshare = '1'";
            }
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"cjsj desc",otherCondition);
            //根据拼接sql,调用dao层获取数据
            return mappingTemplateResourceDao.queryObjectsWithPageInfo(MappingTemplateModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询专题出图模板资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveMappingTemplateResourceInfo(MappingTemplateAddDto dto) {
		try {
   			String checkErr = checkMappingTemplateResourceInfoForAdd(dto);
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("name",dto.getName());
   		    List<MappingTemplateModel> apiModels = mappingTemplateResourceDao.queryObjects(MappingTemplateModel.class,"NAME=:name",paramsMap);
   		    if (ObjectUtil.isNotEmpty(apiModels)&&apiModels.size()>0) {
   		    	return ExecuteResult.error("专题出图模板资源名称不能重复！", null);
   		    }
   		    
   		    MappingTemplateModel model = new MappingTemplateModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("TMT"));
   			model.setCjsj(new Date());
   			model.setZhgxr(model.getCjr());
   			model.setZhgxsj(model.getCjsj());
   			//model.setContent(null);
   			int i = mappingTemplateResourceDao.insertMappingTemplateResourceInfo(model);
   			if(i>0) {
   				//更新空间元数据
   				/*JSONObject  content = dto.getContent();
   	   			if(content!=null) {
   	   				String updateSql = "update T_ZY_THEMATICMAPPING_TEMPLATE set CONTENT = '"+content+"' where ID = '"+model.getId()+"'";
   	   				mappingTemplateResourceDao.updateMappingTemplateResourceInfoBySql(updateSql);
   	   			}*/
   	   			return ExecuteResult.success("专题出图模板资源添加成功！");
   			}else {
   				return ExecuteResult.success("专题出图模板资源添加失败！");
   			}
   		} catch (Exception e) {
   			logger.error("新增专题出图模板资源异常，详情：",e);
   			throw new RuntimeException("新增专题出图模板资源异常，请稍后再试"); 
   		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> updateMappingTemplateResourceInfo(MappingTemplateUpdateDto dto) {
		try {
   			String templateId = dto.getId();
   			if(StringUtils.isEmpty(templateId)) {
       			return ExecuteResult.error("专题出图模板资源ID不能为空！", null);
       		}
   			if(StringUtil.isEmpty(dto.getZhgxr())) {
   				return ExecuteResult.error("更新人不能为空！", null);
   	 		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("templateId",templateId);
   		    MappingTemplateModel originMappingTemplateResource = mappingTemplateResourceDao.queryObject(MappingTemplateModel.class,"ID=:templateId",paramsMap);
   		    if (StringUtil.isEmpty(originMappingTemplateResource)) {
   		    	return ExecuteResult.error("未找到对应专题出图模板资源项，无法完成更新！", null);
   		    }
   		   if(!originMappingTemplateResource.getCjr().equalsIgnoreCase(dto.getZhgxr())) {
				return ExecuteResult.error("模版创建人与当前登录人不符，不能执行更新!", null);
			}
   		    //检查专题出图模板资源是否包含重名
   		    if(StringUtil.isNotEmpty(dto.getName())&&!(dto.getName().equals(originMappingTemplateResource.getName()))) {
   			    paramsMap.put("name",dto.getName());
   			    List<MappingTemplateModel> templateModels = mappingTemplateResourceDao.queryObjects(MappingTemplateModel.class,"ID!=:templateId and NAME=:name",paramsMap);
   			    if (StringUtil.isNotEmpty(templateModels)&&templateModels.size()>0) {
   			    	return ExecuteResult.error("要更新的专题出图模板资源名称已占用！", null);
   			    }
   		    }
   		    /*JSONObject  content = dto.getContent();
   		    dto.setContent(null);*/
   		    MappingTemplateModel resource = getMappingTemplateResourceModelByUpdateInfo(originMappingTemplateResource,dto);
   		    mappingTemplateResourceDao.updateMappingTemplateResourceInfo(resource);
   		    /*if(content!=null) {
		    	String updateSql = "update T_ZY_THEMATICMAPPING_TEMPLATE set CONTENT = '"+content+"' where ID = '"+templateId+"'";
		    	mappingTemplateResourceDao.updateMappingTemplateResourceInfoBySql(updateSql);
		    }*/
   			return ExecuteResult.success("专题出图模板资源更新成功！");
   		} catch (Exception e) {
   			logger.error("更新专题出图模板资源异常，详情：",e);
   			throw new RuntimeException("更新专题出图模板资源异常，请稍后再试"); 
   		}
	}

	@Override
	public ExecuteResult<String> deleteMappingTemplateResourceInfo(String loginUser,String templateId) {
		try {
   			if (StringUtil.isEmpty(templateId)) {
   				return ExecuteResult.error("专题出图模板资源ID入参不能为空值", null);
   			}
   			logger.info("删除专题出图模板资源信息，记录id【{}】", templateId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("templateId", templateId);
   			MappingTemplateModel targetModel = mappingTemplateResourceDao.queryObject(MappingTemplateModel.class,"ID=:templateId", paramsOne);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定专题出图模板资源信息", templateId);
   				return ExecuteResult.error("已无对应专题出图模板资源信息!", null);
   			}
   			if(!targetModel.getCjr().equalsIgnoreCase(loginUser)) {
   				return ExecuteResult.error("模版创建人与当前登录人不符，不能执行删除!", null);
   			}
   			int count1 = mappingTemplateResourceDao.deleteMappingTemplateResourceInfoById(templateId);
   			if (count1> 0) {
   				return ExecuteResult.success("删除成功");
   			}else {
   				return ExecuteResult.error("删除专题出图模板资源失败",null);
   			}
   		} catch (Exception e) {
   			logger.error("【{}】删除专题出图模板资源【{}】出现异常，原因：【{}】,详情：",loginUser, templateId,e.getMessage(), e);
   			return ExecuteResult.error("删除失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	
	
	 @Override
	 public ExecuteResult<UploadFileVo> symbolFilesUpload(MultipartFile file,String typeName) {
		    try {
		    	if(file==null||file.getOriginalFilename()==null) {
		     		return ExecuteResult.error("文件或文件名称不能为空", null);
		     	}
			    //获取文件信息
			    long fileSize = file.getSize();
			    String realFileName = file.getOriginalFilename();
			    String suffix = realFileName.substring(realFileName.lastIndexOf(".")+1);//文件后缀
			    if(!"JPG,PNG,JPEG,jpg,png,jpeg".contains(suffix)) {
			    	throw new RuntimeException("不支持的图片格式（目前支持：jpg,png,jpeg）");
			    }
		        String newFileName = FileUtil.currentTimeMillis()+"."+suffix;//新文件名
		        String targetPath = typeName+ File.separator + newFileName;///文件相对全路径（既不包含根路径的路径）
			    //存储文件
		        InputStream inputStream = file.getInputStream();
		        String filepath = getFileRootPath(fileBasicConfig)+"mapsymbol"+File.separator+targetPath;
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
	public ExecuteResult<List<String>> getFilePathInfoByFolder(String folderName) {
       try {
    	   List<String> symbols = new ArrayList<String>();
    	   String fileDirPath = getFileRootPath(fileBasicConfig)+"mapsymbol"+File.separator+folderName;
    	   File fileDir = new File(fileDirPath);
    	   if(fileDir.exists()) {
    		   File[] files = fileDir.listFiles();
    		   for(int i=0;i<files.length;i++) {
    			   File file = files[i];
    			   if(file.isFile()) {
    				   symbols.add("mapsymbol"+File.separator+folderName+File.separator+file.getName());
    			   }
    		   }
    	   }
	       return ExecuteResult.success("获取成功", symbols);
       } catch (Exception e) {
           logger.error("根据文件夹名称【{}】获取其内文件路径出现异常，详情：",folderName,e);
           return ExecuteResult.error("获取失败", null);
       }
	}

	@Override
	public ExecuteResult<String> deleteSymbolFileByFilePath(String symbolFilePath) {
		try {
	    	if(symbolFilePath.lastIndexOf(".")<0) {
               return ExecuteResult.error("文件路径不合法！", null); 
	    	}
	    	String filePath = getFileRootPath(fileBasicConfig)+symbolFilePath;
	        File file=new File(filePath);
	        if (file.exists()){
	            boolean result = file.delete();
	            if(result) {
	 	        	return ExecuteResult.success("删除目标文件成功");
	 	        }else {
	 	        	return ExecuteResult.error("删除目标文件失败", null);
	 	        }
	        }else{
	            return ExecuteResult.error("文件不存在,删除失败", null);
	        }
        }catch (Exception e){
        	logger.error("基于本地IO操作删除目标文件【{}】出现异常，异常详情：", symbolFilePath, e);
        	return ExecuteResult.error("删除目标文件失败", null);
        }
	}
	
	
	 //获取目标更新的专题出图模板资源信息
   	private MappingTemplateModel getMappingTemplateResourceModelByUpdateInfo(MappingTemplateModel origin,MappingTemplateUpdateDto update) {
   			
   		 if(StringUtil.isNotEmpty(update.getName())) {
   			origin.setName(update.getName());
   		 }
   		 if(StringUtil.isNotEmpty(update.getContent())) {
   			origin.setContent(update.getContent());
   		 }
   		 if(StringUtil.isNotEmpty(update.getIsShare())) {
   			origin.setIsShare(update.getIsShare());
   		 }
   		 if(StringUtil.isNotEmpty(update.getZhgxr())) {
   			origin.setZhgxr(update.getZhgxr());
    	 }
   		 origin.setZhgxsj(new Date());
   		return origin;
   	}
     //校验新增的专题出图模板资源信息
   	 private String checkMappingTemplateResourceInfoForAdd(MappingTemplateAddDto model) throws Exception {
   		 StringBuilder checkInfo = new StringBuilder("");
   		 if(StringUtil.isEmpty(model)) {
   			 checkInfo.append("MappingTemplateAddDto不能为空;");
   			 return checkInfo.toString();
   		 }
   		 if(StringUtil.isEmpty(model.getName())) {
   	        checkInfo.append("专题出图模板资源名称【name】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getIsShare())) {
   			 //若为空，默认设置为不共享
   			model.setIsShare("0");
   		 }
   		 if(StringUtil.isEmpty(model.getContent())) {
    	    checkInfo.append("专题出图模板配置信息【content】不能为空;");
    	 }
   		 if(StringUtil.isEmpty(model.getCjr())) {
   			 checkInfo.append("专题出图模板资源添加人【cjr】不能为空;");
   		 }
   		 return checkInfo.toString();
   		 
   	 }
   	 
   	@Override
	public ExecuteResult<List<MappingLogModel>> queryLogInfos(MappingLogQueryDto query){
		logger.info("查询出图日志列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
        	String otherSql = "";
        	if(StringUtil.isNotEmpty(query.getStartTime())) {
        		if(StringUtil.isEmpty(query.getEndTime())) {
        			query.setEndTime(CommonUtils.getFormatDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
        		}
        		String startTime = query.getStartTime();
        		String endTime = query.getEndTime();
        		String timeSQL = String.format("and CREATETIME >= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') and  CREATETIME <= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') ",startTime,endTime);
        		otherSql = otherSql+timeSQL;
        	}
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"CREATETIME desc",otherSql);
            //根据拼接sql,调用dao层获取数据
            return mappingTemplateResourceDao.queryObjectsWithPageInfo(MappingLogModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询出图日志列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}

	@Override
	public ExecuteResult<String> saveLogInfo(MappingLogAddDto dto) {
		try {
			//校验参数
			String checkErr =  checkMappingLogInfoForAdd(dto);
			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("出图日志保存入参校验不通过，详情："+checkErr, null);
       		}
			//数据保存
			MappingLogModel model = new MappingLogModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("ML"));
   		    model.setIpAddress(CommonUtils.getIpAddr(RequestUtil.getHttpRequest()));
			model.setCreateTime(new Date());
			mappingTemplateResourceDao.insertLog(model);
			return ExecuteResult.success("保存成功");
		} catch (Exception e) {
			logger.error("新增出图日志信息出现异常，入参【{}】,详情：",dto,e);
			return ExecuteResult.error("新增出图日志信息失败", null);
		}
	}
	
	//校验新增的专题出图的日志参数信息
  	 private String checkMappingLogInfoForAdd(MappingLogAddDto model) throws Exception {
  		 StringBuilder checkInfo = new StringBuilder("");
  		 if(StringUtil.isEmpty(model)) {
  			 checkInfo.append("MappingLogAddDto不能为空;");
  			 return checkInfo.toString();
  		 }
  		 if(StringUtil.isEmpty(model.getUserName())) {
  	        checkInfo.append("用户名【userName】不能为空;");
  	     }
  		 if(StringUtil.isEmpty(model.getTopic())) {
  	        checkInfo.append("出图主题【topic】不能为空;");
  	     }
  		 if(StringUtil.isEmpty(model.getContent())) {
 			checkInfo.append("专题出图模板配置信息【content】不能为空;");
 		 }
  		 if(StringUtil.isEmpty(model.getStatus())) {
 			 //若为空，默认设置为出图成功
 			model.setStatus("S");
 		 }
  		 return checkInfo.toString();
  		 
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
