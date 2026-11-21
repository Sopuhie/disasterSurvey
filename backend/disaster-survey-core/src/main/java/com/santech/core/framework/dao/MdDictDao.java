package com.santech.core.framework.dao;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.JcDictionaryDetailModel;
import com.santech.core.framework.entity.model.JcDictionaryTypeModel;

@Repository
public class MdDictDao extends BaseDao {
	/**
     * @param data
     * @Description: insertMdDictionaryType新增字典类型
     * @Author: liu
     * @return: int
     **/
    public int insertMdDictionaryType(JcDictionaryTypeModel data) throws Exception {
        return basicDatabase.ORM.insert(data);
    }

    /**
     * @param data
     * @Description: updateMdDictionaryTypeByModel更新字典类型
     * @Author: liu
     * @return: int
     **/
    public int updateMdDictionaryTypeByModel(JcDictionaryTypeModel data) throws Exception {
        return basicDatabase.ORM.update(data);
    }

    /**
     * @param data
     * @Description: insertMdDictionaryDetail新增字典项
     * @Author: liu
     * @return: int
     **/
    public int insertMdDictionaryDetail(JcDictionaryDetailModel data) throws Exception {
        return basicDatabase.ORM.insert(data);
    }

    /**
     * @param data
     * @Description: updateMdDictionaryDetailByModel更新字典项
     * @Author: liu
     * @return: int
     **/
    public int updateMdDictionaryDetailByModel(JcDictionaryDetailModel data) throws Exception {
        return basicDatabase.ORM.update(data);
    }



}
