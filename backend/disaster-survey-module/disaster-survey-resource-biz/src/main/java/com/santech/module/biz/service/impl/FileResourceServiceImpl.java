package com.santech.module.biz.service.impl;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.santech.core.framework.dao.FileResourceDao;
import com.santech.core.framework.entity.model.JcDictionaryDetailModel;
import com.santech.core.framework.entity.model.ZyFileDetailModel;
import com.santech.core.framework.entity.model.ZyFileModel;
import com.santech.core.framework.service.FileHandlerService;
import com.santech.core.framework.service.MdDictionaryManagerService;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.FileAddDto;
import com.santech.module.biz.entity.dto.FileDeleteDto;
import com.santech.module.biz.entity.dto.FileDetailAddDto;
import com.santech.module.biz.entity.dto.FileDetailsUpdateDto;
import com.santech.module.biz.entity.dto.FileResourceQueryDto;
import com.santech.module.biz.entity.dto.FileUpdateDto;
import com.santech.module.biz.entity.vo.ResourceStatisticsResultVo;
import com.santech.module.biz.service.FileResourceService;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class FileResourceServiceImpl implements FileResourceService {
	private static final  Logger logger = LoggerFactory.getLogger(FileResourceServiceImpl.class);
	
    @Resource
    private FileResourceDao fileResourceDao;
    @Resource
    private FileHandlerService fileHandlerService;
    @Resource
    private MdDictionaryManagerService mdDictionaryManagerService;

	@Override
	public ExecuteResult<List<ZyFileModel>> queryFileResourceInfosWithPage(FileResourceQueryDto query) {
		logger.info("查询文件资源列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            String otherCondition = "";
            if(!"1".equals(query.getAdminFlag())) {
            	//非管理员只能看开放的
            	otherCondition = " and STATUS='0' ";//文件资源状态，0开放，1不开放
            }
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"cjsj desc",otherCondition);
            //根据拼接sql,调用dao层获取数据
            return fileResourceDao.queryObjectsWithPageInfo(ZyFileModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询文件资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}

	@Override
	public ExecuteResult<ZyFileModel> queryFileDetailInfosById(String fileResourceId) {
		try {
   			if (StringUtil.isEmpty(fileResourceId)) {
   				return ExecuteResult.error("文件资源ID入参不能为空值", null);
   			}
   			logger.info("根据ID查询文件资源详情信息，记录id【{}】", fileResourceId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("fileResourceId", fileResourceId);
   			ZyFileModel targetModel = fileResourceDao.queryObject(ZyFileModel.class,"ID=:fileResourceId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应文件资源信息!", null);
   			}
   			List<ZyFileDetailModel> details = fileResourceDao.queryObjects(ZyFileDetailModel.class,"FRID=:fileResourceId", paramsOne);
   			targetModel.setDetails(details);
   			return ExecuteResult.success("查询成功",targetModel);
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询文件资源详情信息出现异常，原因：【{}】,详情：", fileResourceId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveFileResourceInfo(FileAddDto dto) {
		try {
   			String checkErr = checkFileResourceInfoForAdd(dto) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("name",dto.getName());
   		    List<ZyFileModel> fileResourcemodels = fileResourceDao.queryObjects(ZyFileModel.class,"NAME=:name",paramsMap);
   		    if (ObjectUtil.isNotEmpty(fileResourcemodels)&&fileResourcemodels.size()>0) {
   		    	return ExecuteResult.error("文件资源名称不能重复！", null);
   		    }
   		    ZyFileModel model = new ZyFileModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("FR"));
   			model.setCjsj(new Date());
   			model.setZhgxr(model.getCjr());
   			model.setZhgxsj(model.getCjsj());
   			
   			//保存子文件信息
   			List<FileDetailAddDto> details =dto.getDetails();
   			for(FileDetailAddDto detailDto : details) {
   				ZyFileDetailModel detail = new ZyFileDetailModel();
   				BeanUtils.copyProperties(detailDto, detail);
   				detail.setId(UuidUtils.buildID("F"));
   				detail.setFrid(model.getId());
   				detail.setCjr(model.getCjr());
   				detail.setCjsj(new Date());
   				fileResourceDao.insertFileDetailInfo(detail);
   			}
   			fileResourceDao.insertFileResourceInfo(model);
   			return ExecuteResult.success("文件资源添加成功！");
   		} catch (Exception e) {
   			logger.error("新增文件资源异常，详情：",e);
   			//删除上传到磁盘的文件信息
   			try {
   				List<FileDetailAddDto> details = dto.getDetails();
   				if(details!=null&&details.size()>0) {
   					for(FileDetailAddDto detailDto : details) {
   	   	   				//删除对应磁盘文件
   	   					ExecuteResult<String> deleteResult = fileHandlerService.deleteFile(detailDto.getFilePath());
   	   					if("F".equalsIgnoreCase(deleteResult.getResultCode())) {
   	   						logger.error("删除关联磁盘物理文件【"+detailDto.getFilePath()+"】失败");
   	   					}
   	   	   			}
   				}
			} catch (Exception e2) {
				logger.error("删除磁盘物理文件出现异常，详情：",e2);
			}
   			throw new RuntimeException("新增文件资源异常，请稍后再试"); 
   		}
	}

	@Override
	public ExecuteResult<String> updateFileResourceInfo(FileUpdateDto dto) {
		try {
   			String fileResourceId = dto.getId();
   			if(StringUtils.isEmpty(fileResourceId)) {
       			return ExecuteResult.error("文件资源ID不能为空！", null);
       		}
   			if(StringUtil.isEmpty(dto.getZhgxr())) {
   				return ExecuteResult.error("更新人不能为空！", null);
   	 		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("fileResourceId",fileResourceId);
   		    ZyFileModel originFileResource = fileResourceDao.queryObject(ZyFileModel.class,"ID=:fileResourceId",paramsMap);
   		    if (StringUtil.isEmpty(originFileResource)) {
   		    	return ExecuteResult.error("未找到对应文件资源项，无法完成更新！", null);
   		    }
   		    //检查文件资源是否包含重名
   		    if(StringUtil.isNotEmpty(dto.getName())&&!(dto.getName().equals(originFileResource.getName()))) {
   			    paramsMap.put("name",dto.getName());
   			    List<ZyFileModel> fileResourceModels = fileResourceDao.queryObjects(ZyFileModel.class,"ID!=:fileResourceId and NAME=:name",paramsMap);
   			    if (StringUtil.isNotEmpty(fileResourceModels)&&fileResourceModels.size()>0) {
   			    	return ExecuteResult.error("要更新的文件资源名称已占用！", null);
   			    }
   		    }
   		    ZyFileModel resource = getFileResourceModelByUpdateInfo(originFileResource,dto);
   		    fileResourceDao.updateFileResourceInfo(resource);
   			return ExecuteResult.success("文件资源更新成功！");
   		} catch (Exception e) {
   			logger.error("更新文件资源异常，详情：",e);
   			return ExecuteResult.error("更新文件资源异常，请稍后再试", null);
   		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> deleteFileResourceInfo(FileDeleteDto model) {
		String fileResourceId = model.getFileResourceId();
		try {
   			if (StringUtil.isEmpty(fileResourceId)) {
   				return ExecuteResult.error("文件资源ID入参不能为空值", null);
   			}
   			logger.info("删除文件资源信息，记录id【{}】", fileResourceId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("fileResourceId", fileResourceId);
   			ZyFileModel targetModel = fileResourceDao.queryObject(ZyFileModel.class,"ID=:fileResourceId", paramsOne);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定文件资源信息", fileResourceId);
   				return ExecuteResult.error("已无对应文件资源信息!", null);
   			}
   			int count1 = fileResourceDao.deleteFileResourceInfoById(fileResourceId);
   			if (count1> 0) {
   				//调用文件删除接口，对磁盘文件进行删除
   				List<ZyFileDetailModel> details = fileResourceDao.queryObjects(ZyFileDetailModel.class, "FRID=:fileResourceId", paramsOne);
   				if(details.size()>0) {
   	   				//循环删除磁盘关联的附件信息
   	   				for(ZyFileDetailModel file:details) {
   	   					//删除文件记录
   	   					int count2 = fileResourceDao.deleteFileDetailInfoById(file.getId());
   	   					if(count2<1) {
   	   						throw new RuntimeException("删除文件资源关联文件记录【"+file.getId()+"】失败");
   	   					}
   	   					//删除对应磁盘文件
   	   					ExecuteResult<String> deleteResult = fileHandlerService.deleteFile(file.getFilePath());
   	   					if("F".equalsIgnoreCase(deleteResult.getResultCode())) {
   	   						throw new RuntimeException("删除文件资源关联磁盘物理文件【"+file.getFilePath()+"】失败");
   	   					}
   	   				}
   				}
   				return ExecuteResult.success("删除成功");
   			}else {
   				throw new RuntimeException("删除文件资源失败");
   			}
   		} catch (Exception e) {
   			logger.error("【{}】删除文件资源【{}】出现异常，原因：【{}】,详情：",model.getLoginUser(), fileResourceId,e.getMessage(), e);
   			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
   		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> updateFileResourceDetailsInfo(FileDetailsUpdateDto dto) {
		try {
   			String checkErr = checkFileResourceDetailInfoForAdd(dto) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			String fileResourceId = dto.getId();
   			Map<String, Object> params = new HashMap<String, Object>();
   			params.put("fileResourceId", fileResourceId);
   			ZyFileModel targetModel = fileResourceDao.queryObject(ZyFileModel.class,"ID=:fileResourceId", params);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定文件资源信息", fileResourceId);
   				return ExecuteResult.error("未能查到指定文件资源信息,不能为其添加附件!", null);
   			}
   			//保存子文件信息
   			List<FileDetailAddDto> details = dto.getDetails();
   			for(FileDetailAddDto detailDto : details) {
   				ZyFileDetailModel detail = new ZyFileDetailModel();
   				BeanUtils.copyProperties(detailDto, detail);
   				detail.setId(UuidUtils.buildID("F"));
   				detail.setFrid(fileResourceId);
   				detail.setCjr(dto.getCjr());
   				detail.setCjsj(new Date());
   				fileResourceDao.insertFileDetailInfo(detail);
   			}
   			return ExecuteResult.success("文件资源添加附件成功！");
   		} catch (Exception e) {
   			logger.error("文件资源新增附件异常，详情：",e);
   			//删除上传到磁盘的文件信息
   			try {
   				List<FileDetailAddDto> details = dto.getDetails();
   				if(details!=null&&details.size()>0) {
   					for(FileDetailAddDto detailDto : details) {
   	   	   				//删除对应磁盘文件
   	   					ExecuteResult<String> deleteResult = fileHandlerService.deleteFile(detailDto.getFilePath());
   	   					if("F".equalsIgnoreCase(deleteResult.getResultCode())) {
   	   						logger.error("删除关联磁盘物理文件【"+detailDto.getFilePath()+"】失败");
   	   					}
   	   	   			}
   				}
			} catch (Exception e2) {
				logger.error("删除磁盘物理文件出现异常，详情：",e2);
			}
   			throw new RuntimeException("文件资源新增附件异常，请稍后再试",null); 
   		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> deleteFileDetailInfo(FileDeleteDto model) {
		String fileDetailIds = model.getFileDetailIds();
		try {
   			if (StringUtil.isEmpty(model.getFileResourceId())) {
   				return ExecuteResult.error("文件资源ID入参不能为空值", null);
   			}
   			if (StringUtil.isEmpty(fileDetailIds)) {
   				return ExecuteResult.error("具体文件ID入参不能为空值", null);
   			}
   			logger.info("删除具体文件记录信息，记录id【{}】", fileDetailIds);
   			String[] fileIds = fileDetailIds.split(",");
   			//T调用文件删除接口，对磁盘文件进行删除
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			for(int i=0;i<fileIds.length;i++) {
   				//删除文件记录
   				String fileId = fileIds[i];
   				paramsOne.put("fileId",fileId);
   				ZyFileDetailModel file = fileResourceDao.queryObject(ZyFileDetailModel.class, "ID=:fileId", paramsOne);
   				if(file!=null&&StringUtil.isNotEmpty(file.getId())) {
   					int count = fileResourceDao.deleteFileDetailInfoById(fileId);
   					if(count<1) {
   						throw new RuntimeException("删除文件记录【"+fileId+"】失败");
   					}
   					//删除对应磁盘文件
   					ExecuteResult<String> deleteResult = fileHandlerService.deleteFile(file.getFilePath());
   					if("F".equalsIgnoreCase(deleteResult.getResultCode())) {
   						throw new RuntimeException("删除关联磁盘物理文件【"+file.getFilePath()+"】失败");
   					}
   				}
   			}
   			return ExecuteResult.success("删除成功", null);
   		} catch (Exception e) {
   			logger.error("【{}】删除具体文件记录【{}】出现异常，原因：【{}】,详情：",model.getLoginUser(), fileDetailIds,e.getMessage(), e);
   			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
   		}
	}
	
	@Override
	public ExecuteResult<List<ResourceStatisticsResultVo>> statisticsFileResourceForLeftTab(String adminFlag,String groupColumn,String dicType) {
		try {
			if(StringUtil.isEmpty(groupColumn)) {
				return ExecuteResult.error("统计字段不能为空值", null);
			}
			if(StringUtil.isEmpty(dicType)) {
				return ExecuteResult.error("字典类型不能为空值", null);
			}
			String whereSql = " where 1=1";
			if(!"1".equals(adminFlag)) {
            	//非管理员只能看开放的
				whereSql += " and STATUS='0' ";//文件资源状态，0开放，1不开放
            }
			List<ResourceStatisticsResultVo> result = new ArrayList<ResourceStatisticsResultVo>();
			//整体数量统计
			int countId = 1;
			ResourceStatisticsResultVo statisticResult = new ResourceStatisticsResultVo();
			statisticResult.setNodeName("全部文件资源");
			statisticResult.setNodeId("F"+countId);
			statisticResult.setPid("-1");
			statisticResult.setNodeCode("");
			statisticResult.setNodeType("root");
			String totalQuerySql = "select count(f.id) as RESULT from T_ZY_FILERESOURCE f "+whereSql;
			List<Map<String, Object>> totalResult =  fileResourceDao.selectFileResourceInfos(totalQuerySql, null);
			if(totalResult!=null&&totalResult.size()>0) {
				statisticResult.setStatisticResult(Integer.parseInt(String.valueOf(totalResult.get(0).get("RESULT"))));
			}else {
				statisticResult.setStatisticResult(0);
			}
			//对子项进行统计
			//获取字典信息
			ExecuteResult<List<JcDictionaryDetailModel>> dicResult = mdDictionaryManagerService.queryDictionaryDetailInfo("", dicType);
			List<JcDictionaryDetailModel> dicts = dicResult.getData();
			List<ResourceStatisticsResultVo> models = new ArrayList<ResourceStatisticsResultVo>();
   			if("S".equalsIgnoreCase(dicResult.getResultCode())&&dicts!=null&&dicts.size()>0) {
   				String mainQuerySql = "select count(f.id) as statisticResult,"+groupColumn+" as nodeCode  from T_ZY_FILERESOURCE f";
   				String groupSql = " group by "+groupColumn;
   				String allSql = mainQuerySql + whereSql + groupSql;
   				List<Map<String, Object>> statisticsResult =  fileResourceDao.selectFileResourceInfos(allSql, null);
   				if(statisticsResult!=null&&statisticsResult.size()>0) {
   					List<ResourceStatisticsResultVo> results = JSONArray.parseArray(JSON.toJSONString(statisticsResult), ResourceStatisticsResultVo.class);
   					for(JcDictionaryDetailModel dict:dicts) {
   						boolean hastype = false;
   						for(ResourceStatisticsResultVo model:results) {
   							if(dict.getZdbm().equalsIgnoreCase(model.getNodeCode())) {
   								model.setPid(statisticResult.getNodeId());
   								countId++;
   								model.setNodeId("F"+countId);
   								model.setNodeName(dict.getZdz());
   								model.setNodeType("leaf");
   								hastype = true;
   								models.add(model);
   								break;
   							}
   	   					}
   						if(!hastype) {
   							ResourceStatisticsResultVo model = new ResourceStatisticsResultVo();
   							model.setPid(statisticResult.getNodeId());
							countId++;
							model.setNodeId("F"+countId);
   							model.setNodeName(dict.getZdz());
   							model.setNodeCode(dict.getZdbm());
   							model.setNodeType("leaf");
   							model.setStatisticResult(0);
   							models.add(model);
   						}
   					}
   				}
   			}
   			statisticResult.setChildren(models);
   			result.add(statisticResult);
   			return ExecuteResult.success("文件资源统计成功！",result);
   		} catch (Exception e) {
   			logger.error("文件资源统计异常，详情：",e);
   			return ExecuteResult.error("文件资源统计异常，请稍后再试", null);
   		}
	}
	
	 //获取目标更新的文件资源信息
   	private ZyFileModel getFileResourceModelByUpdateInfo(ZyFileModel origin,FileUpdateDto update) {
   			
   		 if(StringUtil.isNotEmpty(update.getName())) {
   			origin.setName(update.getName());
   		 }
   		 if(StringUtil.isNotEmpty(update.getType())) {
   			origin.setType(update.getType());
   		 }
   		 if(StringUtil.isNotEmpty(update.getLevel())) {
   			origin.setLevel(update.getLevel());
   		 }
   		 if(StringUtil.isNotEmpty(update.getStatus())) {
   			origin.setStatus(update.getStatus());
    	 }
   		 if(StringUtil.isNotEmpty(update.getDescription())) {
   			origin.setDescription(update.getDescription());
    	 }
   		 if(StringUtil.isNotEmpty(update.getProject())) {
   			origin.setProject(update.getProject());
    	 }
   		 if(StringUtil.isNotEmpty(update.getZhgxr())) {
   			origin.setZhgxr(update.getZhgxr());
    	 }
   		 origin.setZhgxsj(new Date());
   		return origin;
   	}
     //校验新增的文件资源信息
   	 private String checkFileResourceInfoForAdd(FileAddDto model) {
   		 StringBuilder checkInfo = new StringBuilder("");
   		 if(StringUtil.isEmpty(model)) {
   			 checkInfo.append("FileAddDto不能为空;");
   			 return checkInfo.toString();
   		 }
   		 if(StringUtil.isEmpty(model.getName())) {
   	        checkInfo.append("文件资源名称【name】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getType())) {
    	    checkInfo.append("文件资源类型【type】不能为空;");
    	 }
   		 if(StringUtil.isEmpty(model.getLevel())) {
   	        checkInfo.append("文件资源秘密等级【level】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getStatus())) {
   			model.setStatus("0");//默认开放
   	     }
   		 if(StringUtil.isEmpty(model.getDetails())) {
   	        checkInfo.append("文件资源包含的文件【details】不能为空;");
   	     }else {
   	    	for(FileDetailAddDto file:model.getDetails()) {
  	    		 if(StringUtil.isEmpty(file.getFilePath())) {
  	    			checkInfo.append("文件资源新上传的文件的路径信息【details.filePath】不能为空;"); 
  	    		 }
  	    		 if(StringUtil.isEmpty(file.getFileName())) {
  	    			checkInfo.append("文件资源新上传的文件的名称信息【details.fileName】不能为空;"); 
  	    		 }
  	    	 }
   	     }
   		 if(StringUtil.isEmpty(model.getCjr())) {
   			 checkInfo.append("文件资源添加人【cjr】不能为空;");
   		 }
   		 /*if(StringUtil.isEmpty(model.getProject())) {
  			 checkInfo.append("文件所属项目【project】不能为空;");
  		 }*/
   		 return checkInfo.toString();
   		 
   	 }
   	 
     //校验新增的文件资源信息
   	 private String checkFileResourceDetailInfoForAdd(FileDetailsUpdateDto model) {
   		 StringBuilder checkInfo = new StringBuilder("");
   		 if(StringUtil.isEmpty(model)) {
   			 checkInfo.append("FileDetailsUpdateDto不能为空;");
   			 return checkInfo.toString();
   		 }
   		 if(StringUtil.isEmpty(model.getId())) {
   	        checkInfo.append("文件资源ID【id】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getDetails())) {
   	        checkInfo.append("文件资源新上传的文件【details】不能为空;");
   	     }else {
   	    	 for(FileDetailAddDto file:model.getDetails()) {
   	    		 if(StringUtil.isEmpty(file.getFilePath())) {
   	    			checkInfo.append("文件资源新上传的文件的路径信息【details.filePath】不能为空;"); 
   	    		 }
   	    		 if(StringUtil.isEmpty(file.getFileName())) {
   	    			checkInfo.append("文件资源新上传的文件的名称信息【details.fileName】不能为空;"); 
   	    		 }
   	    	 }
   	     }
   		 if(StringUtil.isEmpty(model.getCjr())) {
   			 checkInfo.append("文件资源添加人【cjr】不能为空;");
   		 }
   		 return checkInfo.toString();
   		 
   	 }

	

}
