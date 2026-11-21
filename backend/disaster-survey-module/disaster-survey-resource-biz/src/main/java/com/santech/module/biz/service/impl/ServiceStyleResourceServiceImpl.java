package com.santech.module.biz.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.dao.ServiceStyleResourceDao;
import com.santech.core.framework.entity.model.ZyServiceStyleModel;
import com.santech.core.framework.entity.model.ZyServiceStyleSimpleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.ServiceStyleAddDto;
import com.santech.module.biz.entity.dto.ServiceStyleResourceQueryDto;
import com.santech.module.biz.entity.dto.ServiceStyleUpdateDto;
import com.santech.module.biz.service.ServiceStyleResourceService;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class ServiceStyleResourceServiceImpl implements ServiceStyleResourceService {
	private static final  Logger logger = LoggerFactory.getLogger(ServiceStyleResourceServiceImpl.class);
	
	
    @Resource
    private ServiceStyleResourceDao serviceStyleResourceDao;
    

	@Override
	public ExecuteResult<List<ZyServiceStyleSimpleModel>> queryServiceStyleResourceInfosWithPage(ServiceStyleResourceQueryDto query) {
		logger.info("查询服务样式资源列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            String otherCondition = "";
            if(!"1".equals(query.getAdminFlag())) {
            	//非管理员只能看可用的
            	otherCondition = " and STATUS='0' ";//样式资源状态，0可用，1不可用
            }
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"cjsj desc",otherCondition);
            //根据拼接sql,调用dao层获取数据
            return serviceStyleResourceDao.queryObjectsWithPageInfo(ZyServiceStyleSimpleModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询服务样式资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}

	@Override
   	public ExecuteResult<ZyServiceStyleModel> queryServiceStyleResourceInfoById(String serviceStyleId) {
       	try {
   			if (StringUtil.isEmpty(serviceStyleId)) {
   				return ExecuteResult.error("服务样式资源ID入参不能为空值", null);
   			}
   			logger.info("根据ID查询服务样式资源信息，记录id【{}】", serviceStyleId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("serviceStyleId", serviceStyleId);
   			ZyServiceStyleModel targetModel = serviceStyleResourceDao.queryObject(ZyServiceStyleModel.class,"ID=:serviceStyleId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应服务样式资源信息!", null);
   			}else {
   				return ExecuteResult.success("查询成功",targetModel);
   			}
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询服务样式资源信息出现异常，原因：【{}】,详情：", serviceStyleId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
   	}
	
	@Override
	public ExecuteResult<String> queryServiceStyleXmlInfoById(String serviceStyleId) {
		try {
   			if (StringUtil.isEmpty(serviceStyleId)) {
   				return ExecuteResult.error("服务样式资源ID入参不能为空值", null);
   			}
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("serviceStyleId", serviceStyleId);
   			ZyServiceStyleModel targetModel = serviceStyleResourceDao.queryObject(ZyServiceStyleModel.class,"ID=:serviceStyleId", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应服务样式资源记录信息!", null);
   			}else {
   				if("1".equals(targetModel.getStatus())) {
   					return ExecuteResult.error("对应服务样式资源已禁用，请联系管理员!", null);
   				}
   				String xmlContent = targetModel.getContent();
   				if(StringUtil.isNotEmpty(xmlContent)) {
   					return ExecuteResult.success("查询成功",xmlContent);
   				}else {
   					return ExecuteResult.error("对应服务样式资源暂未添加xml样式信息!", null);
   				}
   			}
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询服务样式XML信息出现异常，原因：【{}】,详情：", serviceStyleId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	@Override
	public ExecuteResult<String> queryServiceStyleXmlInfoByName(String styleName) {
		try {
   			if (StringUtil.isEmpty(styleName)) {
   				return ExecuteResult.error("数据样式资源ID入参不能为空值", null);
   			}
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("styleName", styleName);
   			ZyServiceStyleModel targetModel = serviceStyleResourceDao.queryObject(ZyServiceStyleModel.class,"NAME=:styleName", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应数据样式资源记录信息!", null);
   			}else {
   				if("1".equals(targetModel.getStatus())) {
   					return ExecuteResult.error("对应数据样式资源已禁用，请联系管理员!", null);
   				}
   				String xmlContent = targetModel.getContent();
   				if(StringUtil.isNotEmpty(xmlContent)) {
   					return ExecuteResult.success("查询成功",xmlContent);
   				}else {
   					return ExecuteResult.error("对应数据样式资源暂未添加xml样式信息!", null);
   				}
   			}
   		} catch (Exception e) {
   			logger.error("根据名称【{}】查询Shp数据样式XML信息出现异常，原因：【{}】,详情：", styleName,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	@Override
	public ExecuteResult<JSONObject> uploadStyleXmlAndFormatCheck(MultipartFile xmlfile) {
		BufferedReader reader = null;
		XMLWriter writer = null;
		try {
			JSONObject result = new JSONObject();
			//对文件按行读取
			String fileName = xmlfile.getOriginalFilename();
			if(!(fileName.endsWith(".xml")||fileName.endsWith(".XML")||fileName.endsWith(".sld"))) {
				return  ExecuteResult.error("上传文件非xml文件【"+fileName+"】！", null);
			}
			InputStream xmlIn = xmlfile.getInputStream();
			reader = new BufferedReader(new InputStreamReader(xmlIn, "UTF-8"));
	        StringBuilder document = new StringBuilder();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	            document.append(line);
	        }
			//对读出的内容进行格式化检查
            Document doc = getXmlDoc(document.toString());
            if(doc==null) {
            	return ExecuteResult.error("xml语法检查未通过,请确认！", null);
            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            
            StringWriter sw = new StringWriter();
            writer = new XMLWriter(sw,format);
            writer.write(doc);
            writer.flush();
            result.put("fileName", fileName);
            result.put("xml", sw.toString());
            return ExecuteResult.success("xml解析并检查通过", result);
        }catch (Exception e){
           logger.error("xml解析并检查出现异常，异常详情：",e);
           return ExecuteResult.error("xml解析并检查异常,详情："+e.getMessage(), null);
        }finally {
    		try {
    			if(writer!=null) {
    				writer.close();
    			}
    			if(reader!=null) {
    				reader.close();
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	@Override
	//@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveServiceStyleResourceInfo(ServiceStyleAddDto dto) {
		try {
   			String checkErr = checkServiceStyleResourceInfoForAdd(dto) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("name",dto.getName());
   		    List<ZyServiceStyleModel> styleModels = serviceStyleResourceDao.queryObjects(ZyServiceStyleModel.class,"NAME=:name",paramsMap);
   		    if (ObjectUtil.isNotEmpty(styleModels)&&styleModels.size()>0) {
   		    	return ExecuteResult.error("服务样式资源名称不能重复！", null);
   		    }
   		    //调整逻辑，新增时不做xml存储
   		    /*JSONObject  xmlContent = dto.getContent();
   		    if(xmlContent!=null&&StringUtil.isNotEmpty(xmlContent.getString("xml"))) {
		   		Document doc = getXmlDoc(xmlContent.getString("xml"));
		        if(doc==null) {
		         	return ExecuteResult.error("样式xml语法检查未通过,请确认！", null);
		        }
		        if(StringUtil.isEmpty(xmlContent.getString("fileName"))) {
		        	xmlContent.put("fileName", dto.getName()+".xml");
		        }
   		    }*/
   		    ZyServiceStyleModel model = new ZyServiceStyleModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("YS"));
   		    //model.setUrl(styleUrl+"/"+model.getId());
   			model.setCjsj(new Date());
   			model.setZhgxr(model.getCjr());
   			model.setZhgxsj(model.getCjsj());
   			model.setContent(null);
   			int i = serviceStyleResourceDao.insertServiceStyleResourceInfo(model);
   			
   			//更新xml数据
   			/*if(i>0 && xmlContent!=null) {
   				String updateSql = "update T_ZY_STYLERESOURCE set CONTENT = '"+xmlContent+"' where ID = '"+model.getId()+"'";
   				serviceStyleResourceDao.updateServiceStyleResourceInfoBySql(updateSql);
   			}*/
   			return ExecuteResult.success("服务样式资源添加成功！");
   		} catch (Exception e) {
   			logger.error("新增服务样式资源异常，详情：",e);
   			//throw new RuntimeException("新增服务样式资源异常，请稍后再试"); 
   			return ExecuteResult.error("新增服务样式资源异常！",null);
   		}
	}

	@Override
	public ExecuteResult<String> updateServiceStyleResourceInfo(ServiceStyleUpdateDto dto) {
		try {
   			String serviceStyleId = dto.getId();
   			if(StringUtils.isEmpty(serviceStyleId)) {
       			return ExecuteResult.error("服务样式资源ID不能为空！", null);
       		}
   			if(StringUtil.isEmpty(dto.getZhgxr())) {
   				return ExecuteResult.error("更新人不能为空！", null);
   	 		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("serviceStyleId",serviceStyleId);
   		    ZyServiceStyleModel originServiceStyleResource = serviceStyleResourceDao.queryObject(ZyServiceStyleModel.class,"ID=:serviceStyleId",paramsMap);
   		    if (StringUtil.isEmpty(originServiceStyleResource)) {
   		    	return ExecuteResult.error("未找到对应服务样式资源项，无法完成更新！", null);
   		    }
   		    //检查服务样式资源是否包含重名
   		    if(StringUtil.isNotEmpty(dto.getName())&&!(dto.getName().equals(originServiceStyleResource.getName()))) {
   			    paramsMap.put("name",dto.getName());
   			    List<ZyServiceStyleModel> styleModels = serviceStyleResourceDao.queryObjects(ZyServiceStyleModel.class,"ID!=:serviceStyleId and NAME=:name",paramsMap);
   			    if (StringUtil.isNotEmpty(styleModels)&&styleModels.size()>0) {
   			    	return ExecuteResult.error("要更新的服务样式资源名称已占用！", null);
   			    }
   		    }
   		    String  xmlContent = dto.getContent();
   		    if(StringUtil.isNotEmpty(xmlContent)) {
	   		    Document doc = getXmlDoc(xmlContent);
	 	        if(doc==null) {
	 	         	return ExecuteResult.error("样式xml语法检查未通过未能完成更新,请确认！", null);
	 	        }
   		    }
   		    /*if(xmlContent!=null&&StringUtil.isEmpty(xmlContent.getString("fileName"))) {
   		    	JSONObject content = originServiceStyleResource.getContent();
   		    	if(content!=null&&StringUtil.isNotEmpty(content.getString("fileName"))) {
   		    		xmlContent.put("fileName", content.getString("fileName"));
   		    	}else {
   		    		if(StringUtil.isEmpty(dto.getName())) {
   		    			xmlContent.put("fileName", originServiceStyleResource.getName()+".xml");
   		    		}else {
   		    			xmlContent.put("fileName", dto.getName()+".xml");
   		    		}
   		    	}
	        }*/
   		    //dto.setContent(null);
   		    ZyServiceStyleModel resource = getServiceStyleResourceModelByUpdateInfo(originServiceStyleResource,dto);
   		    serviceStyleResourceDao.updateServiceStyleResourceInfo(resource);
   		    /*if(xmlContent!=null&&StringUtil.isNotEmpty(xmlContent.getString("xml"))) {
				String updateSql = "update T_ZY_STYLERESOURCE set CONTENT = '"+xmlContent+"' where ID = '"+serviceStyleId+"'";
   				serviceStyleResourceDao.updateServiceStyleResourceInfoBySql(updateSql);
		    }*/
   			return ExecuteResult.success("服务样式资源更新成功！");
   		} catch (Exception e) {
   			logger.error("更新服务样式资源异常，详情：",e);
   			return ExecuteResult.error("更新服务样式资源异常，请稍后再试", null);
   		}
	}

	@Override
	public ExecuteResult<String> deleteServiceStyleResourceInfo(String loginUser,String serviceStyleId) {
		try {
   			if (StringUtil.isEmpty(serviceStyleId)) {
   				return ExecuteResult.error("服务样式资源ID入参不能为空值", null);
   			}
   			logger.info("删除服务样式资源信息，记录id【{}】", serviceStyleId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("serviceStyleId", serviceStyleId);
   			ZyServiceStyleModel targetModel = serviceStyleResourceDao.queryObject(ZyServiceStyleModel.class,"ID=:serviceStyleId", paramsOne);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定服务样式资源信息", serviceStyleId);
   				return ExecuteResult.error("已无对应服务样式资源信息!", null);
   			}
   			int count1 = serviceStyleResourceDao.deleteServiceStyleResourceInfoById(serviceStyleId);
   			if (count1> 0) {
   				return ExecuteResult.success("删除成功");
   			}else {
   				return ExecuteResult.error("删除服务样式资源失败",null);
   			}
   		} catch (Exception e) {
   			logger.error("【{}】删除服务样式资源【{}】出现异常，原因：【{}】,详情：",loginUser, serviceStyleId,e.getMessage(), e);
   			return ExecuteResult.error("删除失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	
	
	 //获取目标更新的服务样式资源信息
   	private ZyServiceStyleModel getServiceStyleResourceModelByUpdateInfo(ZyServiceStyleModel origin,ServiceStyleUpdateDto update) {
   			
   		 if(StringUtil.isNotEmpty(update.getName())) {
   			origin.setName(update.getName());
   		 }
   		 if(StringUtil.isNotEmpty(update.getType())) {
   			origin.setType(update.getType());
   		 }
   		 if(StringUtil.isNotEmpty(update.getStatus())) {
   			origin.setStatus(update.getStatus());
    	 }
   		 if(StringUtil.isNotEmpty(update.getContent())) {
    		origin.setContent(update.getContent());
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
     //校验新增的服务样式资源信息
   	 private String checkServiceStyleResourceInfoForAdd(ServiceStyleAddDto model) throws Exception {
   		 StringBuilder checkInfo = new StringBuilder("");
   		 if(StringUtil.isEmpty(model)) {
   			 checkInfo.append("ServiceStyleAddDto不能为空;");
   			 return checkInfo.toString();
   		 }
   		 if(StringUtil.isEmpty(model.getName())) {
   	        checkInfo.append("服务样式资源名称【name】不能为空;");
   	     }
   		 if(StringUtil.isEmpty(model.getType())) {
    	    checkInfo.append("服务样式资源类型或分类【type】不能为空;");
    	 }
   		 /*if(StringUtil.isEmpty(model.getContent())) {
   	        checkInfo.append("服务样式资源业务类型【content】不能为空;");
   	     }*/
   		 if(StringUtil.isEmpty(model.getStatus())) {
   			model.setStatus("0"); //默认可用
   	     }
   		 if(StringUtil.isEmpty(model.getCjr())) {
   			 checkInfo.append("服务样式资源添加人【cjr】不能为空;");
   		 }
   		 /*if(StringUtil.isEmpty(model.getProject())) {
  			 checkInfo.append("服务样式所属项目【project】不能为空;");
  		 }*/
   		 return checkInfo.toString();
   		 
   	 }
   	 
   	 private Document getXmlDoc(String xmlStr) {
   		SAXReader xmlReader = new SAXReader();
        StringReader sr = new StringReader(xmlStr);
        try {
			Document doc = xmlReader.read(sr);
			return doc;
		} catch (DocumentException e) {
			logger.error("根据xml字符串获取xml文档对象异常，详情：",e);
			return null;
		}
   	 }



	

}
