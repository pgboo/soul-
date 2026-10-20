package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 通知列表项响应。
 */
@Data
public class NoticeResp {

    private Long noticeId;

    private Integer noticeType;

    private String title;

    private String content;

    private DynamicItemResp.AuthorItem fromUser;

    private Integer targetType;

    private Long targetId;

    private Integer readFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
