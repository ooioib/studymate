package org.codenova.studymate.repository;

import org.codenova.studymate.model.LoginLog;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public class LoginLogRepository {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public int create(String userId) {
        return sqlSessionTemplate.insert("loginLog.create", userId);
    }

    public List<LoginLog> findByUserId(String userId) {
        return sqlSessionTemplate.selectList("loginLog.findByUserId", userId);
    }

    public LoginLog findLatestByUserId(String userId) {
        return sqlSessionTemplate.selectOne("loginLog.findLatestByUserId", userId);
    }

}
