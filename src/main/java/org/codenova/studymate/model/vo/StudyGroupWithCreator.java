package org.codenova.studymate.model.vo;

import lombok.*;
import org.codenova.studymate.model.entity.StudyGroup;
import org.codenova.studymate.model.entity.User;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyGroupWithCreator {
    private StudyGroup group;
    private User creator;
}

