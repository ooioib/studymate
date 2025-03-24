package org.codenova.studymate.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Post {
    private int id;
    private String groupId;
    private String writerId;
    private String content;
    private LocalDateTime wroteAt;
}
