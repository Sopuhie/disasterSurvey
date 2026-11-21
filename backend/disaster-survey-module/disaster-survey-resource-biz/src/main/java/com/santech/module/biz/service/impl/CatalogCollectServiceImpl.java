package com.santech.module.biz.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.santech.core.framework.dao.CatalogCollectDao;
import com.santech.core.framework.entity.model.ZyCatalogCollectModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.CatalogCollectAddDto;
import com.santech.module.biz.entity.dto.CatalogCollectQueryDto;
import com.santech.module.biz.entity.dto.CatalogCollectUpdateDto;
import com.santech.module.biz.service.CatalogCollectService;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Service
public class CatalogCollectServiceImpl implements CatalogCollectService {
	private static final  Logger logger = LoggerFactory.getLogger(CatalogCollectServiceImpl.class);
	
    @Autowired
    private CatalogCollectDao catalogCollectDao;
    
    
    @Override
	public ExecuteResult<List<ZyCatalogCollectModel>> queryCatalogCollectInfosWithPage(CatalogCollectQueryDto query) {
		logger.info("查询专题目录收藏列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
        	if(StringUtil.isEmpty(query.getLoginUser())) {
        		return ExecuteResult.error("当前登录人不能为空", null);
        	}
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            params.put("eq_cjr", query.getLoginUser());
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"cjsj desc","");
            //根据拼接sql,调用dao层获取数据
            return catalogCollectDao.queryObjectsWithPageInfo(ZyCatalogCollectModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询专题目录收藏列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}
    
    
    @Override
	public ExecuteResult<String> saveCatalogCollectInfo(CatalogCollectAddDto dto) {
    	try {
			String checkErr = checkCatalogCollectInfoForAdd(dto) ;
			if(StringUtil.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
		    ZyCatalogCollectModel model = new ZyCatalogCollectModel();
		    BeanUtils.copyProperties(dto, model);
		    model.setId(UuidUtils.buildID("CC"));
			model.setCjsj(new Date());
			int i = catalogCollectDao.insertCatalogCollectInfo(model);
			if(i>0) {
				return ExecuteResult.success("专题目录收藏添加成功！");
			}else {
				return ExecuteResult.error("专题目录收藏添加失败！",null);
			}
			
		} catch (Exception e) {
			logger.error("新增专题目录收藏异常，详情：",e);
			return ExecuteResult.error("新增专题目录收藏异常，请稍后再试！",null);
		}
	}
    
    @Override
	public ExecuteResult<String> updateCatalogCollectInfo(CatalogCollectUpdateDto dto) {
    	try {
			String collectId = dto.getId();
			if(StringUtils.isEmpty(collectId)) {
    			return ExecuteResult.error("专题目录收藏ID不能为空！", null);
    		}
			if(StringUtil.isEmpty(dto.getLoginUser())) {
				return ExecuteResult.error("数据更新人不能为空！", null);
	 		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("collectId",collectId);
		    ZyCatalogCollectModel origin = catalogCollectDao.queryObject(ZyCatalogCollectModel.class,"ID=:collectId",paramsMap);
		    if (StringUtil.isEmpty(origin)) {
		    	return ExecuteResult.error("未找到对应专题目录收藏项，无法完成更新！", null);
		    }
		    if(!(dto.getLoginUser().equalsIgnoreCase(origin.getCjr()))) {
		    	return ExecuteResult.error("非专题目录收藏者，不能进行更新！", null);
		    }
		    ZyCatalogCollectModel service = getCatalogCollectModelByUpdateInfo(origin,dto);
		    catalogCollectDao.updateCatalogCollectInfo(service);
			return ExecuteResult.success("专题目录收藏更新成功！");
		} catch (Exception e) {
			logger.error("更新专题目录收藏异常，详情：",e);
			return ExecuteResult.error("更新专题目录收藏异常，请稍后再试！", null);
		}
	}


	@Override
	public ExecuteResult<String> deleteCatalogCollectInfo(String collectId, String loginUser) {
		try {
			if (StringUtil.isEmpty(collectId)) {
				return ExecuteResult.error("专题目录收藏ID入参不能为空值", null);
			}
			if (StringUtil.isEmpty(loginUser)) {
				return ExecuteResult.error("当前登录人入参不能为空值", null);
			}
			logger.info("删除专题目录收藏信息，记录id【{}】", collectId);
			Map<String, Object> paramsOne = new HashMap<String, Object>();
			paramsOne.put("collectId", collectId);
			ZyCatalogCollectModel targetModel = catalogCollectDao.queryObject(ZyCatalogCollectModel.class,"ID=:collectId", paramsOne);
			if (targetModel == null) {
				logger.info("根据ID【{}】未能查到指定专题目录收藏信息", collectId);
				return ExecuteResult.error("已无对应专题目录收藏信息!", null);
			}
			if(!(loginUser.equalsIgnoreCase(targetModel.getCjr()))) {
		    	return ExecuteResult.error("非专题目录收藏者，不能进行删除！", null);
		    }
			int count1 = catalogCollectDao.deleteCatalogCollectInfoById(collectId);
			if (count1> 0) {
				return ExecuteResult.success("删除成功");
			}else {
				return ExecuteResult.error("删除失败!", null);
			}
		} catch (Exception e) {
			logger.error("删除专题目录收藏【{}】出现异常，原因：【{}】,详情：", collectId,e.getMessage(), e);
			return ExecuteResult.error("删除异常，原因 【"+e.getMessage()+"】", null);
		}
	}
	
    //获取目标更新的专题目录收藏信息
	private ZyCatalogCollectModel getCatalogCollectModelByUpdateInfo(ZyCatalogCollectModel origin,CatalogCollectUpdateDto update) {
		if(StringUtil.isNotEmpty(update.getSortIndex())) {
			origin.setSortIndex(update.getSortIndex());
		}
		return origin;
	}
    //校验新增的服务信息
	 private String checkCatalogCollectInfoForAdd(CatalogCollectAddDto model) throws Exception {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("CatalogCollectAddDto不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getCatalogId())) {
	        checkInfo.append("专题目录ID【catalogId】不能为空;");
	     }
		 if(StringUtil.isEmpty(model.getCatalogName())) {
	        checkInfo.append("专题目录名称【catalogName】不能为空;");
	     }
         if(StringUtil.isEmpty(model.getThemeId())) {
            checkInfo.append("专题ID【themeId】不能为空;");
         }
         if(StringUtil.isEmpty(model.getSortIndex())||model.getSortIndex()<1) {
 			//如果为空，复制为默认的值（库中最大排序加1）
 			 model.setSortIndex(catalogCollectDao.getObjectSortIndexForIntegerType("T_ZY_CATALOG_COLLECT", "SORTINDEX"));
 		 }
		 if(StringUtil.isEmpty(model.getCjr())) {
			 checkInfo.append("专题收藏添加人【cjr】不能为空;");
		 }
		 return checkInfo.toString();
	 }


	

}
