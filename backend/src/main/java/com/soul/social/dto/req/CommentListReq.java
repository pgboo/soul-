package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论列表请求。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentListReq extends PageQuery {

    @NotNull(message = "动态 ID 不能为空")
    private Long dynamicId;
}
