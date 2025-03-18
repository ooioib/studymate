package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.Avatar;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;

@AllArgsConstructor
public class AvatarRepository {
    private SqlSessionTemplate template;

    public List<Avatar> findAll() {
        return template.selectList("avatar.findAll");
    }
}
