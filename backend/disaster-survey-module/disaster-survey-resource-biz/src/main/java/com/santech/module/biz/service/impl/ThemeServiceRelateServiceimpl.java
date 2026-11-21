package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.config.KindCryptoConfig;
import com.santech.core.framework.dao.ServiceResourceDao;
import com.santech.core.framework.dao.ThemeResourceDao;
import com.santech.core.framework.dao.ThemeServiceRelateDao;
import com.santech.core.framework.entity.model.ZyServiceModel;
import com.santech.core.framework.entity.model.ZyServiceSimpleModel;
import com.santech.core.framework.entity.model.ZyThemeModel;
import com.santech.core.framework.entity.model.ZyThemeServiceRelateModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.kindcrypto.KindCryptoUtils;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.config.ServiceConfig;
import com.santech.module.biz.entity.dto.ThemeResourceTreeQueryDto;
import com.santech.module.biz.entity.dto.ThemeServiceBindDto;
import com.santech.module.biz.entity.vo.BindServiceLayerVo;
import com.santech.module.biz.entity.vo.ThemeResourceTreeVo;
import com.santech.module.biz.entity.vo.ThemeWithServiceLayersVo;
import com.santech.module.biz.service.ThemeServiceRelateService;

import cn.hutool.core.util.ObjectUtil;

@Service
public class ThemeServiceRelateServiceimpl implements ThemeServiceRelateService {
	private static final  Logger logger = LoggerFactory.getLogger(ThemeServiceRelateServiceimpl.class);
	
	@Resource
    private ServiceConfig serviceConfig;
	
	@Resource
 	private KindCryptoConfig kindCryptoConfig;
	
	@Resource
    private ThemeServiceRelateDao themeServiceRelateDao;
    
	@Resource
    private ThemeResourceDao themeResourceDao;
    
	@Resource
    private ServiceResourceDao serviceResourceDao;
    
    //@Autowired
    //private ServiceLayerDao serviceLayerDao;
    
    
    @Override
   	public ExecuteResult<List<ThemeResourceTreeVo>> queryThemeResourceTreeInfos(ThemeResourceTreeQueryDto query) {
       		logger.info("查询数据专题资源列表信息，入参：【{}】", query);
           try {
               //请求参数
               Map<String,Object> params = new HashMap<String,Object>();
               String whereSql = "";
               if(StringUtil.isNotEmpty(query.getProject())) {
            	   params.put("project", query.getProject());
            	   whereSql = "project=:project";
               }
               whereSql+=" order by SORTINDEX asc";
               //根据拼接sql,调用dao层获取数据
               List<ZyThemeModel> themeDatas = themeResourceDao.queryObjects(ZyThemeModel.class, whereSql,params);
               //生成专题树
               List<ThemeResourceTreeVo> themeTree = getThemeTree(themeDatas,new ArrayList<ThemeResourceTreeVo>(),"-1");
               
               //查询被专题绑定的所有数据服务，避免重复查库（考虑到数据量的问题，采用此方式）
	           String  querySql = "select distinct s.ID,s.SERVICENAME from T_ZY_SERVICE s " + 
		           		"left join T_ZY_THEMESERVICERELATE ts  on s.ID = ts.SERVICEID " + 
		           		"where ts.SERVICEID is not null";
	           
	           List<Map<String, Object>> list = serviceResourceDao.selectServiceResourcesWithoutPage(querySql, null);
	           if(list!=null&&list.size()>0) {
	        	   List<ZyServiceSimpleModel> serviceDatas  = JSONArray.parseArray(JSON.toJSONString(list), ZyServiceSimpleModel.class);
	        	   //获取被专题绑定的所有图层信息
	        	   /*List<ZyLayerModel> layerDatas = null;
	        	   String  queryLayerSql = "select l.ID,l.PARENTID,l.SERVICEID,l.LAYERNAME,l.LAYERINDEX,l.LAYERTYPE from T_ZY_LAYER l " + 
			           		"left join T_ZY_THEMESERVICERELATE ts  on l.ID = ts.LAYERID " + 
			           		"where ts.LAYERID is not null";
	        	   List<Map<String, Object>> layerlist = serviceLayerDao.selectServiceLayersWithoutPage(queryLayerSql, null);
	        	   
	        	   if(layerlist!=null&&layerlist.size()>0) {
	        		   //将图层信息挂到服务上
	        		   layerDatas = JSONArray.parseArray(JSON.toJSONString(layerlist), ZyLayerModel.class);
	        	   }*/
	        	   //将服务挂到专题上
	        	   themeTree = getThemeServiceTree(themeTree,serviceDatas);
	           }
	           //查询条件的专题目录不为空，则截取对应的树分支予以返回
	           if(StringUtil.isNotEmpty(query.getThemename())) {
	        	   themeTree =  getChildTreeByNodeName(themeTree,query.getThemename());
	           }
               return ExecuteResult.success("查询成功", themeTree);
           } catch (Exception e) {
               logger.error("查询数据专题资源列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
               return ExecuteResult.error("查询失败",null);
           }
   	}
    
    private List<ThemeResourceTreeVo> getChildTreeByNodeName(List<ThemeResourceTreeVo> totalTree,String nodeName){
    	List<ThemeResourceTreeVo> childTree = null;
    	if(totalTree!=null&&totalTree.size()>0) {
    		for(ThemeResourceTreeVo tree:totalTree) {
    			String name = tree.getNodeName();
        		if(name.equals(nodeName)) {
        			childTree = tree.getChildren();
        			break;
        		}else {
        			List<ThemeResourceTreeVo> children = tree.getChildren();
        			if(children!=null&&children.size()>0) {
        				childTree = getChildTreeByNodeName(children,nodeName);
        				if(childTree!=null&&childTree.size()>0) {
        					break;
        				}
        			}
        		}
        	}
    	}
    	return childTree;
    }
    
  //根据指定的专题的父级id获取其子孙树结构信息
    private List<ThemeResourceTreeVo> getThemeTree(List<ZyThemeModel> themes,List<ThemeResourceTreeVo> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<ThemeResourceTreeVo>();
		}
		if(parentId!=null&&themes!=null&&themes.size()>0) {
     	   for(ZyThemeModel theme:themes) {
     		   if(parentId.equals(theme.getParentId())) {
     			  ThemeResourceTreeVo tree = new ThemeResourceTreeVo();
     			  tree.setNodeId(theme.getId());
     			  tree.setNodeName(theme.getThemeName());
     			  tree.setNodeType(theme.getType());
     			  tree.setPid(parentId);
     			  tree.setChildren(getThemeTree(themes,new ArrayList<ThemeResourceTreeVo>(),theme.getId()));
        		  trees.add(tree);
     		   }
     	   }
        }
		return trees;
	}
   //private List<ThemeResourceTreeModel>  getThemeServiceTree(List<ThemeResourceTreeModel> themeTree,List<ZyServiceSimpleModel> serviceDatas,List<ZyLayerModel> layerDatas) throws Exception{ 
   private List<ThemeResourceTreeVo>  getThemeServiceTree(List<ThemeResourceTreeVo> themeTree,List<ZyServiceSimpleModel> serviceDatas) throws Exception{
	   if(themeTree!=null&&themeTree.size()>0) {
     	   for(ThemeResourceTreeVo theme:themeTree) {
     		   List<ThemeResourceTreeVo> themeServiceChildren = new ArrayList<ThemeResourceTreeVo>();
     		   if("theme".equalsIgnoreCase(theme.getNodeType())) {
     			  //获取专题的关联服务与图层信息
          		  List<ZyThemeServiceRelateModel> relates = themeServiceRelateDao.queryObjects(ZyThemeServiceRelateModel.class, "THEMEID='" + theme.getNodeId() + "' order by sortindex",null);
          		  if(relates!=null&&relates.size()>0) {
          			  for(ZyThemeServiceRelateModel relate:relates) {
          				  if(serviceDatas!=null&&serviceDatas.size()>0) {
          					 for(ZyServiceSimpleModel serviceData:serviceDatas) {
        	           			  if(serviceData.getId().equals(relate.getServiceid())) {
        	           				  ThemeResourceTreeVo service = new ThemeResourceTreeVo();
        	            				service.setNodeId(relate.getThemeid()+"-"+serviceData.getId());
        	            				service.setNodeName(serviceData.getServiceName());
        	            				service.setPid(theme.getNodeId());
        	            				service.setNodeType("service");
        	            				/*List<ThemeResourceTreeModel> layers = new ArrayList<ThemeResourceTreeModel>(); 
        	            				if(layerDatas!=null&&layerDatas.size()>0) {
        	            					for(ZyLayerModel layerData:layerDatas) {
        		            					if(layerData.getId().equals(relate.getLayerid())) {
        		            						ThemeResourceTreeModel layer = new ThemeResourceTreeModel();
        		            						layer.setNodeId(layerData.getId());
        		            						layer.setNodeName(layerData.getLayerName());
        		            						layer.setNodeType("layer");
        		            						layer.setPid(serviceData.getId());
        		            						layers.add(layer);
        		            					}
        		            				}
        	            				}
        	            				service.setChildren(layers);*/
        	            				themeServiceChildren.add(service);
        	            				break;
        	           			  }
        	           			}
          				  }
     	        		}
          			}
     		   }
     		   List<ThemeResourceTreeVo> themeChildren = theme.getChildren();
     		   if(themeChildren!=null&&themeChildren.size()>0) {
     			  themeChildren = getThemeServiceTree(themeChildren,serviceDatas);
     			  themeServiceChildren.addAll(themeChildren);
     			  theme.setChildren(themeServiceChildren);
     		   }else {
     			  theme.setChildren(themeServiceChildren);
     		   }
     	   }
        }
   		return themeTree;
   }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> serviceThemeBind(ThemeServiceBindDto model) {
    	try {
   			String checkErr = checkBindInfoForAdd(model) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			String themeId = model.getThemeId();
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("themeId",themeId);
   		    ZyThemeModel themeModel = themeResourceDao.queryObject(ZyThemeModel.class,"ID=:themeId",paramsMap);
   		    if (ObjectUtil.isEmpty(themeModel)) {
   		    	return ExecuteResult.error("根据专题ID未发现目标专题记录！", null);
   		    }
   		    if("folder".equalsIgnoreCase(themeModel.getType())) {
   		    	return ExecuteResult.error("数据服务需绑定到专题节点，不可绑定到目录！", null);
   		    }
   		   
   		     int sortindex = themeServiceRelateDao.getObjectSortIndexForIntegerType("T_ZY_THEMESERVICERELATE", "SORTINDEX","WHERE THEMEID='"+themeId+"'");
			 ZyThemeServiceRelateModel tsr = new ZyThemeServiceRelateModel();
	   	     tsr.setThemeid(themeId);
	   	     List<BindServiceLayerVo> services = model.getServices();
	   	     for(int i=0;i<services.size();i++) {
	   	    	BindServiceLayerVo service = services.get(i);
	   	    	paramsMap.put("serviceid", service.getServiceId());
				List<ZyThemeServiceRelateModel> tsrs = themeServiceRelateDao.queryObjects(ZyThemeServiceRelateModel.class,"THEMEID=:themeId and SERVICEID=:serviceid",paramsMap);
				if (tsrs != null&&tsrs.size()>0) {
					//避免重复绑定
					continue;
				}
   	            tsr.setId(UuidUtils.buildID("TSR"));
   	            tsr.setServiceid(service.getServiceId());
   	            tsr.setServicename(service.getServiceName());
   	          /*tsr.setLayerid(layer.getId());
   	            tsr.setLayername(layer.getLayerName());*/
   	            tsr.setSortindex(sortindex);
   	            tsr.setCjr(model.getLoginUser());
   	            tsr.setCjsj(new Date());
   	            themeServiceRelateDao.insertThemeServiceRelateInfo(tsr);
   	            sortindex++;
   	        }
   			return ExecuteResult.success("服务专题绑定成功！");
   		} catch (Exception e) {
   			logger.error("服务专题绑定出现异常，详情：",e);
   			throw new RuntimeException("服务专题绑定失败，原因 【"+e.getMessage()+"】");
   		}
	}
    

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ExecuteResult<String> serviceThemeUnBind(List<ThemeServiceBindDto> models) {
		try {
   			String checkErr = checkUnBindInfo(models);
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			for (ThemeServiceBindDto model : models) {
   	            Map<String, Object> map = new HashMap<String, Object>();
   	            map.put("themeId", model.getThemeId());
   	            List<BindServiceLayerVo> services = model.getServices();
   	            if(services!=null&&services.size()>0) {
   	            	for (int i = 0; i < services.size(); i++) {
   	   	            	BindServiceLayerVo service = services.get(i);
   	   	                String serviceId = service.getServiceId();
   	   	                if(serviceId.contains("-")) {
   	   	                	String[] ids = serviceId.split("-");
   	   	                	serviceId = ids[1];
   	   	                	//由于前端传的专题id有误差，临时调整为如下方式获取
   	   	                	map.put("themeId",ids[0]);
   	   	                }
   	   	                //String id = layer.getId();
   	   	                map.put("serviceId",serviceId);
   	   	                /*if(!StringUtils.isEmpty(id)){
   	   	                    map.put("layerId", id);
   	   	                    themeServiceRelateDao.deleteObject(ZyThemeServiceRelateModel.class, "THEMEID=:themeId AND LAYERID=:layerId AND SERVICEID=:serviceId ", map);
   	   	                }else {*/
   	   	                    int count = themeServiceRelateDao.deleteObject(ZyThemeServiceRelateModel.class, "THEMEID=:themeId AND SERVICEID=:serviceId ", map);
	   	   	               if(count<1) {
	   	   	            	   throw new Exception("解绑失败-根据入参未发现对应绑定记录");
	   	   	               }
   	   	                    //}
   	   	            }
   	            }
   	            
   	        }
   			return ExecuteResult.success("服务专题解绑成功！");
   		} catch (Exception e) {
   			logger.error("服务专题解绑出现异常，详情：",e);
   			throw new RuntimeException("服务专题解绑失败，原因 【"+e.getMessage()+"】");
   		}
	}
	
	@Override
	public ExecuteResult<List<ThemeWithServiceLayersVo>> queryThemeBindLayersInfoById(String themeId) {
		try {
   			if (StringUtil.isEmpty(themeId)) {
   				return ExecuteResult.error("数据专题资源ID入参不能为空值", null);
   			}
   			logger.info("根据ID查询数据专题资源信息，记录id【{}】", themeId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("themeId", themeId);
   			ZyThemeModel themeModel = themeResourceDao.queryObject(ZyThemeModel.class,"ID=:themeId", paramsOne);
   			if (themeModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应数据专题资源信息!", null);
   			}
   			
   			List<ZyThemeServiceRelateModel> tsrs = themeServiceRelateDao.queryObjects(ZyThemeServiceRelateModel.class,"THEMEID=:themeId",paramsOne);
   			List<ThemeWithServiceLayersVo> themeWithLayersViewList = new ArrayList<ThemeWithServiceLayersVo>();
   			if(tsrs!=null&&tsrs.size()>0){
                List<ZyThemeServiceRelateModel> themeServiceRelateModelArrayList = removeDuplicateUser(tsrs);
                for(ZyThemeServiceRelateModel serviceRelateModel:themeServiceRelateModelArrayList){
                	ThemeWithServiceLayersVo themeWithLayersView = new ThemeWithServiceLayersVo();
                    themeWithLayersView.setThemeId(themeModel.getId());
                    themeWithLayersView.setThemeName(themeModel.getThemeName());
                    themeWithLayersView.setRelateId(serviceRelateModel.getId());
                    themeWithLayersView.setSortIndex(serviceRelateModel.getSortindex());
                    String serviceId = serviceRelateModel.getServiceid();
                    Map<String,Object> map = new HashMap<>();
                    map.put("id",serviceId);
                    ZyServiceModel serviceModel = serviceResourceDao.queryObject(ZyServiceModel.class,"id=:id",map);
                    JSONObject extentMeta = JSONObject.parseObject(serviceModel.getExtentMeta());
                    JSONObject object = new JSONObject();
                    object.put("metadata",extentMeta);
                    themeWithLayersView.setMetaData(JSONObject.toJSONString(object));
                    themeWithLayersView.setServiceId(serviceId);
                    themeWithLayersView.setServiceName(serviceModel.getServiceName());
                    themeWithLayersView.setServiceType(serviceModel.getServiceType());
                    if("enable".equalsIgnoreCase(serviceConfig.getEnable())) {
                    	//启用代理转发模式
                    	themeWithLayersView.setServiceUrl(serviceConfig.getProxyForwardingUrl()+"/"+serviceId);
                    	
                    }else {
                    	themeWithLayersView.setServiceUrl(serviceModel.getServiceUrl());
                    	if(StringUtil.isNotEmpty(serviceModel.getServiceUrl())){
           	   		    	String decryptStr = KindCryptoUtils.decryptSM4(serviceModel.getServiceUrl(), 3,kindCryptoConfig.isEnable());
           	   		    	if(StringUtil.isNotEmpty(decryptStr)) {
           	   		    		themeWithLayersView.setServiceUrl(decryptStr);
           	   		    	}
           	   		    }
                    }
                    themeWithLayersView.setServiceConfidential(serviceModel.getConfidential());
                    themeWithLayersView.setServiceRemark(serviceModel.getDescription());
                   /* List<ZyLayerModel> layerModelList = new ArrayList<ZyLayerModel>();
                    for(ZyThemeServiceRelateModel serviceRelateModel1:tsrs){
                        if(serviceRelateModel1.getServiceid().equals(serviceId)){
                            String layerId = serviceRelateModel1.getLayerid();
                            Map<String,Object> stringObjectMap = new HashMap<>();
                            if(!StringUtils.isEmpty(layerId)){
                                stringObjectMap.put("id",layerId);
                                ZyLayerModel layerModel = serviceLayerDao.queryObject(ZyLayerModel.class,"id=:id",stringObjectMap);
                                layerModelList.add(layerModel);
                            }
                        }
                    }
                    if(layerModelList.size()>0){
                        themeWithLayersView.setIsHaveLayer(true);
                    }else {*/
                        themeWithLayersView.setIsHaveLayer(false);
                    //}
                    //themeWithLayersView.setLayers(layerModelList);
                    themeWithLayersViewList.add(themeWithLayersView);
                }
            }
   			return ExecuteResult.success("查询成功",themeWithLayersViewList);
   		} catch (Exception e) {
   			logger.error("根据ID【{}】查询数据专题资源信息出现异常，原因：【{}】,详情：", themeId,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	 /*
      *	集合根据字段去重
      */
    private static ArrayList<ZyThemeServiceRelateModel> removeDuplicateUser(List<ZyThemeServiceRelateModel> themeServiceList) {
        themeServiceList.sort(new Comparator<ZyThemeServiceRelateModel>() {
            @Override
            public int compare(ZyThemeServiceRelateModel o1, ZyThemeServiceRelateModel o2) {
                /*if(o1.getServiceid().equals(o2.getServiceid())){
                    if(o1.getLayerid()!=null && o2.getLayerid()!=null){
                        return o1.getLayerid().compareTo(o2.getLayerid());
                    }else {
                        return o1.getLayerid() == null ? -1:1;
                    }
                }else {*/
                    return o1.getServiceid().compareTo(o2.getServiceid());
                //}
            }
        });
        Set<ZyThemeServiceRelateModel> set = new TreeSet<ZyThemeServiceRelateModel>(new Comparator<ZyThemeServiceRelateModel>() {
            @Override
            public int compare(ZyThemeServiceRelateModel o1, ZyThemeServiceRelateModel o2) {
                //字符串,则按照asicc码升序排列
                return o1.getServiceid().compareTo(o2.getServiceid());
            }
        });
        set.addAll(themeServiceList);
        List<ZyThemeServiceRelateModel> themeServiceRelateModels = new ArrayList<ZyThemeServiceRelateModel>(set);
        Set<ZyThemeServiceRelateModel> setSort = new TreeSet<ZyThemeServiceRelateModel>(new Comparator<ZyThemeServiceRelateModel>() {
            @Override
            public int compare(ZyThemeServiceRelateModel o1, ZyThemeServiceRelateModel o2) {
                return o2.getSortindex().compareTo(o1.getSortindex());
            }
        });
        setSort.addAll(themeServiceRelateModels);
        return new ArrayList<ZyThemeServiceRelateModel>(setSort);
    }
    
	
    
	 //校验绑定接口的参数信息
  	 private String checkBindInfoForAdd(ThemeServiceBindDto model) {
  		 StringBuilder checkInfo = new StringBuilder("");
  		 if(StringUtil.isEmpty(model)) {
  			 checkInfo.append("ThemeServiceBindModel不能为空;");
  			 return checkInfo.toString();
  		 }
  		 if(StringUtil.isEmpty(model.getThemeId())) {
  	        checkInfo.append("专题ID【themeId】不能为空;");
  	     }
  		 if(StringUtil.isEmpty(model.getServices())) {
  			checkInfo.append("绑定的目标资源【services】不能为空;");
  		 }else {
  			List<BindServiceLayerVo> layers = model.getServices();
  			for(BindServiceLayerVo layer:layers) {
  				if(StringUtil.isEmpty(layer.getServiceId())) {
  					checkInfo.append("绑定的目标资源项的服务id【serviceId】存在空值;");
  				}
  			}
  		 }
  		 if(StringUtil.isEmpty(model.getLoginUser())) {
  			 checkInfo.append("当前操作人【loginUser】不能为空;");
  		 }
  		 return checkInfo.toString();
  		 
  	 }
    
    
  	//校验绑定接口的参数信息
  	 private String checkUnBindInfo(List<ThemeServiceBindDto> models) {
  		 StringBuilder checkInfo = new StringBuilder("");
  		 if(StringUtil.isEmpty(models)) {
  			 checkInfo.append("List<ThemeServiceBindModel>不能为空;");
  			 return checkInfo.toString();
  		 }
  		 for(ThemeServiceBindDto model:models) {
  			if(StringUtil.isEmpty(model.getThemeId())) {
  	  	        checkInfo.append("解绑的目标专题的ID【themeId】存在空值;");
  	  	     }
  	  		 if(StringUtil.isEmpty(model.getServices())) {
  	  			//checkInfo.append("解绑的目标专题的资源【layers】存在空值;");
  	  		 }else {
  	  			List<BindServiceLayerVo> services = model.getServices();
  	  			for(BindServiceLayerVo service:services) {
  	  				if(StringUtil.isEmpty(service.getServiceId())) {
  	  					checkInfo.append("解绑的目标专题的目标资源项的服务id【serviceId】存在空值;");
  	  				}
  	  			}
  	  		 }
  		 }
  		 return checkInfo.toString();
  		 
  	 }

    


}
