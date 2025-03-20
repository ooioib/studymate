package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.StudyGroup;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class StudyGroupRepository {
    private SqlSessionTemplate sqlSessionTemplate;

    public int create(StudyGroup studyGroup) {
        return sqlSessionTemplate.insert("studyGroup.create", studyGroup);
    }

    public int addMemberCountById(String id){
        return sqlSessionTemplate.update("studyGroup.addMemberCountById", id);
    }

}
