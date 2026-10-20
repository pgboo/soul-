package com.soul.social.common.constant;

/**
 * system_config.config_key 常量。
 */
public final class SysConfigKey {

    private SysConfigKey() {
    }

    /** 短信通道开关：0 开发模式固定验证码，1 真实短信 */
    public static final String SMS_ENABLE = "sms_enable";

    /** token 有效期天数（可选覆盖） */
    public static final String TOKEN_EXPIRE_DAYS = "token_expire_days";

    /** 打招呼每日上限（可选覆盖） */
    public static final String GREET_DAILY_LIMIT = "greet_daily_limit";
}
