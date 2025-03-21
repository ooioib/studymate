package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.StudyGroup;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public int subtractMemberCountById(String id){
        return sqlSessionTemplate.update("studyGroup.subtractMemberCountById", id);
    }

    public List<StudyGroup> findByNameLikeOrGoalLike(String word){
        return sqlSessionTemplate.selectList("studyGroup.findByNameLikeOrGoalLike", word);
    }

    public StudyGroup findById(String id){
        return sqlSessionTemplate.selectOne("studyGroup.findById", id);
    }
}
