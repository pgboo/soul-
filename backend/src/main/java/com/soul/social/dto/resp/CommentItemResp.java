package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评论项响应。
 */
@Data
public class CommentItemResp {

    private Long commentId;

    private DynamicItemResp.AuthorItem user;

    private String content;

    private Long parentId;

    /** 被回复人（可空） */
    private ReplyUser replyUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Data
    public static class ReplyUser {
        private Long userId;
        private String nickname;

        public ReplyUser() {
        }

        public ReplyUser(Long userId, String nickname) {
            this.userId = userId;
            this.nickname = nickname;
        }
    }
}
