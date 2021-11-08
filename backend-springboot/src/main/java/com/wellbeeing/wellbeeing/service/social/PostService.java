package com.wellbeeing.wellbeeing.service.social;

import com.wellbeeing.wellbeeing.domain.account.Profile;
import com.wellbeeing.wellbeeing.domain.exception.NotFoundException;
import com.wellbeeing.wellbeeing.domain.social.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Page<Post> getMyPosts(Profile creatorParam, Pageable pageable);
    Post getPost(long postId);

    Post addPost(Post post, String creatorName) throws NotFoundException;
    Post updatePost(long id, Post post, String updaterName) throws NotFoundException;
    boolean deletePost(long postId, String cancellerName) throws NotFoundException;
}