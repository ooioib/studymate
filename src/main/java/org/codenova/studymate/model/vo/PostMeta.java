package org.codenova.studymate.model.vo;


import lombok.*;
import org.codenova.studymate.model.entity.PostReaction;

import java.util.List;

@Setter
@Getter
@Builder
public class PostMeta {
    private int id;
    private String content;
    private String writerName;
    private String writerAvatar;
    private String time;

    private List<PostReaction> reactions;

}
