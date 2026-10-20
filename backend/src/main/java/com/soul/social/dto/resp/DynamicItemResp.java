package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 瞬间列表项响应。
 */
@Data
public class DynamicItemResp {

    private Long dynamicId;

    private AuthorItem author;

    private String content;

    private List<String> mediaUrls;

    private String topicName;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    /** 我是否点赞 */
    private Boolean liked;

    /** 我是否收藏 */
    private Boolean collected;

    /** 审核状态（本人可见） */
    private Integer auditStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Data
    public static class AuthorItem {
        private Long userId;
        private String nickname;
        private String avatar;

        public AuthorItem() {
        }

        public AuthorItem(Long userId, String nickname, String avatar) {
            this.userId = userId;
            this.nickname = nickname;
            this.avatar = avatar;
        }
    }
}
