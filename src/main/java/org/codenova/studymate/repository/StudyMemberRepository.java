package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.StudyMember;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class StudyMemberRepository {
    private SqlSessionTemplate sqlSessionTemplate;

    public int createApproved(StudyMember studyMember) {
        return sqlSessionTemplate.insert("studyMember.createApproved", studyMember);
    }

    public int createPending(StudyMember studyMember) {
        return sqlSessionTemplate.insert("studyMember.createPending", studyMember);
    }

    public int updateJoinedAtById(int id) {
        return sqlSessionTemplate.update("studyMember.updateJoinedAtById", id);
    }
}
