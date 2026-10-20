package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 发送消息响应。
 */
@Data
public class MessageSendResp {

    private String messageUuid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}
