package com.soul.social.common.constant;

/**
 * 缓存 key 前缀常量。
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
    }

    /** 验证码：sms:code:{phone} */
    public static final String SMS_CODE_PREFIX = "sms:code:";

    /** 验证码发送频控：sms:limit:{phone} */
    public static final String SMS_LIMIT_PREFIX = "sms:limit:";

    /** 登出 token 黑名单：logout:token:{token} */
    public static final String LOGOUT_TOKEN_PREFIX = "logout:token:";

    /** 打招呼每日计数：greet:daily:{userId}:count */
    public static final String GREET_DAILY_PREFIX = "greet:daily:";
}
