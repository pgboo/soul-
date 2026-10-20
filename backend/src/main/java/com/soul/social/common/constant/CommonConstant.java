package com.soul.social.common.constant;

/**
 * 通用常量。
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /** 请求头 Authorization */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** Bearer 前缀 */
    public static final String BEARER_PREFIX = "Bearer ";

    /** 用户端 token 类型 */
    public static final String TOKEN_TYPE_MINI = "mini";

    /** 后台 token 类型 */
    public static final String TOKEN_TYPE_ADMIN = "admin";

    /** 未删除 */
    public static final int NOT_DELETED = 0;

    /** 已删除 */
    public static final int DELETED = 1;

    /** 通用启用状态 */
    public static final int STATUS_ENABLE = 1;

    /** 通用禁用状态 */
    public static final int STATUS_DISABLE = 0;

    /** 用户状态：正常 */
    public static final int USER_STATUS_NORMAL = 1;

    /** 用户状态：封禁 */
    public static final int USER_STATUS_BANNED = 2;

    /** 通用是/否 */
    public static final int YES = 1;
    public static final int NO = 0;

    /** 开发模式固定验证码 */
    public static final String DEV_FIXED_SMS_CODE = "123456";

    /** 验证码有效期（秒），5 分钟 */
    public static final long SMS_CODE_TTL_SECONDS = 300L;

    /** 验证码发送间隔（秒） */
    public static final long SMS_CODE_SEND_INTERVAL_SECONDS = 60L;

    /** 分页默认值 */
    public static final long DEFAULT_PAGE_NUM = 1L;
    public static final long DEFAULT_PAGE_SIZE = 10L;
    public static final long MAX_PAGE_SIZE = 100L;

    // ---------- 风控记录类型 risk_user_record.risk_type ----------
    /** 风控类型：禁言 */
    public static final int RISK_TYPE_MUTE = 1;
    /** 风控类型：封禁 */
    public static final int RISK_TYPE_BAN = 2;
    /** 风控类型：观察 */
    public static final int RISK_TYPE_WATCH = 3;

    /** 风控记录状态：解除 */
    public static final int RISK_STATUS_RELEASED = 0;
    /** 风控记录状态：生效 */
    public static final int RISK_STATUS_ACTIVE = 1;

    // ---------- 审核记录 audit_record ----------
    /** 审核业务类型：动态 */
    public static final int AUDIT_BIZ_DYNAMIC = 1;
    /** 审核业务类型：评论 */
    public static final int AUDIT_BIZ_COMMENT = 2;
    /** 审核业务类型：资料 */
    public static final int AUDIT_BIZ_PROFILE = 3;
    /** 审核业务类型：昵称 */
    public static final int AUDIT_BIZ_NICKNAME = 4;

    /** 审核结果：待审 */
    public static final int AUDIT_STATUS_PENDING = 1;
    /** 审核结果：通过 */
    public static final int AUDIT_STATUS_PASS = 2;
    /** 审核结果：拒绝 */
    public static final int AUDIT_STATUS_REJECT = 3;

    // ---------- 动态 dynamic_content ----------
    /** 动态状态：正常 */
    public static final int DYNAMIC_STATUS_NORMAL = 1;
    /** 动态状态：下架 */
    public static final int DYNAMIC_STATUS_OFFLINE = 2;

    /** 动态审核状态：已过审 */
    public static final int DYNAMIC_AUDIT_PASS = 0;
    /** 动态审核状态：待审核 */
    public static final int DYNAMIC_AUDIT_PENDING = 1;
    /** 动态审核状态：拒绝 */
    public static final int DYNAMIC_AUDIT_REJECT = 2;

    // ---------- 举报 report_record ----------
    /** 举报目标类型：动态 */
    public static final int REPORT_TARGET_DYNAMIC = 1;
    /** 举报目标类型：评论 */
    public static final int REPORT_TARGET_COMMENT = 2;
    /** 举报目标类型：用户 */
    public static final int REPORT_TARGET_USER = 3;

    /** 举报状态：待处理 */
    public static final int REPORT_STATUS_PENDING = 1;
    /** 举报状态：已处理 */
    public static final int REPORT_STATUS_HANDLED = 2;
    /** 举报状态：驳回 */
    public static final int REPORT_STATUS_REJECT = 3;
}
