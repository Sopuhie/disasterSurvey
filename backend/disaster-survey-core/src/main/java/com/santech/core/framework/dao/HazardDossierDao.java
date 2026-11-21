package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.BizHazardDossierModel;
import com.santech.framework.utils.tool.StringUtil;

@Repository
public class HazardDossierDao extends BaseDao {

	public int insert(BizHazardDossierModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
	}

	public int update(BizHazardDossierModel model) throws Exception {
		return basicDatabase.ORM.update(model);
	}

	public BizHazardDossierModel getByBizKey(String bizKey) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("bizKey", bizKey);
		return queryObject(BizHazardDossierModel.class, "BIZ_KEY=:bizKey", params);
	}

	public BizHazardDossierModel getById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return queryObject(BizHazardDossierModel.class, "ID=:id", params);
	}

	public List<BizHazardDossierModel> listArchived(String keyword) throws Exception {
		StringBuilder where = new StringBuilder("ARCHIVE_STATUS='已归档'");
		Map<String, Object> params = new HashMap<>();
		if (StringUtil.isNotEmpty(keyword)) {
			where.append(" and (HAZARD_NAME like :kw or ARCHIVE_NO like :kw or VERIFY_STATUS like :kw)");
			params.put("kw", "%" + keyword + "%");
		}
		where.append(" order by ZHGXSJ desc, CJSJ desc");
		return queryObjects(BizHazardDossierModel.class, where.toString(), params);
	}

	public int deleteByBizKey(String bizKey) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("bizKey", bizKey);
		return basicDatabase.ORM.delete(BizHazardDossierModel.class, "BIZ_KEY=:bizKey", params);
	}
}
