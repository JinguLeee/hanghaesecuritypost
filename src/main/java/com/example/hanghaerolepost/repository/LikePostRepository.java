package com.example.hanghaerolepost.repository;

import com.example.hanghaerolepost.entity.LikePost;
import com.example.hanghaerolepost.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikePostRepository extends JpaRepository<LikePost, Long> {
    LikePost findByPostIdAndUser(Long postId, User user);

    Long countByPostId(Long postId);

}
