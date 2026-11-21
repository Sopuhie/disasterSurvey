package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.santech.core.framework.config.KindCryptoConfig;
import com.santech.core.framework.dao.BaseDao;
import com.santech.core.framework.entity.model.JcSystemConfigModel;
import com.santech.framework.utils.component.encrypt.kindcrypto.KindCryptoUtils;
import com.santech.framework.utils.tool.StringUtil;

@Repository
public class SystemConfigDao extends BaseDao {

    public int insertSystemConfigInfo(JcSystemConfigModel model,KindCryptoConfig kindCryptoConfig) throws Exception {
    	if(StringUtil.isEmpty(model.getSign())) {
    		//获取完整性加密串，TITLE 系统名称，STYLE 系统风格
			StringBuilder dataStr = new StringBuilder("");
			dataStr.append( model.getTitle()).append(";");
			dataStr.append( model.getStyle()).append(";");
			Map<String,String> result = KindCryptoUtils.signData(kindCryptoConfig.getAppname(),dataStr.toString(),kindCryptoConfig.isEnable());
			model.setSign(result.get("sign"));
			model.setPubKeyCert(result.get("pubKeyCert"));
		}
		return basicDatabase.ORM.insert(model);
    }
    
	public int updateSystemConfigInfo(JcSystemConfigModel model,KindCryptoConfig kindCryptoConfig) throws Exception {
		StringBuilder dataStr = new StringBuilder("");
		dataStr.append( model.getTitle()).append(";");
		dataStr.append( model.getStyle()).append(";");
		Map<String,String> result = KindCryptoUtils.signData(kindCryptoConfig.getAppname(),dataStr.toString(),kindCryptoConfig.isEnable());
		model.setSign(result.get("sign"));
		model.setPubKeyCert(result.get("pubKeyCert"));
		return basicDatabase.ORM.update(model);
    }
	
	public int deleteSystemConfigInfoById(String id) throws Exception {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("id", id);
		return basicDatabase.ORM.delete(JcSystemConfigModel.class, "ID=:id", paramsMap);
    }
	
	public List<Map<String, Object>> selectSystemConfigInfos(String sql,Map<String,Object> params) throws Exception {
        List<Map<String, Object>> tableList = null;
        if(params.size()==0){
            tableList = basicDatabase.SQL.queryListMap(sql);
        }else{
            tableList = basicDatabase.SQL.queryListMap(sql, params);
        }
        return tableList;
    }
    

}
