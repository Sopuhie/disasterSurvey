package com.santech.module.biz.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.santech.core.framework.config.KindCryptoConfig;
import com.santech.core.framework.dao.SystemConfigDao;
import com.santech.core.framework.entity.model.JcSystemConfigModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.kindcrypto.KindCryptoUtils;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.SystemConfigAddDto;
import com.santech.module.biz.entity.dto.SystemConfigQueryDto;
import com.santech.module.biz.entity.dto.SystemConfigUpdateDto;
import com.santech.module.biz.service.SystemConfigService;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {
	private static final  Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);
	@Resource
 	private KindCryptoConfig kindCryptoConfig;
    @Resource
    private SystemConfigDao systemConfigDao;

    @Override
   	public ExecuteResult<List<JcSystemConfigModel>> querySystemConfigInfosWithPage(SystemConfigQueryDto query) {
       	logger.info("查询系统基础配置列表信息，入参：【{}】", query);
           PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
           try {
               //请求参数
               Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
               //拼接查询sql
               Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SORTINDEX asc","");
               //根据拼接sql,调用dao层获取数据
               return systemConfigDao.queryObjectsWithPageInfo(JcSystemConfigModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
           } catch (Exception e) {
               logger.error("查询系统基础配置列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
               return ExecuteResult.error("查询失败",null);
           }
   	}
    
    @Override
	public ExecuteResult<JcSystemConfigModel> querySystemConfigInfoByName(String configName) {
    	try {
   			if(StringUtil.isEmpty(configName)) {
       			return ExecuteResult.error("系统配置名称不能为空", null);
       		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("name",configName);
   		    JcSystemConfigModel configModel = systemConfigDao.queryObject(JcSystemConfigModel.class,"NAME=:name",paramsMap);
   		    if (StringUtil.isEmpty(configModel)||StringUtil.isEmpty(configModel.getId())) {
   		    	return ExecuteResult.error("未查到对应名称的系统配置信息！", null);
   		    }
   		    if(StringUtil.isNotEmpty(configModel.getSign())) {
				//获取完整性加密串，TITLE 系统名称，STYLE 系统风格
				StringBuilder dataStr = new StringBuilder("");
				dataStr.append( configModel.getTitle()).append(";");
				dataStr.append( configModel.getStyle()).append(";");
				boolean verify = KindCryptoUtils.verifySignData(kindCryptoConfig.getAppname(),dataStr.toString(),configModel.getSign(),configModel.getPubKeyCert(),kindCryptoConfig.isEnable());
				if(!verify) {
					return ExecuteResult.error("系统配置信息【"+configModel.getId()+"】被篡改，验签未通过，请核实", null);
				}
			}
   			return ExecuteResult.success("查询成功！",configModel);
   		} catch (Exception e) {
   			logger.error("根据名称查询系统基础配置信息异常，详情：",e);
   			return ExecuteResult.error("查询异常，请稍后再试！", null);
   		}
	}
       
    @Override
   	public ExecuteResult<String> saveSystemConfigInfo(SystemConfigAddDto dto) {
       	try {
   			String checkErr = checkSystemConfigInfoForAdd(dto) ;
   			if(StringUtil.isNotEmpty(checkErr)) {
       			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
       		}
   			
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("name",dto.getName());
   		    List<JcSystemConfigModel> configModels = systemConfigDao.queryObjects(JcSystemConfigModel.class,"NAME=:name",paramsMap);
   		    if (ObjectUtil.isNotEmpty(configModels)&&configModels.size()>0) {
   		    	return ExecuteResult.error("系统基础配置名称不能重复！", null);
   		    }
   		    //保存的底图信息的值为字典编码，字典编码的来源是具体资源管理的专题id，字典值为对应专题的名称，即字典配置的时候需要和底图专题要相对应
   		    JcSystemConfigModel model = new JcSystemConfigModel();
   		    BeanUtils.copyProperties(dto, model);
   		    model.setId(UuidUtils.buildID("SC"));
   			model.setCjsj(new Date());
   			model.setZhgxr(model.getCjr());
   			model.setZhgxsj(model.getCjsj());
   			systemConfigDao.insertSystemConfigInfo(model,kindCryptoConfig);
   			return ExecuteResult.success("系统基础配置项添加成功！");
   		} catch (Exception e) {
   			logger.error("新增系统基础配置项异常，详情：",e);
   			return ExecuteResult.error("新增系统基础配置项异常，请稍后再试！", null);
   		}
   	}

   	@Override
   	public ExecuteResult<String> updateSystemConfigInfo(SystemConfigUpdateDto dto) {
   		try {
   			String systemConfigId = dto.getId();
   			if(StringUtils.isEmpty(systemConfigId)) {
       			return ExecuteResult.error("系统基础配置项ID不能为空！", null);
       		}
   			Map<String,Object> paramsMap = new HashMap<String,Object>();
   		    paramsMap.put("id",systemConfigId);
   		    JcSystemConfigModel originSystemConfig = systemConfigDao.queryObject(JcSystemConfigModel.class,"ID=:id",paramsMap);
   		    if (StringUtil.isEmpty(originSystemConfig)) {
   		    	return ExecuteResult.error("未找到对应系统基础配置项，无法完成更新！", null);
   		    }
   		    //检查系统基础配置项是否包含重名
   		    if(StringUtil.isNotEmpty(dto.getName())&&!(dto.getName().equals(originSystemConfig.getName()))) {
   			    paramsMap.put("name",dto.getName());
   			    List<JcSystemConfigModel> systemConfigModels = systemConfigDao.queryObjects(JcSystemConfigModel.class,"ID!=:id and NAME=:name",paramsMap);
   			    if (StringUtil.isNotEmpty(systemConfigModels)&&systemConfigModels.size()>0) {
   			    	return ExecuteResult.error("要更新的系统基础配置项名称已占用！", null);
   			    }
   		    }
   		    JcSystemConfigModel systemConfig = getSystemConfigModelByUpdateInfo(originSystemConfig,dto);
   		    systemConfigDao.updateSystemConfigInfo(systemConfig,kindCryptoConfig);
   			return ExecuteResult.success("系统基础配置项更新成功！");
   		} catch (Exception e) {
   			logger.error("更新系统基础配置项异常，详情：",e);
   			return ExecuteResult.error("更新系统基础配置项异常，原因：【"+e.getMessage()+"】",null);
   		}
   	}

   	@Override
   	public ExecuteResult<String> deleteSystemConfigInfo(String systemConfigId, String loginUser) {
   		try {
   			if (StringUtil.isEmpty(systemConfigId)) {
   				return ExecuteResult.error("系统基础配置项ID入参不能为空值", null);
   			}
   			logger.info("删除系统基础配置项信息，记录id【{}】", systemConfigId);
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("id", systemConfigId);
   			JcSystemConfigModel targetModel = systemConfigDao.queryObject(JcSystemConfigModel.class,"ID=:id", paramsOne);
   			if (targetModel == null) {
   				logger.info("根据ID【{}】未能查到指定系统基础配置项信息", systemConfigId);
   				return ExecuteResult.error("已无对应系统基础配置项信息!", null);
   			}
   			int count1 = systemConfigDao.deleteSystemConfigInfoById(systemConfigId);
   			if (count1> 0) {
   				return ExecuteResult.success("删除成功 ");
   			}else {
   				return  ExecuteResult.error("删除系统基础配置项失败",null);
   			}
   		} catch (Exception e) {
   			logger.error("删除系统基础配置项【{}】出现异常，原因：【{}】,详情：", systemConfigId,e.getMessage(), e);
   			return  ExecuteResult.error("删除失败，原因 【"+e.getMessage()+"】",null);
   		}
   	}
   	
    
    //校验新增的系统基础配置信息
 	 private String checkSystemConfigInfoForAdd(SystemConfigAddDto model) throws Exception {
 		 StringBuilder checkInfo = new StringBuilder("");
 		 if(StringUtil.isEmpty(model)) {
 			 checkInfo.append("SystemConfigAddDto不能为空;");
 			 return checkInfo.toString();
 		 }
 		 if(StringUtil.isEmpty(model.getName())) {
			 checkInfo.append("配置名称【name】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getAliasName())) {
			 checkInfo.append("配置别名【AliasName】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getTitle())) {
 			 checkInfo.append("系统标题【title】不能为空;");
 		 }
 		 if(StringUtil.isEmpty(model.getStyle())) {
			 checkInfo.append("系统风格【style】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getMapTheme())) {
 			 checkInfo.append("地图底图专题【mapTheme】不能为空;");
 		 }
 		 /*if(StringUtil.isEmpty(model.getImageTheme())) {
 			 checkInfo.append("影像底图专题【imageTheme】不能为空;");
 		 }*/
 		 if(StringUtil.isEmpty(model.getCode())) {
			 checkInfo.append("空间参考标识，EPSG【code】不能为空;");
		 }
 		 /*if(StringUtil.isEmpty(model.getProjection())) {
			 checkInfo.append("空间参考【projection】不能为空;");
		 }*/
 		 if(StringUtil.isEmpty(model.getMaxZoom())) {
			 checkInfo.append("最大级别【maxZoom】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getMinZoom())) {
			 checkInfo.append("最小级别【minZoom】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getZoom())) {
			 checkInfo.append("初始级别【zoom】不能为空;");
		 }
 		 /*if(StringUtil.isEmpty(model.getExtent())) {
			 checkInfo.append("空间范围【extent】不能为空;");
		 }*/
 		 if(StringUtil.isEmpty(model.getCenter())) {
			 checkInfo.append("中心点【center】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getUnit())) {
			 checkInfo.append("单位【unit】不能为空;");
		 }
 		 if(StringUtil.isEmpty(model.getCjr())) {
 			 checkInfo.append("创建人【cjr】不能为空;");
 		 }
 		 if(StringUtil.isEmpty(model.getSortindex())||model.getSortindex()<1) {
			//如果为空，复制为默认的值（库中最大排序加1）
			 model.setSortindex(systemConfigDao.getObjectSortIndexForIntegerType("T_JC_SYSTEMCONFIG", "SORTINDEX"));
		 }
 		 return checkInfo.toString();
 		 
 	 }
 	 
 	 private JcSystemConfigModel getSystemConfigModelByUpdateInfo(JcSystemConfigModel originSystemConfig,SystemConfigUpdateDto updateSystemConfig) {
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getName())) {
 			originSystemConfig.setName(updateSystemConfig.getName());
 		 }	
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getAliasName())) {
 			originSystemConfig.setAliasName(updateSystemConfig.getAliasName());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getTitle())) {
 			originSystemConfig.setTitle(updateSystemConfig.getTitle());
 		 }
 		 
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getLogo())) {
 			originSystemConfig.setLogo(updateSystemConfig.getLogo());
 		 }
 		 
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getMapTheme())) {
 			originSystemConfig.setMapTheme(updateSystemConfig.getMapTheme());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getImageTheme())) {
 			originSystemConfig.setImageTheme(updateSystemConfig.getImageTheme());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getMap3dTheme())) {
 			originSystemConfig.setMap3dTheme(updateSystemConfig.getMap3dTheme());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getCode())) {
 			originSystemConfig.setCode(updateSystemConfig.getCode());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getProjection())) {
 			originSystemConfig.setProjection(updateSystemConfig.getProjection());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getMaxZoom())) {
 			originSystemConfig.setMaxZoom(updateSystemConfig.getMaxZoom());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getMinZoom())) {
 			originSystemConfig.setMinZoom(updateSystemConfig.getMinZoom());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getZoom())) {
 			originSystemConfig.setZoom(updateSystemConfig.getZoom());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getExtent())) {
 			originSystemConfig.setExtent(updateSystemConfig.getExtent());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getCenter())) {
 			originSystemConfig.setCenter(updateSystemConfig.getCenter());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getUnit())) {
 			originSystemConfig.setUnit(updateSystemConfig.getUnit());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getResolutions())) {
 			originSystemConfig.setResolutions(updateSystemConfig.getResolutions());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getOrigin())) {
 			originSystemConfig.setOrigin(updateSystemConfig.getOrigin());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getSortindex())) {
 			originSystemConfig.setSortindex(updateSystemConfig.getSortindex());
 		 }
 		 if(StringUtil.isNotEmpty(updateSystemConfig.getZhgxr())) {
 			originSystemConfig.setZhgxr(updateSystemConfig.getZhgxr());
  		 }
 		 originSystemConfig.setZhgxsj(new Date());
 		return originSystemConfig;
 	}

	

}
