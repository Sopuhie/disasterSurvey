package com.santech.core.framework.service.impl;

import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.santech.core.framework.config.FileBasicConfig;
import com.santech.core.framework.config.KindCryptoConfig;
import com.santech.core.framework.dao.LogDao;
import com.santech.core.framework.entity.model.JcLogModel;
import com.santech.core.framework.entity.view.LogQueryDto;
import com.santech.core.framework.service.LogService;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.component.encrypt.kindcrypto.KindCryptoUtils;
import com.santech.framework.utils.excel.easyexcel.EasyExcelUtils;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.SqlUtil;
import com.santech.framework.utils.tool.StringUtil;
import com.santech.framework.utils.tool.UuidUtils;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * @Title: LogServiceImpl.java 
 * @Description：系统日志业务层接口类 ，实现LogService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class LogServiceImpl implements LogService {
    private static final  Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    
    @Resource
 	private KindCryptoConfig kindCryptoConfig;
    @Resource
	private FileBasicConfig fileBasicConfig;
    @Resource
    private LogDao logDao;
    

	@Override
	public ExecuteResult<List<JcLogModel>> queryLogInfos(LogQueryDto query){
		logger.info("查询日志列表信息，入参：【{}】", query);
        PageInfo pageInfo = new PageInfo(SqlUtil.getCurrentPage(query.getCurrent()),SqlUtil.getPageSize(query.getSize()));
        try {
        	String otherSql = "";
        	if(StringUtil.isNotEmpty(query.getStartTime())) {
        		if(StringUtil.isEmpty(query.getEndTime())) {
        			query.setEndTime(CommonUtils.getFormatDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
        		}
        		String startTime = query.getStartTime();
        		String endTime = query.getEndTime();
        		String timeSQL = String.format("and CREATETIME >= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') and  CREATETIME <= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') ",startTime,endTime);
        		otherSql = otherSql+timeSQL;
        	}
            //请求参数
            Map<String,Object> params = (Map<String, Object>) CommonUtils.beanToMap(query);
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"CREATETIME desc",otherSql);
            //根据拼接sql,调用dao层获取数据
            return logDao.queryObjectsWithPageInfo(JcLogModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"), pageInfo);
        } catch (Exception e) {
            logger.error("查询日志列表信息，失败原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("查询失败",null);
        }
	}

	@Override
	public ExecuteResult<String> saveLogeInfo(JcLogModel model) {
		try {
			model.setId(UuidUtils.buildID("LOG"));
			if(StringUtil.isEmpty(model.getCreateTime())) {
				model.setCreateTime(new Date());
			}
			//对数据完整性进行签名生成，以防日志篡改
			StringBuilder dataStr = new StringBuilder("");
			dataStr.append( model.getUserName()).append(";");
			dataStr.append( model.getTarget()).append(";");
			dataStr.append( model.getType()).append(";");
			dataStr.append( model.getIpAddress()).append(";");
			dataStr.append( CommonUtils.getFormatDateString(model.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).append(";");
			Map<String,String> result = KindCryptoUtils.signData(kindCryptoConfig.getAppname(),dataStr.toString(),kindCryptoConfig.isEnable());
			model.setSign(result.get("sign"));
			model.setPubKeyCert(result.get("pubKeyCert"));
			logDao.insertLog(model);
			return ExecuteResult.success("保存成功");
		} catch (Exception e) {
			logger.error("新增日志信息出现异常，入参【{}】,详情：",model,e);
			return ExecuteResult.error("新增日志信息失败", null);
			
		}
	}

	@Override
	public ExecuteResult<String> bakLogeInfo() {
		try {
			LocalDate localDate = LocalDate.now();
	        int targetYear = localDate.getYear()-3;//默认备份三年前的，即默认数据库中的日志信息至少保留3年
	        String endTime = targetYear+"-01-01 00:00:59";
    		String otherSql =String.format("and  CREATETIME <= to_timestamp('%s','yyyy-mm-dd hh24:mi:ss') ",endTime);
            //请求参数
            Map<String,Object> params = new HashMap<String,Object>();
            //拼接查询sql
            Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"CREATETIME ASC",otherSql);
            //根据拼接sql,调用dao层获取数据
            List<JcLogModel> logs = logDao.queryObjects(JcLogModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
            
            if(logs!=null&&logs.size()>0) {
            	//进行数据备份
            	//对数据写入excel
        	    String rootPath = getFileCacheRootPath(fileBasicConfig);
        	    String systemlogbakPath = rootPath+(rootPath.endsWith(File.separator)?"systemlogbak":File.separator+"systemlogbak");
        	    File dir = new File(systemlogbakPath);
        	    if(!dir.exists()) {
        	    	dir.mkdirs();
        	    }
        	    String fileName = targetYear+"-01-01系统日志_备份_"+System.currentTimeMillis()+".xlsx";
        	    String targetFilePath = systemlogbakPath+File.separator+fileName;
        	    boolean flag = EasyExcelUtils.writeExcelSingleSheetByAllFields(targetFilePath, "系统日志记录", JcLogModel.class,  logs);
            	//备份成功，将数据库中的数据进行删除
        	    if(flag) {
        	    	int count = logDao.deleteObject(JcLogModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
        	    	logger.info("【{}】日期前的日志数据备份完毕，共【{}】条，对备份的数据从数据库中删除，共计删除【{}】条",targetYear+"-01-01",logs.size(),count);
        	    }else {
        	    	return ExecuteResult.error("备份失败",null);
        	    }
            }
            return ExecuteResult.success("备份成功");
        } catch (Exception e) {
            logger.error("备份系统日志信息出现异常，异常原因【{}】,异常详情：", e.getMessage(),e);
            return ExecuteResult.error("备份异常",null);
        }
	}
	
   /**
    * 获取数据本地存储根目录
    * @return
    */
   private String getFileCacheRootPath(FileBasicConfig commonConfig) {
   	String names= System.getProperty("os.name");
   	String baseFilePath;
   	 if(names.toLowerCase().startsWith("win")){
        	baseFilePath = commonConfig.getRootForWin();
        }else{
        	baseFilePath = commonConfig.getRootForLiunx();
        }
   	 return baseFilePath;
   }

   

}
