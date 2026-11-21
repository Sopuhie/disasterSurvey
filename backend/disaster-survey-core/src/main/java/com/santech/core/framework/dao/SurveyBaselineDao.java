package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.BizSurveyBaselineModel;
import com.santech.framework.utils.tool.StringUtil;

@Repository
public class SurveyBaselineDao extends BaseDao {

	public int insert(BizSurveyBaselineModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
	}

	public int update(BizSurveyBaselineModel model) throws Exception {
		return basicDatabase.ORM.update(model);
	}

	public BizSurveyBaselineModel getBySceneId(String sceneId) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("sceneId", sceneId);
		return queryObject(BizSurveyBaselineModel.class, "SCENE_ID=:sceneId", params);
	}

	public BizSurveyBaselineModel getById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return queryObject(BizSurveyBaselineModel.class, "ID=:id", params);
	}

	public BizSurveyBaselineModel getLatestByEventId(String eventId) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		List<BizSurveyBaselineModel> list = queryObjects(BizSurveyBaselineModel.class,
				"EVENT_ID=:eventId order by VERSION_NO desc, ZHGXSJ desc, CJSJ desc", params);
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	public BizSurveyBaselineModel getConfirmedByEventId(String eventId) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		List<BizSurveyBaselineModel> list = queryObjects(BizSurveyBaselineModel.class,
				"EVENT_ID=:eventId and STATUS='CONFIRMED' order by VERSION_NO desc, CONFIRMED_TIME desc",
				params);
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	public BizSurveyBaselineModel getLatest() throws Exception {
		List<BizSurveyBaselineModel> list = queryObjects(BizSurveyBaselineModel.class,
				"1=1 order by ZHGXSJ desc, CJSJ desc", new HashMap<>());
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	public List<BizSurveyBaselineModel> listByEventId(String eventId) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		return queryObjects(BizSurveyBaselineModel.class,
				"EVENT_ID=:eventId order by VERSION_NO desc, ZHGXSJ desc", params);
	}

	public List<BizSurveyBaselineModel> list(String keyword) throws Exception {
		StringBuilder where = new StringBuilder("1=1");
		Map<String, Object> params = new HashMap<>();
		if (StringUtil.isNotEmpty(keyword)) {
			where.append(" and (SCENE_ID like :kw or DISTRICT like :kw or REMARK like :kw or EVENT_ID like :kw)");
			params.put("kw", "%" + keyword + "%");
		}
		where.append(" order by ZHGXSJ desc, CJSJ desc");
		return queryObjects(BizSurveyBaselineModel.class, where.toString(), params);
	}
}
