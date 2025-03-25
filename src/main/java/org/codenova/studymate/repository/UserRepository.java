package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.User;
import org.codenova.studymate.model.query.UserWithAvatar;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepository {
    private SqlSessionTemplate template;

    public int create(User user) {
        return template.insert("user.create", user);
    }

    public User findById(String id) {
        return template.selectOne("user.findById", id);
    }

    public UserWithAvatar findWithAvatarById(String id) {
        return template.selectOne("user.findWithAvatarById", id);
    }

    public int updateLoginCountByUserId(String id) {
        return template.update("user.updateLoginCountByUserId", id);
    }
}