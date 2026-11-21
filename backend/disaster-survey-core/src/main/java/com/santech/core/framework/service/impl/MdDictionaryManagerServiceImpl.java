package com.santech.core.framework.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.santech.core.framework.dao.MdDictDao;
import com.santech.core.framework.entity.model.JcDictionaryDetailModel;
import com.santech.core.framework.entity.model.JcDictionaryTypeModel;
import com.santech.core.framework.entity.view.DictionaryTypeQueryDto;
import com.santech.core.framework.service.MdDictionaryManagerService;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.redis.RedisUtil;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * @Title: MdDictionaryManagerServiceImpl.java 
 * @Description：字典运维业务层接口类 ，实现MdDictionaryManagerService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class MdDictionaryManagerServiceImpl implements MdDictionaryManagerService {
    private static final  Logger logger = LoggerFactory.getLogger(MdDictionaryManagerServiceImpl.class);

    @Resource
    private MdDictDao mdDictDao;
    @Resource
    private RedisUtil redisUtil;

	@Override
	public ExecuteResult<List<JcDictionaryTypeModel>> queryDictionaryTypeInfos(DictionaryTypeQueryDto query){
		logger.info("查询字典类型列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
        	if(StringUtil.isEmpty(query.getEq_sfqy())) {
        		//默认只查询启用的
        		query.setEq_sfqy("0");
        	}
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"ZDLXSX asc","");
            //根据拼接sql,调用dao层获取数据
            return mdDictDao.queryObjectsWithPageInfo(JcDictionaryTypeModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询字典类型列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}

	@Override
	public ExecuteResult<String> saveDictionaryTypeInfo(JcDictionaryTypeModel model) {
		try {
			//校验参数
			String checkResult = checkSaveDictionaryTypeParams(model);
			if(StringUtil.isNotEmpty(checkResult)) {
				logger.info("新增字典类型信息,参数校验不通过，详情：【{}】",checkResult);
				return ExecuteResult.error("入参校验不通过，详情："+checkResult,null);
			}
			logger.info("新增字典类型信息,入参：【{}】",model);
			//校验zdlxbm是否已经存在，存在进行错误提示 
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("zdlxbm", model.getZdlxbm());
			JcDictionaryTypeModel targetModel = mdDictDao.queryObject(JcDictionaryTypeModel.class,"zdlxbm=:zdlxbm", paramsOne);
			if (targetModel != null&&StringUtil.isNotEmpty(targetModel.getId())) {
				logger.info("已存在zdlxbm【{}】的字典类型信息，不能保存", model.getId());
				return ExecuteResult.error("该字典类型已存在!", null);
			}
			model.setId(UuidUtils.buildID("DIC"));
			model.setSfqy("0");//是否启用或删除标识，0启用,1不启用或已删除,默认启用
			if(model.getZdlxsx()==null) {
				model.setZdlxsx(1);
			}
			model.setZhgxr(model.getCjr());
			model.setCjsj(new Date());
			model.setZhgxsj(new Date());
			mdDictDao.insertMdDictionaryType(model);
			return ExecuteResult.success("保存成功");
		} catch (Exception e) {
			logger.error("新增字典类型信息出现异常，入参【{}】,详情：",model,e);
			return ExecuteResult.error("新增字典类型信息失败", null);
			
		}
	}

	

	@Override
	public ExecuteResult<String> updateDictionaryTypeInfo(JcDictionaryTypeModel model) {
		
		try {
			//校验参数
			logger.info("更新字典类型信息,入参：【{}】",model);
			String id = model.getId();
			if(StringUtil.isEmpty(id)) {
				logger.info("更新字典类型信息,ID不能为空！");
				return ExecuteResult.error("入参校验不通过，ID不能为空！",null);
			}
			//根据ID查询关联关系信息
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("id", id);
			JcDictionaryTypeModel targetModel = mdDictDao.queryObject(JcDictionaryTypeModel.class,"id=:id", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定字典类型信息", model.getId());
				return ExecuteResult.error("未能查到指定字典类型信息!", null);
			}
			//获取更新后的Model信息
			JcDictionaryTypeModel updateModel = getUpdateJcDictionaryTypeModel(targetModel,model);
			mdDictDao.updateMdDictionaryTypeByModel(updateModel);
			return ExecuteResult.success("更新成功");
		} catch (Exception e) {
			logger.error("更新字典类型信息出现异常，入参【{}】,详情：",model,e);
			return ExecuteResult.error("更新字典类型信息出现失败", null);
		}
	}

	@Override
	public ExecuteResult<String> deleteDictionaryTypeInfo(String dicTypeId) {
		try {
			if (StringUtil.isEmpty(dicTypeId)) {
				return ExecuteResult.error("入参不能为空值", null);
			}
			logger.info("删除字典类型信息，记录id【{}】", dicTypeId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("id", dicTypeId);
			JcDictionaryTypeModel targetModel = mdDictDao.queryObject(JcDictionaryTypeModel.class,"id=:id", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定字典类型信息", dicTypeId);
				return ExecuteResult.error("已无对应字典类型信息!", null);
			}
			targetModel.setSfqy("1");
			mdDictDao.updateMdDictionaryTypeByModel(targetModel);
			return ExecuteResult.success("删除字典类型记录成功");
		} catch (Exception e) {
			logger.error("删除字典类型记录【{}】出现异常，原因：【{}】,详情：", dicTypeId,e.getMessage(), e);
			return ExecuteResult.error("删除字典类型记录失败",null);
		}
	}
	
	
	@Override
	public ExecuteResult<List<JcDictionaryDetailModel>> queryDictionaryDetailInfo(String flid, String zdlxbm) {
		try {
    	    logger.info("根据字典类型id或编码查询包含的字典项信息,入参：flid【{}】zdlxbm【{}】",flid,zdlxbm);
    	   	//校验参数
    	    if(StringUtil.isEmpty(flid)&&StringUtil.isEmpty(zdlxbm)) {
    		   return ExecuteResult.error("分类ID和分类型代码不能均为空值", null);
    	    }
    	    //先从Redis进行获取
    	    if(StringUtil.isNotEmpty(zdlxbm)) {
    	    	List<Object> dicts = redisUtil.lGet("ZDCache_"+zdlxbm,0,-1);
    	    	if(dicts!=null&&dicts.size()>0) {
    	    		List<JcDictionaryDetailModel> rsults = JSON.parseArray(JSON.toJSONString(dicts.get(0)), JcDictionaryDetailModel.class);
    	    		return ExecuteResult.success("查询成功", rsults);
    	    	}
    	    }
    	   	//请求参数
		    Map<String,Object> params = new HashMap<String,Object>();
	        params.put("flid", flid);
	        params.put("zdlxbm", zdlxbm);
	        List<JcDictionaryDetailModel> model =  mdDictDao.queryObjects(JcDictionaryDetailModel.class, "flid=:flid or zdlxbm=:zdlxbm order by zdsx asc", params);
	        if(StringUtil.isNotEmpty(zdlxbm)&&StringUtil.isNotEmpty(model)) {
    	    	redisUtil.lSet("ZDCache_"+zdlxbm, model, 1*60*60);//将查询结果放入缓存并设置缓存时效1小时
    	    }
	        return ExecuteResult.success("查询成功", model);
        } catch (Exception e) {
			logger.error("根据字典类型id[{}]或字典类型编码[{}]，查询包含的字典项信息出现异常，详情：",flid,zdlxbm,e);
			return  ExecuteResult.error("查询失败",null);
		}
	}
	
	@Override
	public ExecuteResult<String> saveDictionaryDetailInfo(JcDictionaryDetailModel model) {
		try {
			//校验参数
			String checkResult = checkSaveDictionaryDetailParams(model);
			if(StringUtil.isNotEmpty(checkResult)) {
				logger.info("新增字典项信息,参数校验不通过，详情：【{}】",checkResult);
				return ExecuteResult.error("入参校验不通过，详情："+checkResult,null);
			}
			logger.info("新增字典项信息,入参：【{}】",model);
			//校验zdlxbm下的zdbm是否已经存在，存在进行错误提示 
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("zdlxbm", model.getZdlxbm());
			paramsOne.put("zdbm", model.getZdbm());
			JcDictionaryDetailModel targetModel = mdDictDao.queryObject(JcDictionaryDetailModel.class,"zdlxbm=:zdlxbm and zdbm=:zdbm", paramsOne);
			if (targetModel != null&&StringUtil.isNotEmpty(targetModel.getId())) {
				logger.info("字典类型编码【{}】下已存在zdbm【{}】的字典项信息，不能保存", model.getZdlxbm(),model.getId());
				return ExecuteResult.error("该字典项已存在!", null);
			}
			model.setId(UuidUtils.buildID("DICC"));
			if(model.getZdsx()==null) {
				model.setZdsx(1);
			}
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(new Date());
			mdDictDao.insertMdDictionaryDetail(model);
			return ExecuteResult.success("保存成功");
		} catch (Exception e) {
			logger.error("新增字典项信息出现异常，入参【{}】,详情：",model,e);
			return ExecuteResult.error("新增字典项信息失败", null);
		}
	}

	@Override
	public ExecuteResult<String> updateDictionaryDetailInfo(JcDictionaryDetailModel model) {
		try {
			//校验参数
			logger.info("更新字典项信息,入参：【{}】",model);
			String id = model.getId();
			if(StringUtil.isEmpty(id)) {
				logger.info("更新字典项信息,ID不能为空！");
				return ExecuteResult.error("入参校验不通过，ID不能为空！",null);
			}
			//根据ID查询关联关系信息
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("id", id);
			JcDictionaryDetailModel targetModel = mdDictDao.queryObject(JcDictionaryDetailModel.class,"id=:id", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定字典项信息", model.getId());
				return ExecuteResult.error("未能查到指定字典项信息!", null);
			}
			//获取更新后的Model信息
			JcDictionaryDetailModel updateModel = getUpdateJcDictionaryDetailModel(targetModel,model);
			mdDictDao.updateMdDictionaryDetailByModel(updateModel);
			return ExecuteResult.success("更新成功");
		} catch (Exception e) {
			logger.error("更新字典项信息出现异常，入参【{}】,详情：",model,e);
			return ExecuteResult.error("更新字典项信息出现失败", null);
		}
	}

	@Override
	public ExecuteResult<String> deleteDictionaryDetailInfo(String dicId) {
		try {
			if (StringUtil.isEmpty(dicId)) {
				return ExecuteResult.error("入参不能为空值", null);
			}
			logger.info("删除字典项信息，记录id【{}】", dicId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("id", dicId);
			JcDictionaryDetailModel targetModel = mdDictDao.queryObject(JcDictionaryDetailModel.class,"id=:id", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定字典项信息", dicId);
				return ExecuteResult.error("已无对应字典项信息!", null);
			}
			mdDictDao.deleteObject(JcDictionaryDetailModel.class , "id=:id", paramsOne);
			return ExecuteResult.success("删除字典项成功");
		} catch (Exception e) {
			logger.error("删除字典项【{}】出现异常，原因：【{}】,详情：", dicId,e.getMessage(), e);
			return ExecuteResult.error("删除字典项记录失败",null);
		}
	}

	//获取更新后的字典类型信息
	 private JcDictionaryTypeModel getUpdateJcDictionaryTypeModel(JcDictionaryTypeModel originalModel,JcDictionaryTypeModel paramModel) {
		 
		 /*if(StringUtil.isNotEmpty(paramModel.getZdlxbm())) {
			originalModel.setZdlxbm(paramModel.getZdlxbm());
		 }*/
		 if(StringUtil.isNotEmpty(paramModel.getZdlxmc())) {
			originalModel.setZdlxmc(paramModel.getZdlxmc());
		 }
		 if(StringUtil.isNotEmpty(paramModel.getZdlxsx())&&paramModel.getZdlxsx()!=0) {
				originalModel.setZdlxsx(paramModel.getZdlxsx());
		 }
		 if(StringUtil.isNotEmpty(paramModel.getBz())) {
			originalModel.setBz(paramModel.getBz());
		 }
		 originalModel.setZhgxsj(new Date());
		 return originalModel;
		 
	 }
	
	//校验新增的字典类型信息
	 private String checkSaveDictionaryTypeParams(JcDictionaryTypeModel model) {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("JcDictionaryTypeModel不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getZdlxbm())) {
			 checkInfo.append("字典类型编码【zdlxbm】不能为空;");		 
		 }
		 if(StringUtil.isEmpty(model.getZdlxmc())) {
			 checkInfo.append("字典类型名称【zdlxmc】不能为空;");
		 }
		 /*if(StringUtil.isEmpty(model.getZdlxsx())) {
			 checkInfo.append("字典类型顺序【zdlxsx】不能为空;");
		 }*/
		 return checkInfo.toString();
		 
	 }

	//获取更新后的字典项信息
	 private JcDictionaryDetailModel getUpdateJcDictionaryDetailModel(JcDictionaryDetailModel originalModel,JcDictionaryDetailModel paramModel) {
		 
		 if(StringUtil.isNotEmpty(paramModel.getZdbm())) {
			originalModel.setZdbm(paramModel.getZdbm());
		 }
		 if(StringUtil.isNotEmpty(paramModel.getZdz())) {
				originalModel.setZdz(paramModel.getZdz());
		 }
		 if(StringUtil.isNotEmpty(paramModel.getZdsx())&&paramModel.getZdsx()!=0) {
				originalModel.setZdsx(paramModel.getZdsx());
		 }
		 if(StringUtil.isNotEmpty(paramModel.getBz())) {
			originalModel.setBz(paramModel.getBz());
		 }
		 if(StringUtil.isNotEmpty(paramModel.getZhgxr())) {
			originalModel.setZhgxr(paramModel.getZhgxr());
		 }
		 originalModel.setZhgxsj(new Date());
		 return originalModel;
		 
	 }
	
	//校验新增的字典项信息
	 private String checkSaveDictionaryDetailParams(JcDictionaryDetailModel model) {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("JcDictionaryDetailModel不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getZdlxbm())) {
			 checkInfo.append("所属字典类型编码【zdlxbm】不能为空;");		 
		 }
		 if(StringUtil.isEmpty(model.getFlid())) {
			 checkInfo.append("所属字典类型id【flid】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getZdbm())) {
			 checkInfo.append("字典编码【zdbm】不能为空;");		 
		 }
		 if(StringUtil.isEmpty(model.getZdz())) {
			 checkInfo.append("字典值【zdz】不能为空;");		 
		 }
		 if(StringUtil.isEmpty(model.getZdsx())) {
			 checkInfo.append("字典顺序【zdsx】不能为空;");
		 }
		 return checkInfo.toString();
		 
	 }
	    	
    

}
