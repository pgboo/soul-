package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 拉黑请求。
 */
@Data
public class BlacklistAddReq {

    @NotNull(message = "目标用户不能为空")
    private Long targetUserId;
}
