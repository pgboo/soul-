package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 消息列表项响应。
 */
@Data
public class MessageResp {

    private String messageUuid;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private String contentType;

    private Integer sendStatus;

    private Integer readFlag;

    /** 是否已撤回（已撤回时 content 返回空串） */
    private Boolean recalled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}
