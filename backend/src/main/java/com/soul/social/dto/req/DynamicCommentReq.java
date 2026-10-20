package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论/回复请求。
 */
@Data
public class DynamicCommentReq {

    @NotNull(message = "动态 ID 不能为空")
    private Long dynamicId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 200, message = "评论不能超过 200 字")
    private String content;

    /** 父评论 ID（0=一级） */
    private Long parentId;

    /** 被回复人 ID */
    private Long replyUserId;
}
