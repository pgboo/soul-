package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 会话列表项响应。
 */
@Data
public class ConversationResp {

    private String sessionUuid;

    private DynamicItemResp.AuthorItem targetUser;

    /** 类型:1普通 2打招呼 3AI */
    private Integer sessionType;

    private Integer unreadCount;

    private String lastMsgPreview;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMsgTime;

    /** 置顶 */
    private Boolean topFlag;
}
