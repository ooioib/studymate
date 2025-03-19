package org.codenova.studymate.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class StudyMember {

    private int id;
    private String userId;
    private String groupId;
    private String role;
    private LocalDateTime appliedAt;
    private LocalDateTime joinedAt;
}
