package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 评论响应。
 */
@Data
public class CommentResp {

    private Long commentId;

    public CommentResp() {
    }

    public CommentResp(Long commentId) {
        this.commentId = commentId;
    }
}
