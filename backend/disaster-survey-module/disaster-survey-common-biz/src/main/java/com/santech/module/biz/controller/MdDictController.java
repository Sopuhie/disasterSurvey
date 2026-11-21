package com.santech.module.biz.controller;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.JcDictionaryDetailModel;
import com.santech.core.framework.entity.model.JcDictionaryTypeModel;
import com.santech.core.framework.entity.view.DictionaryTypeQueryDto;
import com.santech.core.framework.service.MdDictionaryManagerService;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Common-数据字典运维管理相关功能接口")
@RestController
@RequestMapping("/web-disaster-common/web/disaster/common/dict")
public class MdDictController {
	private static final  Logger logger = LoggerFactory.getLogger(MdDictController.class);
    @Resource
    private MdDictionaryManagerService mdDictionaryManagerService;

    /**
   	 * @Title exceptionHandler
   	 * @Description (用来捕获MdDictController接口程序中未能预见的异常信息，保证前端相应结果的一致性，避免页面崩溃)
   	 * @param e (Exception,所有异常的父类异常)
   	 * @return (接口执行情况)
   	 */
   @ExceptionHandler(Exception.class)
   public ExecuteResult<String> exceptionHandler(Exception e){
   		logger.error("MdDictController中捕获的其他异常，异常详情：",e);
       return ExecuteResult.error(e.getMessage(),null);
   }
       
    /**
	  * @Title queryDictionaryTypeInfos 
	  * @Description (根据筛选条件批量查询字典类型记录信息)
	  * @param query (DictionaryTypeQueryDto,查询条件参数)
	  * @return (接口的执行情况及符合条件的记录信息)
	  */
   @PostMapping("/type/query")
   @ApiOperation(value = "字典类型记录批量查询接口（可分页）", notes = "{字段说明【\n" +
           "】")
   public ExecuteResult<List<JcDictionaryTypeModel>> queryDictionaryTypeInfos(@RequestBody DictionaryTypeQueryDto query){
       return mdDictionaryManagerService.queryDictionaryTypeInfos(query);
   }
    
   /**
	  * @Title saveDictionaryTypeInfo 
	  * @Description (字典类型保存或新增接口，字典类型编码不能为空)
	  * @param model (JcDictionaryTypeModel 新增字典类型所要保存的信息)
	  * @return (接口的执行情况)
	  */
   @LogSystem(target = "字典类型新增", type = Constants.LOG_TYPE_ADD)
   @PostMapping("/type/save")
   @ApiOperation(value = "字典类型保存或新增接口", notes = "{字段说明【\n" +
           "】")
   public ExecuteResult<String> saveDictionaryInfo(@RequestBody JcDictionaryTypeModel model) {
       return mdDictionaryManagerService.saveDictionaryTypeInfo(model);
   }
   
   /**
	  * @Title updateDictionaryTypeInfo 
	  * @Description (根据字典ID，对目标字典类型记录信息进行更新，id必填，以及zdlxbm不可修改)
	  * @param model (JcDictionaryTypeModel 要更新的字典类型信息)
	  * @return (接口的执行情况)
	  */
   @LogSystem(target = "字典类型更新", type = Constants.LOG_TYPE_UPDATE)
   @PostMapping("/type/update")
   @ApiOperation(value = "字典类型更新接口", notes = "{字段说明【\n" +
           "】")
   public ExecuteResult<String> updateDictionaryInfo(@RequestBody JcDictionaryTypeModel model){
   	return mdDictionaryManagerService.updateDictionaryTypeInfo(model);
   }
   
   /**
	  * @Title deleteDictionaryTypeInfo 
	  * @Description (根据id，对目标字典类型记录信息进行删除，删除是以开关控制非真实删除)
	  * @param dicTypeId (字典类型记录ID)
	  * @return (接口的执行情况)
	  */
   @LogSystem(target = "字典类型删除", type = Constants.LOG_TYPE_DELETE)
   @GetMapping("/type/delete")
   @ApiOperation(value = "字典类型删除接口", notes = "{字段说明【\n" +
  		"dicId(字典项ID，必填)\n"+
          "】")
   public ExecuteResult<String> deleteDictionaryInfo(@RequestParam(value="dicId",required = true) String dicId){
  	 return mdDictionaryManagerService.deleteDictionaryTypeInfo(dicId);
   }
  /**
	  * @Title queryDictionaryDetailInfo 
	  * @Description (根据条件查询对应的字典项信息，二者条件至少一个有值)
	  * @param flid (字典分类id,条件必选，zdlxbm为空时必填)
	  * @param zdlxbm (字典分类代码,条件必选，flid为空时必填)
	  * @return (接口的执行情况及对字典值信息)
	  */
   @GetMapping("/detail/query")
   @ApiOperation(value = "字典项查询接口", notes = "{字段说明【\n" +
           "flid (字典分类id,条件必选，zdlxbm为空时必填)\n"+
           "zdlxbm (字典分类代码,条件必选，flid为空时必填)\n"+
           "】")
   public ExecuteResult<List<JcDictionaryDetailModel>> queryDictionaryDetailInfo( @RequestParam(value="flid",required = false) String flid,@RequestParam(value="zdlxdm",required = false) String zdlxdm){
       
   	return mdDictionaryManagerService.queryDictionaryDetailInfo(flid,zdlxdm);
   }

   /**
	  * @Title saveDictionaryDetailInfo 
	  * @Description (字典项保存或新增接口)
	  * @param model (JcDictionaryDetailModel 新增字典类型所要保存的信息)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "字典项新增", type = Constants.LOG_TYPE_ADD)
	 @PostMapping("/detail/save")
	 @ApiOperation(value = "字典项保存或新增接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<String> saveDictionaryDetailInfo(@RequestBody JcDictionaryDetailModel model) {
	     return mdDictionaryManagerService.saveDictionaryDetailInfo(model);
	 }
	 
	 /**
	  * @Title updateDictionaryDetailInfo 
	  * @Description (根据字典ID，对目标字典项记录信息进行更新，id必填)
	  * @param model (JcDictionaryDetailModel 要更新的字典项信息)
	  * @return (接口的执行情况)
	  */
     @LogSystem(target = "字典项更新", type = Constants.LOG_TYPE_UPDATE)
	 @PostMapping("/detail/update")
	 @ApiOperation(value = "字典项更新接口", notes = "{字段说明【\n" +
	         "】")
	 public ExecuteResult<String> updateDictionaryDetailInfo(@RequestBody JcDictionaryDetailModel model){
	 	return mdDictionaryManagerService.updateDictionaryDetailInfo(model);
	 }
	 
	 /**
	  * @Title deleteDictionaryDetailInfo 
	  * @Description (根据id，对目标字典项记录信息进行删除，删除是真实删除)
	  * @param dicId (字典记录ID)
	  * @return (接口的执行情况)
	  */
    @LogSystem(target = "字典项删除", type = Constants.LOG_TYPE_DELETE)
	@GetMapping("/detail/delete")
	@ApiOperation(value = "字典项删除接口", notes = "{字段说明【\n" +
			"dicId(字典项ID，必填)\n"+
	        "】")
	public ExecuteResult<String> deleteDictionaryDetailInfo(@RequestParam(value="dicId",required = true) String dicId){
		return mdDictionaryManagerService.deleteDictionaryDetailInfo(dicId);
	}
    
    
    
    
}
