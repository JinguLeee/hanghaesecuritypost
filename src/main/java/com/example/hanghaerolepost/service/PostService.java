package com.example.hanghaerolepost.service;

import com.example.hanghaerolepost.dto.PostRequestDto;
import com.example.hanghaerolepost.dto.PostResponseDto;
import com.example.hanghaerolepost.dto.ReplyResponseDto;
import com.example.hanghaerolepost.entity.*;
import com.example.hanghaerolepost.repository.LikePostRepository;
import com.example.hanghaerolepost.repository.LikeReplyRepository;
import com.example.hanghaerolepost.repository.PostRepository;
import com.example.hanghaerolepost.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final LikePostRepository likePostRepository;
    private final LikeReplyRepository likeReplyRepository;

    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto, User user) {
        Post post = postRepository.saveAndFlush(new Post(requestDto, user));
        return new PostResponseDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getPost() {
        List<Post> postList = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : postList) {
            List<ReplyResponseDto> replyResponseList = getReplyResponseList(post);
            postResponseDtoList.add(new PostResponseDto(post, replyResponseList, countPostLike(post.getId())));
        }

        return postResponseDtoList;
    }

    @Transactional
    public PostResponseDto getOnePost(Long postId) {
        Post post = getPost(postId);    // 게시글이 존재하는지 확인 후 가져온다
        return new PostResponseDto(post, getReplyResponseList(post), countPostLike(postId));
    }

    @Transactional
    public PostResponseDto update(Long postId, PostRequestDto postRequestDto, User user) {
        Post post = getPost(postId);    // 게시글이 존재하는지 확인 후 가져온다

        checkPostRole(postId, user);  // 권한을 확인한다 (자신이 쓴 글인지 확인)

        post.update(postRequestDto);
        return new PostResponseDto(post, getReplyResponseList(post), countPostLike(postId));
    }

    private Long countPostLike(Long postId) {
        return likePostRepository.countByPostId(postId);
    }

    private List<ReplyResponseDto> getReplyResponseList (Post post) {
        List<ReplyResponseDto> replyResponseList = new ArrayList<>();
        for (Reply reply : post.getReplyList()) {
            replyResponseList.add(new ReplyResponseDto(reply, reply.getUser().getUsername(), countReplyLike(reply.getId())));
        }
        return replyResponseList;
    }

    private Long countReplyLike(Long replyId) {
        return likeReplyRepository.countByReplyId(replyId);
    }

    @Transactional
    public ResponseEntity<String> delete(Long postId, User user) {
        getPost(postId);        // 게시글이 존재하는지 확인 후 가져온다

        checkPostRole(postId, user);  // 권한을 확인한다 (자신이 쓴 글인지 확인)

        replyRepository.deleteByPostId(postId); // 댓글 먼저 삭제
        postRepository.deleteById(postId);
        return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
    }

    private void checkPostRole(Long id, User user) {
        if (user.getRole() == UserRoleEnum.ADMIN) return;
        postRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new IllegalArgumentException("권한이 없습니다.")
        );
    }

    public ResponseEntity<String> likePost(Long postId, User user) {
        getPost(postId);        // 게시글이 존재하는지 확인 후 가져온다

        LikePost likePost = getLikePost(postId, user);  // 내가 좋아요 했는지 가져온다
        if (likePost == null){
            // 좋아요 없으면 좋아요 추가
            likePostRepository.saveAndFlush(new LikePost(postId, user));
            return ResponseEntity.status(HttpStatus.OK).body("좋아요");
        } else {
            // 좋아요 중이면 삭제
            likePostRepository.deleteById(likePost.getId());
            return ResponseEntity.status(HttpStatus.OK).body("좋아요 취소!");
        }
    }

    private Post getPost(Long id) {
        return postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시글이 존재하지 않습니다.")
        );
    }

    private LikePost getLikePost(Long postId, User user) {
        return likePostRepository.findByPostIdAndUser(postId, user);
    }

}