package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息列表请求。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageListReq extends PageQuery {

    @NotBlank(message = "会话标识不能为空")
    private String sessionUuid;
}
