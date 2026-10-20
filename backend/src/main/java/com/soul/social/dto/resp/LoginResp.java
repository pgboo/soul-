package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 登录响应。
 */
@Data
public class LoginResp {

    private String token;

    private Long userId;

    private String nickname;

    private String avatar;

    private Boolean profileComplete;

    private Boolean tagComplete;

    private Boolean testComplete;
}
