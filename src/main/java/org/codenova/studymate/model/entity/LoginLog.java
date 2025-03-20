package org.codenova.studymate.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class LoginLog {

    private int id;
    private String userId;
    private String datetime;
    private LocalDateTime loginAt;

}
