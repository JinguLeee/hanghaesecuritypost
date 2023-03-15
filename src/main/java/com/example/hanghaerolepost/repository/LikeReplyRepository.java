package com.example.hanghaerolepost.repository;

import com.example.hanghaerolepost.entity.LikeReply;
import com.example.hanghaerolepost.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeReplyRepository extends JpaRepository<LikeReply, Long> {
    LikeReply findByReplyIdAndUser(Long replyId, User user);

    Long countByReplyId(Long replyId);

}
