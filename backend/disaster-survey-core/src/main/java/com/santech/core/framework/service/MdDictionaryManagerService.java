package com.santech.core.framework.service;

import java.util.List;

import com.santech.core.framework.entity.model.JcDictionaryDetailModel;
import com.santech.core.framework.entity.model.JcDictionaryTypeModel;
import com.santech.core.framework.entity.view.DictionaryTypeQueryDto;
import com.santech.framework.global.ExecuteResult;

/**
 * @Title: MdDictionaryManagerService.java 
 * @Description：字典运维业务层接口类
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
public interface MdDictionaryManagerService {
	
	 /**
	  * @Title queryDictionaryTypeInfos 
	  * @Description (根据筛选条件批量查询字典类型记录信息)
	  * @param query (DictionaryTypeQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
  ExecuteResult<List<JcDictionaryTypeModel>> queryDictionaryTypeInfos(DictionaryTypeQueryDto query);
   
   /**
	  * @Title saveDictionaryTypeInfo 
	  * @Description (字典类型保存或新增接口，字典类型编码不能为空)
	  * @param model (JcDictionaryTypeModel 新增字典类型所要保存的信息)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> saveDictionaryTypeInfo(JcDictionaryTypeModel model);
  
  

   /**
	  * @Title updateDictionaryTypeInfo 
	  * @Description (根据字典ID，对目标字典类型记录信息进行更新，id必填，以及zdlxbm不可修改)
	  * @param model (JcDictionaryTypeModel 要更新的字典类型信息)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> updateDictionaryTypeInfo(JcDictionaryTypeModel model);
  
  /**
	  * @Title deleteDictionaryTypeInfo 
	  * @Description (根据id，对目标字典类型记录信息进行删除，删除是以开关控制非真实删除)
	  * @param dicTypeId (字典类型记录ID)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> deleteDictionaryTypeInfo(String dicTypeId);
  
  
  /**
	  * @Title queryDictionaryDetailInfo 
	  * @Description (根据条件查询对应的字典项信息，二者条件至少一个有值)
	  * @param flid (字典分类id,条件必选，zdlxbm为空时必填)
	  * @param zdlxbm (字典分类代码,条件必选，flid为空时必填)
	  * @return (接口的执行情况及对字典值信息)
	  */
  ExecuteResult<List<JcDictionaryDetailModel>> queryDictionaryDetailInfo(String flid, String zdlxbm);
    
  /**
	  * @Title saveDictionaryDetailInfo 
	  * @Description (字典项保存或新增接口)
	  * @param model (JcDictionaryDetailModel 新增字典类型所要保存的信息)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> saveDictionaryDetailInfo(JcDictionaryDetailModel model);



  /**
	  * @Title updateDictionaryDetailInfo 
	  * @Description (根据字典ID，对目标字典项记录信息进行更新，id必填)
	  * @param model (JcDictionaryDetailModel 要更新的字典项信息)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> updateDictionaryDetailInfo(JcDictionaryDetailModel model);

  /**
	  * @Title deleteDictionaryDetailInfo 
	  * @Description (根据id，对目标字典项记录信息进行删除，删除是真实删除)
	  * @param dicId (字典记录ID)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> deleteDictionaryDetailInfo(String dicId);
    
}
