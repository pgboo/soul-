package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 黑名单列表项。
 */
@Data
public class BlacklistItemResp {

    private Long userId;

    private String nickname;

    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime blacklistTime;
}
