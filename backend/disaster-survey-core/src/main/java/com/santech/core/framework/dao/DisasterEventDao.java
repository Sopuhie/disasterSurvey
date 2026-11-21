package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.BizDisasterEventModel;
import com.santech.framework.utils.tool.StringUtil;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Repository
public class DisasterEventDao extends BaseDao {

	public int insert(BizDisasterEventModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
	}

	public int update(BizDisasterEventModel model) throws Exception {
		return basicDatabase.ORM.update(model);
	}

	public BizDisasterEventModel getById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return queryObject(BizDisasterEventModel.class, "ID=:id", params);
	}

	public BizDisasterEventModel getByEventCode(String eventCode) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventCode", eventCode);
		return queryObject(BizDisasterEventModel.class, "EVENT_CODE=:eventCode", params);
	}

	public List<BizDisasterEventModel> list(String keyword, String status) throws Exception {
		StringBuilder where = new StringBuilder("1=1");
		Map<String, Object> params = new HashMap<>();
		if (StringUtil.isNotEmpty(status)) {
			where.append(" and STATUS=:status");
			params.put("status", status);
		}
		if (StringUtil.isNotEmpty(keyword)) {
			where.append(" and (EVENT_CODE like :kw or EVENT_NAME like :kw or REGION_NAME like :kw)");
			params.put("kw", "%" + keyword + "%");
		}
		where.append(" order by ZHGXSJ desc, CJSJ desc");
		return queryObjects(BizDisasterEventModel.class, where.toString(), params);
	}

	public List<BizDisasterEventModel> listWithPage(String keyword, String status, PageInfo pageInfo) throws Exception {
		StringBuilder where = new StringBuilder("1=1");
		Map<String, Object> params = new HashMap<>();
		if (StringUtil.isNotEmpty(status)) {
			where.append(" and STATUS=:status");
			params.put("status", status);
		}
		if (StringUtil.isNotEmpty(keyword)) {
			where.append(" and (EVENT_CODE like :kw or EVENT_NAME like :kw or REGION_NAME like :kw)");
			params.put("kw", "%" + keyword + "%");
		}
		where.append(" order by ZHGXSJ desc, CJSJ desc");
		return queryObjectsWithPageInfo(BizDisasterEventModel.class, where.toString(), params, pageInfo).getData();
	}
}
