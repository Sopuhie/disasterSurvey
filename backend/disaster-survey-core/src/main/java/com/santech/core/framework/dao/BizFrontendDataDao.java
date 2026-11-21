package com.santech.core.framework.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.BizFrontendDataModel;
import com.santech.core.framework.entity.model.BizReportArchiveModel;
import com.santech.framework.utils.tool.StringUtil;

import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

@Repository
public class BizFrontendDataDao extends BaseDao {

	public int insert(BizFrontendDataModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
	}

	public int update(BizFrontendDataModel model) throws Exception {
		return basicDatabase.ORM.update(model);
	}

	public int deleteById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return basicDatabase.ORM.delete(BizFrontendDataModel.class, "ID=:id", params);
	}

	public BizFrontendDataModel getById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return queryObject(BizFrontendDataModel.class, "ID=:id", params);
	}

	public List<BizFrontendDataModel> listByType(String dataType, String keyword, String extraType) throws Exception {
		StringBuilder where = new StringBuilder("1=1");
		Map<String, Object> params = new HashMap<>();
		if (StringUtil.isNotEmpty(dataType)) {
			where.append(" and DATA_TYPE=:dataType");
			params.put("dataType", dataType);
		}
		if (StringUtil.isNotEmpty(extraType)) {
			where.append(" and EXTRA_TYPE=:extraType");
			params.put("extraType", extraType);
		}
		if (StringUtil.isNotEmpty(keyword)) {
			where.append(" and (NAME like :kw or BIZ_KEY like :kw)");
			params.put("kw", "%" + keyword + "%");
		}
		where.append(" order by SORT_INDEX asc, CJSJ desc");
		return queryObjects(BizFrontendDataModel.class, where.toString(), params);
	}

	public List<BizFrontendDataModel> listByTypeWithPage(String dataType, String keyword, String extraType, PageInfo pageInfo)
			throws Exception {
		StringBuilder where = new StringBuilder("1=1");
		Map<String, Object> params = new HashMap<>();
		if (StringUtil.isNotEmpty(dataType)) {
			where.append(" and DATA_TYPE=:dataType");
			params.put("dataType", dataType);
		}
		if (StringUtil.isNotEmpty(extraType)) {
			where.append(" and EXTRA_TYPE=:extraType");
			params.put("extraType", extraType);
		}
		if (StringUtil.isNotEmpty(keyword)) {
			where.append(" and (NAME like :kw or BIZ_KEY like :kw)");
			params.put("kw", "%" + keyword + "%");
		}
		where.append(" order by SORT_INDEX asc, CJSJ desc");
		return queryObjectsWithPageInfo(BizFrontendDataModel.class, where.toString(), params, pageInfo).getData();
	}

	public int insertArchive(BizReportArchiveModel model) throws Exception {
		return basicDatabase.ORM.insert(model);
	}

	public BizReportArchiveModel getArchiveById(String id) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		return queryObject(BizReportArchiveModel.class, "ID=:id", params);
	}

	public List<BizReportArchiveModel> listArchives() throws Exception {
		return queryObjects(BizReportArchiveModel.class, "1=1 order by CJSJ desc", new HashMap<>());
	}

	public List<BizReportArchiveModel> listArchivesByEventId(String eventId) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("eventId", eventId);
		return queryObjects(BizReportArchiveModel.class,
				"EVENT_ID=:eventId order by CJSJ desc", params);
	}

	public int updateArchive(BizReportArchiveModel model) throws Exception {
		return basicDatabase.ORM.update(model);
	}

	public int executeDdl(String sql) throws Exception {
		return basicDatabase.SQL.updateSql(sql);
	}

	public boolean tableExists(String tableName) {
		try {
			basicDatabase.SQL.queryListMap("SELECT 1 FROM " + tableName + " WHERE 1=0");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean columnExists(String tableName, String columnName) {
		try {
			basicDatabase.SQL.queryListMap("SELECT " + columnName + " FROM " + tableName + " WHERE 1=0");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
