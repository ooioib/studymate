package org.codenova.studymate.model.query;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class StudyMemberWithGroupDetail {
    private int id;
    private String userId;
    private String groupId;
    private String role;
    private LocalDateTime appliedAt;
    private LocalDateTime joinedAt;
    private String groupName;
    private String groupType;
    private int memberCount;
}
