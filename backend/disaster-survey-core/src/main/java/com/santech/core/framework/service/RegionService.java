package com.santech.core.framework.service;

import java.util.List;

import com.santech.core.framework.entity.model.QxRegionModel;
import com.santech.core.framework.entity.model.QxRegionSimpleModel;
import com.santech.core.framework.entity.view.RegionQueryDto;
import com.santech.framework.global.ExecuteResult;
/**
 * @Title: RegionService.java 
 * @Description：行政区业务层接口类 
 * @Version： V1.0
 * @Modify：   
 */
public interface RegionService {
	/**
	 * @Title showRegionTree
	 * @Description (此为行政区目录树接口具体业务逻辑处理方法)
	 * @param regionLevel 行政区级别，多个以英文逗号分割，如市级,区级,镇级,村级
	 * @return (行政区树信息)queryRegionWithPage
	 */
	ExecuteResult<List<QxRegionSimpleModel>> showRegionTree(String regionLevel);
	
	/**
	 * @Title queryRegionWithPage
	 * @Description (此为分页查询行政区信息接口具体业务逻辑处理方法)
	 * @param queryModel (RegionQueryDto,检索参数)
	 * @return (行政区树信息)
	 */
	ExecuteResult<List<QxRegionModel>> queryRegionWithPage(RegionQueryDto queryModel);
	
	/**
	 * @Title saveRegionData
	 * @Description (此为新增或保存行政区信息接口具体业务逻辑处理方法)
	 * @param regionModel （QxRegionModel，要更新的信息）
	 * @return (执行结果)
	 */
    ExecuteResult<Integer> saveRegionData(QxRegionModel regionModel);


	/**
	 * @Title deleteRegionData
	 * @Description (此为删除行政区信息接口具体业务逻辑处理方法)
	 * @param xzqdm 行政区代码
	 * @return (执行结果)
	 */
	ExecuteResult<String> deleteRegionData(String xzqdm);

	
}
