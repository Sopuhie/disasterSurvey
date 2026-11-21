package com.santech.module.biz.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.santech.core.framework.dao.ShpStyleResourceDao;
import com.santech.core.framework.entity.model.ZyServiceStyleModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.module.biz.service.ShpStyleService;

@Service
public class ShpStyleServiceImpl implements ShpStyleService {
	private static final  Logger logger = LoggerFactory.getLogger(ShpStyleServiceImpl.class);
	
    @Resource
    private ShpStyleResourceDao shpStyleResourceDao;
    
	@Override
	public ExecuteResult<String> queryShpStyleXmlInfoByName(String styleName) {
		try {
   			if (StringUtil.isEmpty(styleName)) {
   				return ExecuteResult.error("Shp数据样式资源ID入参不能为空值", null);
   			}
   			Map<String, Object> paramsOne = new HashMap<String, Object>();
   			paramsOne.put("styleName", styleName);
   			ZyServiceStyleModel targetModel = shpStyleResourceDao.queryObject(ZyServiceStyleModel.class,"NAME=:styleName", paramsOne);
   			if (targetModel == null) {
   				return ExecuteResult.error("根据ID未查询到对应Shp数据样式资源记录信息!", null);
   			}else {
   				if("1".equals(targetModel.getStatus())) {
   					return ExecuteResult.error("对应Shp数据样式资源已禁用，请联系管理员!", null);
   				}
   				String xmlContent = targetModel.getContent();
   				if(StringUtil.isNotEmpty(xmlContent)) {
   					return ExecuteResult.success("查询成功",xmlContent);
   				}else {
   					return ExecuteResult.error("对应Shp数据样式资源暂未添加xml样式信息!", null);
   				}
   			}
   		} catch (Exception e) {
   			logger.error("根据名称【{}】查询Shp数据样式XML信息出现异常，原因：【{}】,详情：", styleName,e.getMessage(), e);
   			return  ExecuteResult.error("查询失败，原因 【"+e.getMessage()+"】",null);
   		}
	}
	
   	 

	

}
