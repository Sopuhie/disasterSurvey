package com.santech.module.biz.service.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.config.KindCryptoConfig;
import com.santech.core.framework.dao.ServiceResourceDao;
import com.santech.core.framework.dao.ThemeServiceRelateDao;
import com.santech.core.framework.entity.model.JcActionModel;
import com.santech.core.framework.entity.model.ZyServiceModel;
import com.santech.core.framework.entity.model.ZyServiceSimpleModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.kindcrypto.KindCryptoUtils;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.ServiceAddDto;
import com.santech.module.biz.entity.dto.ServiceResourceQueryDto;
import com.santech.module.biz.entity.dto.ServiceUpdateDto;
import com.santech.module.biz.entity.vo.ResourceStatisticsResultVo;
import com.santech.module.biz.entity.vo.ServiceResourceTreeVo;
import com.santech.module.biz.service.ServiceResourceService;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class ServiceResourceServiceImpl implements ServiceResourceService {
	private static final  Logger logger = LoggerFactory.getLogger(ServiceResourceServiceImpl.class);
	@Resource
 	private KindCryptoConfig kindCryptoConfig;
    @Autowired
    private ServiceResourceDao serviceResourceDao;
    
    @Autowired
    private ThemeServiceRelateDao themeServiceRelateDao;
    
    @Override
	public ExecuteResult<List<ZyServiceModel>> queryServiceResourceInfosWithPage(ServiceResourceQueryDto query) {
		logger.info("查询数据服务资源列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"cjsj desc","");
            //根据拼接sql,调用dao层获取数据
            ExecuteResult<List<ZyServiceModel>> queryResult = serviceResourceDao.queryObjectsWithPageInfo(ZyServiceModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
            if(StringUtil.isNotEmpty(queryResult.getData())) {
         	   //对加密的数据进行解密返回
         	   for(ZyServiceModel service:queryResult.getData()) {
         		   if(StringUtil.isNotEmpty(service.getServiceUrl())){
       	   		    	String decryptStr = KindCryptoUtils.decryptSM4(service.getServiceUrl(), 3,kindCryptoConfig.isEnable());
       	   		    	if(StringUtil.isNotEmpty(decryptStr)) {
       	   		    		service.setServiceUrl(decryptStr);
       	   		    	}
       	   		    }
         	   }
            }
            return queryResult;
        } catch (Exception e) {
            logger.error("查询数据服务资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
    
    
    @Override
	public ExecuteResult<ServiceResourceTreeVo> queryServiceResourceTreeInfos(
			ServiceResourceQueryDto query) {
    	logger.info("查询数据服务资源列表信息，入参：【{}】", query);
        try {
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"cjsj desc","");
            //根据拼接sql,调用dao层获取数据
            List<ZyServiceSimpleModel> serviceDatas = serviceResourceDao.queryObjects(ZyServiceSimpleModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
            //对数据按照平台以及类型进行分组返回
            ServiceResourceTreeVo tree = getResourceTree(serviceDatas);
            return ExecuteResult.success("查询成功", tree);
        } catch (Exception e) {
            logger.error("查询数据服务资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
    
    private ServiceResourceTreeVo  getResourceTree(List<ZyServiceSimpleModel> totalData) throws Exception{
    	 Map<String,Object> paramsMap = new HashMap<String,Object>();
		 paramsMap.put("name","平台服务");
    	 List<JcActionModel> actionModels = serviceResourceDao.queryObjects(JcActionModel.class,"NAME=:name",paramsMap);
	     if (StringUtil.isNotEmpty(actionModels)&&actionModels.size()>0) {
			 JcActionModel action = actionModels.get(0);
			 String config = action.getConfigure();
			 JSONObject catalogJson = JSONObject.parseObject(config);
    		 return  getServiceResourceTreeModel(catalogJson,totalData,new ServiceResourceTreeVo());
    	 }else {
    		return new ServiceResourceTreeVo();
    	 }
    }
    private ServiceResourceTreeVo getServiceResourceTreeModel(JSONObject catalogJson,List<ZyServiceSimpleModel> totalData,ServiceResourceTreeVo tree) {
    	String serviceTypeFlag = catalogJson.getString("jdlx");
    	if("leaf".equalsIgnoreCase(serviceTypeFlag)) {
    		//服务类型节点
    		if(tree==null) {
    			tree = new ServiceResourceTreeVo();
    		}
    		String serviceType = catalogJson.getString("zdbm");
    		tree.setNodeId(catalogJson.getString("id"));
    		tree.setNodeName(catalogJson.getString("zdz"));
    		tree.setNodeType(serviceTypeFlag);
			int leafServiceCount = 0;
			List<ServiceResourceTreeVo> services = new ArrayList<ServiceResourceTreeVo>(); 
    		if(totalData!=null&&totalData.size()>0) {
    			for(ZyServiceSimpleModel serviceData:totalData) {
    				if(serviceType.equalsIgnoreCase(serviceData.getServiceType())) {
    					ServiceResourceTreeVo service = new ServiceResourceTreeVo();
	    				service.setNodeId(serviceData.getId());
	    				service.setNodeName(serviceData.getServiceName());
	    				service.setNodeType("service");
	    				services.add(service);
    				}
    			}
    			leafServiceCount = services.size();
    			tree.setServiceCount(leafServiceCount);
    			tree.setChildren(services);
        	}
    	}else {
    		if(tree==null) {
    			tree = new ServiceResourceTreeVo();
    		}
    		tree.setNodeId(catalogJson.getString("zdbm"));
    		tree.setNodeName(catalogJson.getString("zdz"));
    		tree.setNodeType(serviceTypeFlag);
			JSONArray children = catalogJson.getJSONArray("children");
			List<ServiceResourceTreeVo> childs = new ArrayList<ServiceResourceTreeVo>();
			int folderServiceCount = 0;
			if(children!=null&&children.size()>0) {
				for(int i =0;i<children.size();i++) {
					ServiceResourceTreeVo child = getServiceResourceTreeModel(children.getJSONObject(i),totalData,new ServiceResourceTreeVo());
					childs.add(child);
					folderServiceCount+=child.getServiceCount();
				}
			}
			tree.setServiceCount(folderServiceCount);
			tree.setChildren(childs);
    	}
    	return tree;
    }
    
    
    @Override
	public ExecuteResult<ZyServiceModel> queryServiceResourceInfoById(String serviceId) {
    	try {
			if (StringUtil.isEmpty(serviceId)) {
				return ExecuteResult.error("数据服务资源ID入参不能为空值", null);
			}
			logger.info("根据ID查询数据服务资源信息，记录id【{}】", serviceId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("serviceId", serviceId);
			ZyServiceModel targetModel = serviceResourceDao.queryObject(ZyServiceModel.class,"ID=:serviceId", paramsOne);
			if (targetModel == null) {
				return ExecuteResult.error("根据ID未查询到对应数据服务资源信息!", null);
			}else {
				if(StringUtil.isNotEmpty(targetModel.getServiceUrl())){
   	   		    	String decryptStr = KindCryptoUtils.decryptSM4(targetModel.getServiceUrl(), 3,kindCryptoConfig.isEnable());
   	   		    	if(StringUtil.isNotEmpty(decryptStr)) {
   	   		    		targetModel.setServiceUrl(decryptStr);
   	   		    	}
   	   		    }
				return ExecuteResult.success("查询成功",targetModel);
			}
		} catch (Exception e) {
			logger.error("根据ID【{}】查询数据服务资源信息出现异常，原因：【{}】,详情：", serviceId,e.getMessage(), e);
			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
		}
	}
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> saveServiceResourceInfo(ServiceAddDto dto) {
    	try {
			String checkErr = checkServiceResourceInfoForAdd(dto) ;
			if(StringUtil.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("serviceName",dto.getServiceName());
		    paramsMap.put("servicePlatform",dto.getServicePlatform());
		    List<ZyServiceModel> serviceModels = serviceResourceDao.queryObjects(ZyServiceModel.class,"SERVICENAME=:serviceName and SERVICEPLATFORM=:servicePlatform",paramsMap);
		    if (ObjectUtil.isNotEmpty(serviceModels)&&serviceModels.size()>0) {
		    	return ExecuteResult.error("同一平台下的数据服务资源名称不能重复！", null);
		    }
		    ZyServiceModel model = new ZyServiceModel();
		    BeanUtils.copyProperties(dto, model);
		    model.setId(UuidUtils.buildID("SR"));
			model.setExtentMeta(null);
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(model.getCjsj());
			int i = serviceResourceDao.insertServiceResourceInfo(model);
			//更新空间元数据
			if(i>0) {
				if(StringUtil.isNotEmpty(dto.getExtentMeta())) {
					String updateSql = "update T_ZY_SERVICE set EXTENTMETA = '"+dto.getExtentMeta()+"' where ID = '"+model.getId()+"'";
					serviceResourceDao.updateServiceResourceInfoBySql(updateSql);
				}
				return ExecuteResult.success("数据服务资源添加成功！");
			}else {
				return ExecuteResult.error("数据服务资源添加失败！",null);
			}
			
		} catch (Exception e) {
			logger.error("新增数据服务资源异常，详情：",e);
			throw new RuntimeException("新增数据服务资源异常，请稍后再试");
		}
	}
    
    @Override
    @Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> updateServiceResourceInfo(ServiceUpdateDto dto) {
    	try {
			String serviceId = dto.getId();
			if(StringUtils.isEmpty(serviceId)) {
    			return ExecuteResult.error("数据服务资源ID不能为空！", null);
    		}
			if(StringUtil.isEmpty(dto.getZhgxr())) {
				return ExecuteResult.error("数据更新人不能为空！", null);
	 		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("serviceId",serviceId);
		    ZyServiceModel originService = serviceResourceDao.queryObject(ZyServiceModel.class,"ID=:serviceId",paramsMap);
		    if (StringUtil.isEmpty(originService)) {
		    	return ExecuteResult.error("未找到对应数据服务资源项，无法完成更新！", null);
		    }
		    //检查数据服务资源是否包含重名
		    if(StringUtil.isNotEmpty(dto.getServiceName())&&!(dto.getServiceName().equals(originService.getServiceName()))) {
			    paramsMap.put("serviceName",dto.getServiceName());
			    paramsMap.put("servicePlatform",originService.getServicePlatform());
			    List<ZyServiceModel> serviceModels = serviceResourceDao.queryObjects(ZyServiceModel.class,"ID!=:serviceId and SERVICENAME=:serviceName and SERVICEPLATFORM=:servicePlatform",paramsMap);
			    if (StringUtil.isNotEmpty(serviceModels)&&serviceModels.size()>0) {
			    	return ExecuteResult.error("要更新的对应平台的数据服务资源名称已占用！", null);
			    }
		    }
		    ZyServiceModel service = getServiceResourceModelByUpdateInfo(originService,dto);
		    serviceResourceDao.updateServiceResourceInfo(service);
		    //更新空间元数据
		    if(StringUtil.isNotEmpty(dto.getExtentMeta())) {
		    	String updateSql = "update T_ZY_SERVICE set EXTENTMETA = '"+dto.getExtentMeta()+"' where ID = '"+dto.getId()+"'";
		    	serviceResourceDao.updateServiceResourceInfoBySql(updateSql);
		    }
			return ExecuteResult.success("数据服务资源更新成功！");
		} catch (Exception e) {
			logger.error("更新数据服务资源异常，详情：",e);
			throw new RuntimeException("更新数据服务资源异常，请稍后再试");
		}
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> deleteServiceResourceInfo(String serviceId, String loginUser) {
		try {
			if (StringUtil.isEmpty(serviceId)) {
				return ExecuteResult.error("数据服务资源ID入参不能为空值", null);
			}
			logger.info("删除数据服务资源信息，记录id【{}】", serviceId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("serviceId", serviceId);
			ZyServiceModel targetModel = serviceResourceDao.queryObject(ZyServiceModel.class,"ID=:serviceId", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定数据服务资源信息", serviceId);
				return ExecuteResult.error("已无对应数据服务资源信息!", null);
			}
			int count1 = serviceResourceDao.deleteServiceResourceInfoById(serviceId);
			if (count1> 0) {
		         // 删除资源与专题的绑定关系
		         themeServiceRelateDao.deleteByServiceId(serviceId);
				return ExecuteResult.success("删除成功");
			}else {
				throw new RuntimeException("删除数据服务资源失败");
			}
		} catch (Exception e) {
			logger.error("删除数据服务资源【{}】出现异常，原因：【{}】,详情：", serviceId,e.getMessage(), e);
			throw new RuntimeException("删除失败，原因 【"+e.getMessage()+"】");
		}
	}
	
	@Override
	public void updateMapServiceStatusInfo() {
		 try {
	        	logger.info("开始数据服务状态巡检【{}】",System.currentTimeMillis());
	            //查询所有数据服务
	        	String querySql = "select s.ID as ID,s.SERVICENAME as SERVICENAME,s.SERVICEURL as SERVICEURL,s.STATUS as STATUS  from T_ZY_SERVICE s WHERE s.SERVICEURL like 'http%' ";
	            
	        	List<Map<String, Object>>  services = serviceResourceDao.selectServiceResourcesWithoutPage(querySql, null);
	        	
	        	if(services!=null&&services.size()>0) {
	        		for(Map<String, Object> service:services) {
	        			int code = 404;
	        			String serviceId = String.valueOf(service.get("ID"));
	        			String serviceName = String.valueOf(service.get("SERVICENAME"));
	                    String serviceurl = String.valueOf(service.get("SERVICEURL"));
	                    if(StringUtil.isNotEmpty(serviceurl)){
	                    	//该字段为加密存储，需要解密使用
           	   		    	String decryptStr = KindCryptoUtils.decryptSM4(serviceurl, 3,kindCryptoConfig.isEnable());
           	   		    	if(StringUtil.isNotEmpty(decryptStr)) {
           	   		    		serviceurl = decryptStr;
           	   		    	}
           	   		    }
	                    String serviceStatus = String.valueOf(service.get("STATUS"));
	                    String encode = URLEncoder.encode(serviceurl, Constants.UTF8);
	                    encode = encode.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("\\+", "%20")
	                    		.replaceAll("%3F", "?").replaceAll("%3D", "=");//由于有的服务带参数，补充%3F和%3D的替换
                        try {
                        	URL url = new URL(encode);
                        	HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
    	                    httpUrlConnection.setConnectTimeout(3000);
                            code = httpUrlConnection.getResponseCode();
                        } catch (Exception e) {
                            code = 504;
                        }
                        String status = "0";
                        if(200==code) {
                        	status = "1";
                        }else {
                        	logger.info("数据服务【{}】【{}】巡检不可用，返回码为【{}】",serviceId,serviceName,code);
                        }
                        if(!status.equals(serviceStatus)) {
                        	String updateSql = "update T_ZY_SERVICE set status='"+status+"' where id = '"+serviceId+"'";
                            //更新服务状态
                            serviceResourceDao.updateServiceResourceInfoBySql(updateSql);
                        }
                        
	        		}
	        	}
	        	
	        } catch (Exception e) {
	        	logger.error("数据服务状态巡检出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
	        }finally {
	        	logger.info("数据服务状态巡检结束【{}】",System.currentTimeMillis());
	        }
	}
	
	@Override
	public ExecuteResult<List<ResourceStatisticsResultVo>> statisticsServiceResourceForLeftTab(String actionName) {
		try {
			if(StringUtil.isEmpty(actionName)) {
				return ExecuteResult.error("功能名称不能为空值", null);
			}
			 List<ResourceStatisticsResultVo> result = new ArrayList<ResourceStatisticsResultVo>();
			 ResourceStatisticsResultVo statisticResult = new ResourceStatisticsResultVo();
			 statisticResult.setNodeId("S"+1);
			 Map<String,Object> paramsMap = new HashMap<String,Object>();
			 paramsMap.put("name",actionName);
	    	 List<JcActionModel> actionModels = serviceResourceDao.queryObjects(JcActionModel.class,"NAME=:name",paramsMap);
		     if (StringUtil.isNotEmpty(actionModels)&&actionModels.size()>0) {
				 JcActionModel action = actionModels.get(0);
	    		 String config = action.getConfigure();
	    		 JSONObject catalogJson = JSONObject.parseObject(config);
	    		 List<ZyServiceSimpleModel> serviceDatas = serviceResourceDao.queryObjects(ZyServiceSimpleModel.class, "",null);
	    		 statisticResult = getServiceResourceStatisticsTreeModel(catalogJson,serviceDatas,statisticResult,"-1");
	    	 }
	    	 result.add(statisticResult);
   			return ExecuteResult.success("文件资源统计成功！",result);
   		} catch (Exception e) {
   			logger.error("文件资源统计异常，详情：",e);
   			return ExecuteResult.error("文件资源统计异常，请稍后再试", null);
   		}
	}
	
	 private ResourceStatisticsResultVo getServiceResourceStatisticsTreeModel(JSONObject catalogJson,List<ZyServiceSimpleModel> totalData,ResourceStatisticsResultVo tree,String pid) {
	    	String serviceTypeFlag = catalogJson.getString("jdlx");
	    	String nodeId = UUID.randomUUID().toString();
	    	if("leaf".equalsIgnoreCase(serviceTypeFlag)) {
	    		//服务类型节点
	    		if(tree==null) {
	    			tree = new ResourceStatisticsResultVo();
	    			
	    		}
	    		String serviceType = catalogJson.getString("zdbm");
	    		tree.setNodeName(catalogJson.getString("zdz"));
	    		tree.setNodeCode(serviceType);
	    		tree.setNodeType("leaf");
	    		tree.setNodeId(nodeId);
    			tree.setPid(pid);
				int leafServiceCount = 0;
	    		if(totalData!=null&&totalData.size()>0) {
	    			for(ZyServiceSimpleModel serviceData:totalData) {
	    				if(serviceType.equalsIgnoreCase(serviceData.getServiceType())) {
	    					leafServiceCount++;
	    				}
	    			}
	    			tree.setStatisticResult(leafServiceCount);
	        	}
	    	}else {
	    		if(tree==null) {
	    			tree = new ResourceStatisticsResultVo();
	    		}
	    		if("root".equalsIgnoreCase(serviceTypeFlag)) {
	    			tree.setNodeName("全部数据服务");
		    		tree.setNodeCode("");
	    		}else {
	    			tree.setNodeName(catalogJson.getString("zdz"));
		    		tree.setNodeCode(catalogJson.getString("zdbm"));
	    		}
	    		tree.setNodeId(nodeId);
    			tree.setPid(pid);
	    		tree.setNodeType(serviceTypeFlag);
				JSONArray children = catalogJson.getJSONArray("children");
				List<ResourceStatisticsResultVo> childs = new ArrayList<ResourceStatisticsResultVo>();
				int folderServiceCount = 0;
				if(children!=null&&children.size()>0) {
					for(int i =0;i<children.size();i++) {
						ResourceStatisticsResultVo child = getServiceResourceStatisticsTreeModel(children.getJSONObject(i),totalData,new ResourceStatisticsResultVo(),nodeId);
						childs.add(child);
						folderServiceCount+=child.getStatisticResult();
					}
				}
				tree.setStatisticResult(folderServiceCount);
				tree.setChildren(childs);
	    	}
	    	return tree;
	    }
	
    //获取目标更新的数据服务资源信息
	private ZyServiceModel getServiceResourceModelByUpdateInfo(ZyServiceModel origin,ServiceUpdateDto update) {
			
		 if(StringUtil.isNotEmpty(update.getServiceName())) {
			origin.setServiceName(update.getServiceName());
		 }
		 /*if(StringUtil.isNotEmpty(update.getServicePlatform())) {
			origin.setServicePlatform(update.getServicePlatform());
		 }
		 if(StringUtil.isNotEmpty(update.getServiceType())) {
			origin.setServiceType(update.getServiceType());
		 }*/
		 if(StringUtil.isNotEmpty(update.getServiceUrl())) {
			//对服务地址进行加密存储
 			String encryptStr = KindCryptoUtils.encryptSM4(update.getServiceUrl(), 3,kindCryptoConfig.isEnable());
       		if(StringUtil.isNotEmpty(encryptStr)) {
       			origin.setServiceUrl(encryptStr);
       		}else {
       			origin.setServiceUrl(update.getServiceUrl());
       		}
		 }
		 if(StringUtil.isNotEmpty(update.getSortIndex())) {
			origin.setSortIndex(update.getSortIndex());
		 }
		 if(StringUtil.isNotEmpty(update.getRelay())) {
			origin.setRelay(update.getRelay());
		 }
		 if(StringUtil.isNotEmpty(update.getStatus())) {
			origin.setStatus(update.getStatus());
		 }
		 if(StringUtil.isNotEmpty(update.getDetect())) {
			origin.setDetect(update.getDetect());
		 }
		 if(StringUtil.isNotEmpty(update.getConfidential())) {
			origin.setConfidential(update.getConfidential());
		 }
		 if(StringUtil.isNotEmpty(update.getScreenshot())) {
			origin.setScreenshot(update.getScreenshot());
		 }
		 if(StringUtil.isNotEmpty(update.getUnit())) {
			origin.setUnit(update.getUnit());
		 }
		 if(StringUtil.isNotEmpty(update.getZygzid())) {
			origin.setZygzid(update.getZygzid());
		 }
		 if(StringUtil.isNotEmpty(update.getAttachmentid())) {
			origin.setAttachmentid(update.getAttachmentid());
		 }
		 if(StringUtil.isNotEmpty(update.getAttachmentcgid())) {
			origin.setAttachmentcgid(update.getAttachmentcgid());
		 }
		 if(StringUtil.isNotEmpty(update.getDescription())) {
			origin.setDescription(update.getDescription());
		 }
		 if(StringUtil.isNotEmpty(update.getZhgxr())) {
			origin.setZhgxr(update.getZhgxr());
 		 }
		 origin.setZhgxsj(new Date());
		return origin;
	}
    //校验新增的服务信息
	 private String checkServiceResourceInfoForAdd(ServiceAddDto model) throws Exception {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("ServiceAddDto不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getServiceName())) {
	        checkInfo.append("服务名称【serviceName】不能为空;");
	     }
         if(StringUtil.isEmpty(model.getServiceUrl())) {
            checkInfo.append("服务地址【serviceUrl】不能为空;");
         }else {
        	 //对服务地址进行加密存储
 			String encryptStr = KindCryptoUtils.encryptSM4(model.getServiceUrl(), 3,kindCryptoConfig.isEnable());
       		if(StringUtil.isNotEmpty(encryptStr)) {
       			model.setServiceUrl(encryptStr);
       		}
         }
         if(StringUtil.isEmpty(model.getSortIndex())||model.getSortIndex()<1) {
 			//如果为空，复制为默认的值（库中最大排序加1）
 			 model.setSortIndex(serviceResourceDao.getObjectSortIndexForIntegerType("T_ZY_SERVICE", "SORTINDEX"));
 		 }
		 if(StringUtil.isEmpty(model.getServicePlatform())) {
			 checkInfo.append("数据发布平台【servicePlatform】不能为空;");
		 }else {
			 //保存到后台的数据老是会有空格，临时给trim一下
			 model.setServicePlatform(model.getServicePlatform().trim());
		 }
		 if(StringUtil.isEmpty(model.getServiceType())) {
			 checkInfo.append("数据服务类型【serviceType】不能为空;");
		 }
		 
		 if(StringUtil.isEmpty(model.getConfidential())) {
			 //checkInfo.append("数据服务类型【serviceType】不能为空;");
			 model.setConfidential("2");//服务数据秘密性，1公开非涉密，2涉密,鉴于安全考虑，默认为2
		 }
		 
		 if(StringUtil.isEmpty(model.getCjr())) {
			 checkInfo.append("数据服务添加人【cjr】不能为空;");
		 }
		 return checkInfo.toString();
		 
	 }


	

}
