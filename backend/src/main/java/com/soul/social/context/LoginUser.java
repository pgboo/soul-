package com.soul.social.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户 ID */
    private Long userId;

    /** 是否后台账号（token type=admin） */
    private Boolean adminFlag;

    public LoginUser(Long userId) {
        this.userId = userId;
        this.adminFlag = Boolean.FALSE;
    }
}
