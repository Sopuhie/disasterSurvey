package com.santech.core.framework.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.dao.RegionDao;
import com.santech.core.framework.entity.model.QxRegionModel;
import com.santech.core.framework.entity.model.QxRegionSimpleModel;
import com.santech.core.framework.entity.view.RegionQueryDto;
import com.santech.core.framework.service.RegionService;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;

import cn.hutool.core.util.ObjectUtil;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;


/**
 * @Title: RegionServiceImpl.java 
 * @Description：行政区业务层接口类 ，实现RegionService.java接口类
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class RegionServiceImpl implements RegionService {
	
	public static final Logger logger = LoggerFactory.getLogger(RegionServiceImpl.class);

	@Resource
	private RegionDao regionDao;

	
	@Override
	public ExecuteResult<List<QxRegionSimpleModel>> showRegionTree(String regionLevel)  {
		try {
			List<QxRegionSimpleModel> all = new ArrayList<QxRegionSimpleModel>();
			List<QxRegionSimpleModel> nodeList = regionDao.selectRegionlist(regionLevel);
			all=nodeList.stream().filter(t->
					"-1".equals(t.getParentCode())
			).map((t)->{
				t.setChildren(getRegionTree(t,nodeList));
				return t;
			}).collect(Collectors.toList());
			return ExecuteResult.success("查询成功",all);
		} catch (Exception e) {
			logger.error("行政区查询异常，原因：【{}】,详情：",e.getMessage(),e);
			return ExecuteResult.error("查询失败",null);
		}finally {
			System.gc();
		}
	}
	@Override
	public ExecuteResult<List<QxRegionModel>> queryRegionWithPage(RegionQueryDto regionQueryModel) {
		logger.info("查询行政区信息,入参【{}】",regionQueryModel);
		try{
			PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(regionQueryModel.getCurrent()),SqlUtil.getPageSize(regionQueryModel.getSize()));
			//请求参数
			Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(regionQueryModel);
			//拼接查询sql
			String otherSql = "";
			Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SORTINDEX asc",otherSql);
			if("1".equals(regionQueryModel.getHasRange())) {
				//全量查询，带着数据范围信息
				return regionDao.queryObjectsWithPageInfo(QxRegionModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
				
			}else {
				//不带范围信息
				String querySql = "select r.ID,r.CODE,r.PARENTCODE,r.NAME,r.ALIAS,r.REGIONLEVEL,r.SORTINDEX,r.REMARK from T_QX_REGION r ";
				if (ObjectUtil.isNotEmpty(dbParams)||StringUtil.isNotEmpty(otherSql)) {
	                querySql = querySql + " where " + (String)dbParams.get("sql");
	            } else {
	                querySql = querySql + (String)dbParams.get("sql");
	            }
	            ExecuteResult<List<Map<String,Object>>> listMapResult = regionDao.selectRegionInfos(querySql,(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
	            if("S".equals(listMapResult.getResultCode())) {
	            	 List<QxRegionModel> datas = JSONObject.parseArray(JSONObject.toJSONString(listMapResult.getData()),QxRegionModel.class);
	            	 return ExecuteResult.success("查询成功", datas, listMapResult.getPageInfo());
	            }else {
	            	return ExecuteResult.error("查询失败",null);
	            }
			}
		}catch (Exception e){
			logger.error("查询行政区信息异常,异常原因【{}】,异常详情：",e.getMessage(),e);
			return ExecuteResult.error("查询失败",null);
		}
	}
	@Override
	public ExecuteResult<Integer> saveRegionData(QxRegionModel regionModel) {
		logger.info("保存行政区信息,入参【{}】", regionModel);
		try{
			if(StringUtil.isEmpty(regionModel.getCode())){
				return ExecuteResult.error("行政区代码不能为空",null);
			}
			if(StringUtil.isEmpty(regionModel.getName())){
				return ExecuteResult.error("行政区名称不能为空",null);
			}
			if(StringUtil.isEmpty(regionModel.getRegionLevel())){
				return ExecuteResult.error("行政区级别不能为空",null);
			}
			if(StringUtil.isEmpty(regionModel.getParentCode())){
				regionModel.setRegionLevel("-1");
			}
			String xzqdm = regionModel.getCode();
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("xzqdm",xzqdm);
			List<QxRegionSimpleModel> lyXzqhdmModels = regionDao.queryObjects(QxRegionSimpleModel.class, "code=:xzqdm", params);
			if(StringUtil.isNotEmpty(lyXzqhdmModels)){
				return ExecuteResult.error("行政区代码【"+xzqdm+"】已存在",null);
			}
			int count = regionDao.save(regionModel);
			return ExecuteResult.success("保存成功",count);
		}catch (Exception e){
			logger.error("保存行政区信息异常,异常原因【{}】,异常详情：",e.getMessage(),e);
			return ExecuteResult.error("保存失败",null);
		}
	}


	/**
	 * 删除行政区信息
	 * @param xzqdm
	 * @return
	 */
	@Override
	public ExecuteResult<String> deleteRegionData(String xzqdm) {
		logger.info("删除数删除行政区信息,入参【{}】", xzqdm);
		try{
			if(StringUtil.isEmpty(xzqdm)){
				return ExecuteResult.error("行政区不能为空!",null);
			}
			Map<String,Object> params = new HashMap<>();
			params.put("xzqdm",xzqdm);
			regionDao.deleteObject(QxRegionModel.class, "code=:xzqdm", params);
			return ExecuteResult.success("删除成功");
		}catch (Exception e){
			logger.error("删除数删除行政区信息,异常原因【{}】,异常详情：",e.getMessage(),e);
			return ExecuteResult.error("删除失败",null);
		}
	}

	

	/**
	 * @Title getLyXzqhdm
	 * @Description （递归获取行政区子孙树信息）
	 * @param root	父节点
	 * @param all	所有行政区信息
	 * @return (接口的执行情况)
	 */
	private List<QxRegionSimpleModel> getRegionTree(QxRegionSimpleModel root,List<QxRegionSimpleModel> all){
		List<QxRegionSimpleModel> children=all.stream().filter(t -> {
			return Objects.equals(t.getParentCode(),root.getCode());
		}).map((t)->{
			t.setChildren(getRegionTree(t,all));
			return t;
		}).collect(Collectors.toList());
		//20230608优化代码，让前端级联菜单不显示空间的级联，故置位空，不以空数组返回
		if(children!=null&&children.size()<1) {
			children = null;
		}
		return children;
	}
	
    
}
