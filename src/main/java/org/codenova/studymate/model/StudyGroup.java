package org.codenova.studymate.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class StudyGroup {

    private String id;
    private String name;
    private String type;
    private String goal;
    private String creatorId;
    private LocalDateTime createdAt;
    private int memberCount;
}
