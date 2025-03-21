package org.codenova.studymate.model.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.codenova.studymate.model.entity.StudyGroup;
import org.codenova.studymate.model.entity.User;

@Setter
@Getter
@Builder
public class StudyGroupWithCreator {
    private StudyGroup group;
    private User creator;
}
