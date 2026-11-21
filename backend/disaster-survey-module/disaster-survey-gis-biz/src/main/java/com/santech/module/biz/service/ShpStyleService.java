package com.santech.module.biz.service;

import com.santech.framework.global.ExecuteResult;

public interface ShpStyleService {
	
	
	/**
	  * @Title queryShpStyleXmlInfoByName 
	  * @Description (根据Shp数据样式名称查询对应Shp数据样式xml信息)
	  * @param styleName (Shp数据样式名称)
	  * @return (接口的执行情况及符合条件记录的XML信息)
	  */
	ExecuteResult<String> queryShpStyleXmlInfoByName(String styleName);

}
