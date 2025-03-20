package org.codenova.studymate.model.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private String id;
    private String password;
    private String name;
    private int avatarId;
    private int loginCount;
    private int studyTime;
}
