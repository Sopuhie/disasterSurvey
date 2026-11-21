package com.santech.core.framework.service;

import java.util.List;

import com.santech.core.framework.entity.model.JcLogModel;
import com.santech.core.framework.entity.view.LogQueryDto;
import com.santech.framework.global.ExecuteResult;

/**
 * @Title: LogService.java 
 * @Description：系统日志业务层接口类
 * @Version： V1.0
 * @Modify：   
 */
public interface LogService {
	
	 /**
	  * @Title queryLogInfos 
	  * @Description (根据筛选条件批量查询日志信息，可分页)
	  * @param query (LogQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
  ExecuteResult<List<JcLogModel>> queryLogInfos(LogQueryDto query);
   
   /**
	  * @Title saveLogeInfo 
	  * @Description (保存或新增接口的业务逻辑)
	  * @param model (JcLogModel 所要保存的日志信息)
	  * @return (接口的执行情况)
	  */
  ExecuteResult<String> saveLogeInfo(JcLogModel model);
  
  
  
 /**
  * @Title bakLogeInfo 
  * @Description (定期备份系统日志接口的业务逻辑，备份完毕，会将备份后的数据从数据库中删除，默认数据库中只保存3-4年的日志数据)
  * @return (接口的执行情况)
  */
  ExecuteResult<String> bakLogeInfo();

   
    
}
