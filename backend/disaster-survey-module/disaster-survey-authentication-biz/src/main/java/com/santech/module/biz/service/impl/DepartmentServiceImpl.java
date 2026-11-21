package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.santech.core.framework.dao.DeptDao;
import com.santech.core.framework.entity.model.QxDepartmentModel;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;
import com.santech.module.biz.entity.dto.DepartmentAddDto;
import com.santech.module.biz.entity.dto.DepartmentUpdateDto;
import com.santech.module.biz.entity.dto.DeptQueryDto;
import com.santech.module.biz.service.DepartmentService;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * @Title: DepartmentServiceImpl.java 
 * @Description：部门业务层接口类 ，实现DepartmentService.java接口类
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
	
	private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
	
	@Resource
	private DeptDao deptDao; // 组织机构表持久层
	
	/**
	 * @Title queryDepartmentInfos
	 * @Description (此为部门信息查询接口的具体业务逻辑处理方法，该方法可根据检索条件对符合条件的部门信息进行查询，
	 * 如果不传检索条件，默认查询全部，如果传分页信息，可对部门信息进行分页查询)
	 * @param model(DeptQueryModel 检索条件信息)
	 * @return (符合检索条件的部门信息)
	 */
	@Override
	public ExecuteResult<List<QxDepartmentModel>> queryDepartmentInfos(DeptQueryDto query) {
	       try {
	    	   logger.info("查询部门列表信息（分页），入参：【{}】", query);
		       PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
			   //请求参数
	           Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
	           //拼接查询sql
	           Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SORTINDEX asc","");
			   
	           //根据拼接sql,调用dao层获取数据
	           ExecuteResult<List<QxDepartmentModel>> result = deptDao.queryObjectsWithPageInfo(QxDepartmentModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
	           return result;
	       } catch (Exception e) {
	           logger.error("查询部门列表列表信息（分页）出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
	           return ExecuteResult.error("查询失败",null);
	       }
	}
	
	@Override
	public ExecuteResult<List<QxDepartmentModel>> queryDepartmentTreeInfos() {
	       try {
	    	   Map<String,Object> params = new HashMap<String,Object>();
	    	   params.put("status", "running");
	           List<QxDepartmentModel> depts = deptDao.queryObjects(QxDepartmentModel.class, "STATUS=:status  order by SORTINDEX asc",params);
	           List<QxDepartmentModel>  deptTree = getDeptTree(depts,new ArrayList<QxDepartmentModel>(),"-1");
	           return ExecuteResult.success("查询成功", deptTree);
	       } catch (Exception e) {
	           logger.error("查询部门树信息出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
	           return ExecuteResult.error("查询失败",null);
	       }
	}
	
	//根据指定的组织的父级id获取其子孙树结构信息
    private List<QxDepartmentModel> getDeptTree(List<QxDepartmentModel> depts,List<QxDepartmentModel> trees,String parentId) {
		if(trees==null) {
			trees = new ArrayList<QxDepartmentModel>();
		}
		if(parentId!=null&&depts!=null&&depts.size()>0) {
     	   for(QxDepartmentModel dept:depts) {
     		   if(parentId.equals(dept.getPid())) {
     			  dept.setChildren(getDeptTree(depts,new ArrayList<QxDepartmentModel>(),dept.getId()));
        		  trees.add(dept);
     		   }
     	   }
        }
		return trees;
		
	}

	@Override
	public ExecuteResult<String> addDepartment(DepartmentAddDto dto) {
		try {
			String checkErr = checkDepartmentInfoForAdd(dto) ;
			if(StringUtil.isNotEmpty(checkErr)) {
    			return ExecuteResult.error("入参校验不通过，详情："+checkErr, null);
    		}
			
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("orgName",dto.getOrgName());
		    paramsMap.put("pid",dto.getPid());
		    paramsMap.put("type",dto.getOrgType());
		    paramsMap.put("status","running");
		    List<QxDepartmentModel> deptModels = deptDao.queryObjects(QxDepartmentModel.class,"ORGNAME=:orgName and PID=:pid and ORGTYPE=:type and STATUS=:status",paramsMap);
		    if (StringUtil.isNotEmpty(deptModels)&&deptModels.size()>0) {
		    	return ExecuteResult.error("同级部门名称不能重复！", null);
		    }
		    QxDepartmentModel model = new QxDepartmentModel();
		    BeanUtils.copyProperties(dto, model);
			model.setId(UuidUtils.buildID("DP"));
			model.setStatus(StringUtil.isNotEmpty(model.getStatus())?model.getStatus():"running");//机构状态：running 正常；cancel 注销,默认正常
			model.setCjsj(new Date());
			model.setZhgxr(model.getCjr());
			model.setZhgxsj(model.getCjsj());
			deptDao.insertDeptInfo(model);
			return ExecuteResult.success("部门添加成功！");
		} catch (Exception e) {
			logger.error("新增部门异常，详情：",e);
			return ExecuteResult.error("新增部门异常，请稍后再试！", null);
		}
	}

	@Override
	public ExecuteResult<String> deleteDepartment(String departmentId,String loginUser) {
		try {
			if(StringUtil.isEmpty(departmentId)) {
    			return ExecuteResult.error("部门ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("id",departmentId);
		    QxDepartmentModel originDept = deptDao.queryObject(QxDepartmentModel.class,"ID=:id",paramsMap);
		    if (StringUtil.isEmpty(originDept)) {
		    	return ExecuteResult.error("未找到对应机构或部门，无法执行删除！", null);
		    }
		    //部门是否有子部门
	        List<QxDepartmentModel> existDepartment = deptDao.getSubDepartments(departmentId);
	        if (StringUtil.isNotEmpty(existDepartment)) {
	            throw new Exception("请先删除本部门所有子部门");
	        }
	        //部门下是否有用户
	        List<Map<String, Object>> existUser = deptDao.getUserByDepartmentId(departmentId);
	        if (existUser.size() != 0) {
	            throw new Exception("请先删除本部门所有用户");
	        }
		    /*QxDepartmentModel updateDept = new QxDepartmentModel();
		    updateDept.setId(departmentId);
		    updateDept.setStatus("cancel");//机构状态：running 正常；cancel 注销
		    updateDept.setZhgxr(loginUser);
		    updateDept.setZhgxsj(new Date());
		    deptDao.updateDeptInfo(updateDept);*/
	        deptDao.deleteDeptInfo(originDept);
			return ExecuteResult.success("部门删除成功！");
		} catch (Exception e) {
			logger.error("删除部门异常，详情：",e);
			return ExecuteResult.error("删除部门异常，请稍后再试！", null);
		}
	}

	@Override
	public ExecuteResult<String> updateDepartment(DepartmentUpdateDto dto) {
		try {
			String deptId = dto.getId();
			if(StringUtil.isEmpty(deptId)) {
    			return ExecuteResult.error("部门ID不能为空！", null);
    		}
			Map<String,Object> paramsMap = new HashMap<String,Object>();
		    paramsMap.put("deptId",deptId);
		    QxDepartmentModel originDept = deptDao.queryObject(QxDepartmentModel.class,"ID=:deptId",paramsMap);
		    if (StringUtil.isEmpty(originDept)) {
		    	return ExecuteResult.error("未找到对应部门，无法完成更新！", null);
		    }
		    //检查同级部门或机构下是否包含重名
		    if(StringUtil.isNotEmpty(dto.getOrgName())&&!(dto.getOrgName().equals(originDept.getOrgName()))) {
			    paramsMap.put("orgName",dto.getOrgName());
			    if(StringUtil.isNotEmpty(dto.getPid())&&!(dto.getPid().equals(originDept.getPid()))) {
			    	paramsMap.put("pid",dto.getPid());
			    }else {
			    	paramsMap.put("pid",originDept.getPid());
			    }
			    paramsMap.put("type",dto.getOrgType());
			    paramsMap.put("status","running");
			    List<QxDepartmentModel> deptModels = deptDao.queryObjects(QxDepartmentModel.class,"ID!=:deptId and ORGNAME=:orgName and PID=:pid and ORGTYPE=:type and STATUS=:status",paramsMap);
			    if (StringUtil.isNotEmpty(deptModels)&&deptModels.size()>0) {
			    	return ExecuteResult.error("同级部门名称不能重复！", null);
			    }
		    }
		    
		    QxDepartmentModel dept = getDeptModelByUpdateInfo(originDept,dto);
		    deptDao.updateDeptInfo(dept);
			return ExecuteResult.success("部门更新成功！");
		} catch (Exception e) {
			logger.error("更新部门异常，详情：",e);
			return ExecuteResult.success("更新部门异常，原因：【"+e.getMessage()+"】");
		}
	}

	//校验新增的部门信息
	 private String checkDepartmentInfoForAdd(DepartmentAddDto model) throws Exception {
		 StringBuilder checkInfo = new StringBuilder("");
		 if(StringUtil.isEmpty(model)) {
			 checkInfo.append("DepartmentAddDto不能为空;");
			 return checkInfo.toString();
		 }
		 if(StringUtil.isEmpty(model.getPid())) {
			 //如果pid为空，默认为添加根节点机构
			 model.setPid("-1");
		 }
		 if(StringUtil.isEmpty(model.getOrgCode())) {
			 checkInfo.append("部门编号【orgCode】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getOrgName())) {
			 checkInfo.append("部门名称【orgName】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getOrgType())) {
			 checkInfo.append("部门类型【orgType】不能为空;");
		 }
		 if(StringUtil.isEmpty(model.getSortIndex())||model.getSortIndex()<1) {
			 //checkInfo.append("排序索引【sortIndex】不能为空;");
			 //如果为空，复制为默认的值（库中最大排序加1）
			 model.setSortIndex(deptDao.getObjectSortIndexForIntegerType("T_QX_DEPARTMENT", "SORTINDEX"));
		 }
		 if(StringUtil.isEmpty(model.getCjr())) {
			 checkInfo.append("部门创建人【cjr】不能为空;");
		 }
		 return checkInfo.toString();
		 
	 }
	 
	 private QxDepartmentModel getDeptModelByUpdateInfo(QxDepartmentModel originDept,DepartmentUpdateDto updateDept) {
			
		 if(StringUtil.isNotEmpty(updateDept.getPid())) {
			 originDept.setPid(updateDept.getPid());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getOrgName())) {
			 originDept.setOrgName(updateDept.getOrgName());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getOrgCode())) {
			 originDept.setOrgCode(updateDept.getOrgCode());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getOrgType())) {
			 originDept.setOrgType(updateDept.getOrgType());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getOrgDesc())) {
			 originDept.setOrgDesc(updateDept.getOrgDesc());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getRemark())) {
			 originDept.setRemark(updateDept.getRemark());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getSortIndex())) {
			 originDept.setSortIndex(updateDept.getSortIndex());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getStatus())) {
			 originDept.setStatus(updateDept.getStatus());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getRemark())) {
			 originDept.setRemark(updateDept.getRemark());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getXzqdm())) {
			 originDept.setXzqdm(updateDept.getXzqdm());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getXzqmc())) {
			 originDept.setXzqmc(updateDept.getXzqmc());
		 }
		 if(StringUtil.isNotEmpty(updateDept.getZhgxr())) {
			 originDept.setZhgxr(updateDept.getZhgxr());
		 }
		 originDept.setZhgxsj(new Date());
		 return originDept;
	}

}
