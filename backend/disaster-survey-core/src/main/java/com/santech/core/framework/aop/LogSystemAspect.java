package com.santech.core.framework.aop;

import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.JcLogModel;
import com.santech.framework.global.Constants;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.framework.utils.tool.RequestUtil;
import com.santech.framework.utils.tool.URLcoderUtil;

/**
  * @Description: 系统日志切面
  **/
@Aspect
@Component
public class LogSystemAspect {
	
	private static final Logger logger = LoggerFactory.getLogger(LogSystemAspect.class);

	@Autowired
	private AsyncTask asyncTask;

	@Around("@annotation(logSystem)")
	public Object around(ProceedingJoinPoint point, LogSystem logSystem) throws Throwable {
		
		JcLogModel logModel = new JcLogModel();
		String userId = RequestUtil.getHeaderByName(Constants.HEADER_USERID)==null?(String) RequestUtil.getHttpRequest().getAttribute(Constants.HEADER_USERID):RequestUtil.getHeaderByName(Constants.HEADER_USERID);
		logModel.setUserId(userId);
		String userName = URLcoderUtil.decode(RequestUtil.getHeaderByName(Constants.HEADER_USERNAME)==null?(String) RequestUtil.getHttpRequest().getAttribute(Constants.HEADER_USERNAME):RequestUtil.getHeaderByName(Constants.HEADER_USERNAME));
		logModel.setUserName(userName);
		logModel.setTarget(logSystem.target());
		logModel.setType(logSystem.type());
		logModel.setIpAddress(CommonUtils.getIpAddr(RequestUtil.getHttpRequest()));
		Object obj;
		try {
			obj = point.proceed();
			if(obj!=null) {
				JSONObject result = JSONObject.parseObject(JSON.toJSONString(obj));
				if("S".equalsIgnoreCase(result.getString("resultCode"))) {
					logModel.setStatus(Constants.LOG_SUCCESS_STATUS);
				}else {
					logModel.setStatus(Constants.LOG_ABNORMAL_STATUS);
					String resultMsg = result.getString("resultMsg")==null?"":result.getString("resultMsg");
					logModel.setStatusMsg(resultMsg.length()>300?resultMsg.substring(0, 255):resultMsg);
				}
			}else {
				logModel.setStatus(Constants.LOG_SUCCESS_STATUS);
			}
		}catch (Exception e) {
			logModel.setStatus(Constants.LOG_ABNORMAL_STATUS);
			logModel.setStatusMsg(e.getMessage());
			throw e;
		}finally {
			try {
				logModel.setCreateTime(new Date());
				asyncTask.saveSystemLog(logModel);
			} catch (Exception e2) {
				logger.error("字典管理微服务-利用切面保存日志出现异常，详情：",e2);
			}
		}
		return obj;
	}



}
