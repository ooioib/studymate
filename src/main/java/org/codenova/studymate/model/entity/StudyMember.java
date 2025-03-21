package org.codenova.studymate.model.entity;


import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyMember {

    private int id;
    private String userId;
    private String groupId;
    private String role;
    private LocalDateTime appliedAt;
    private LocalDateTime joinedAt;
}