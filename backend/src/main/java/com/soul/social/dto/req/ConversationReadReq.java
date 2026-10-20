package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标记会话已读请求。
 */
@Data
public class ConversationReadReq {

    @NotBlank(message = "会话标识不能为空")
    private String sessionUuid;
}
