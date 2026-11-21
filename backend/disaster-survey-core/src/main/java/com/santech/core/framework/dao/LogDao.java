package com.santech.core.framework.dao;

import org.springframework.stereotype.Repository;

import com.santech.core.framework.entity.model.JcLogModel;

@Repository
public class LogDao extends BaseDao{

    
    public int insertLog(JcLogModel data) throws Exception {
        return basicDatabase.ORM.insert(data);
    }

}
