package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 不感兴趣请求。
 */
@Data
public class MatchDislikeReq {

    @NotNull(message = "目标用户不能为空")
    private Long targetUserId;

    private String reason;
}
