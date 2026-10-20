package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送打招呼请求。
 */
@Data
public class GreetSendReq {

    @NotNull(message = "目标用户不能为空")
    private Long toUserId;

    /** 话术（不传则用默认话术） */
    private String content;
}
