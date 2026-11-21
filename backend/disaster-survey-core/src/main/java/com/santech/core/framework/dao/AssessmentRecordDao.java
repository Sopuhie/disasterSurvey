package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.BizAssessmentRecordModel;
import com.santech.framework.utils.tool.StringUtil;

@Repository
public class AssessmentRecordDao extends BaseDao {

	public int insert(BizAssessmentRecordModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
	}

	public int update(BizAssessmentRecordModel model) throws Exception {
		return basicDatabase.ORM.update(model);
	}

	public BizAssessmentRecordModel getById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return queryObject(BizAssessmentRecordModel.class, "ID=:id", params);
	}

	public List<BizAssessmentRecordModel> listByEventId(String eventId) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		return queryObjects(BizAssessmentRecordModel.class,
				"EVENT_ID=:eventId order by ZHGXSJ desc, CJSJ desc", params);
	}

	public List<BizAssessmentRecordModel> listByEventIdAndDisasterType(String eventId, String disasterType)
			throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		StringBuilder where = new StringBuilder("EVENT_ID=:eventId");
		if (StringUtil.isNotEmpty(disasterType)) {
			where.append(" and DISASTER_TYPE=:disasterType");
			params.put("disasterType", disasterType);
		}
		where.append(" order by ZHGXSJ desc, CJSJ desc");
		return queryObjects(BizAssessmentRecordModel.class, where.toString(), params);
	}

	public BizAssessmentRecordModel getByEventAndSubType(String eventId, String subType) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		params.put("subType", subType);
		return queryObject(BizAssessmentRecordModel.class,
				"EVENT_ID=:eventId and SUB_TYPE=:subType", params);
	}

	public List<BizAssessmentRecordModel> listAsyncRunning() throws Exception {
		Map<String, Object> params = new HashMap<>();
		return queryObjects(BizAssessmentRecordModel.class,
				"COMPUTE_MODE='ASYNC' and STATUS='RUNNING' order by CJSJ asc", params);
	}
}
