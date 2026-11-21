package com.santech.module.biz.service;

import java.util.List;
import java.util.Map;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.vo.ExposureTreeVo;

/**
 * @Title: ExposureDataService.java 
 * @Description：承灾体相关数据查询业务层接口类
 * @Version： V1.0
 * @Modify：   
 */
public interface ExposureDataService {
	
	/**
	  * @Title queryExposureTreeInfos 
	  * @Description (根据承灾体类型获取对应承灾体树信息)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
	ExecuteResult<List<ExposureTreeVo>> queryExposureTreeInfos(String exposureType);

	/**
	 * 列表查询（字段对齐前端 surveyBase 承灾体展示）
	 * @param exposureType 优先 ExposureTypeEnum：SEAWALL/FISHING_PORT/MARICULTURE/TOURIST_AREA；兼容旧别名
	 */
	ExecuteResult<List<Map<String, Object>>> queryExposureList(String exposureType, String keyword);
}
