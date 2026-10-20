package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送消息请求。
 */
@Data
public class MessageSendReq {

    @NotBlank(message = "会话标识不能为空")
    private String sessionUuid;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息不能超过 500 字")
    private String content;
}
