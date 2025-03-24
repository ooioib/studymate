package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.Post;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class PostRepository {
    private SqlSessionTemplate sqlSessionTemplate;

    public int create(Post post) {
        return sqlSessionTemplate.insert("post.create", post);
    }

    public List<Post> findByGroupId(String groupId) {
        return sqlSessionTemplate.selectList("post.findByGroupId", groupId);
    }
}
