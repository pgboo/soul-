package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台登录响应。
 */
@Data
public class AdminLoginResp {

    private String token;

    private Long userId;

    private String nickname;
}
