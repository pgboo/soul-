package com.soul.social.common.exception;

/**
 * 统一错误码枚举（对齐 docs/03-API接口文档.md §1.5 错误码表）。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    CODE_ERROR(1001, "验证码错误"),
    CODE_EXPIRED(1002, "验证码已过期"),
    USER_BANNED(1003, "账号已被封禁"),
    USER_MUTED(1004, "账号已被禁言"),
    PROFILE_INCOMPLETE(1005, "资料尚未完善"),
    CODE_NOT_SENT(1006, "需要获取验证码"),
    CODE_TOO_OFTEN(1007, "验证码发送过于频繁"),

    TAG_COUNT_INVALID(2001, "标签数量须为 3-10 个"),
    TAG_UNAVAILABLE(2002, "标签不可用"),

    TEST_ALREADY_SUBMITTED(3001, "请勿重复提交灵魂测试"),
    TEST_QUESTION_EMPTY(3002, "灵魂测试题目为空"),

    CONTENT_SENSITIVE(4001, "内容含违规信息"),
    DYNAMIC_NOT_FOUND(4002, "瞬间不存在或已删除"),
    NO_PERMISSION_DELETE(4003, "仅可操作自己的内容"),
    CONTENT_EMPTY(4004, "内容不能为空"),

    ALREADY_OPERATED(5001, "请勿重复操作"),
    CONTENT_OFFLINE(5002, "内容已下架，不可操作"),

    BLACKLISTED(6001, "对方已拉黑你或你已拉黑对方"),
    STRANGER_MSG_CLOSED(6002, "对方未开启陌生人私信"),
    CANNOT_OPERATE_SELF(6003, "不可对自己操作"),

    GREET_LIMIT(7001, "打招呼已达今日上限"),
    GREET_REPEAT(7002, "今日已向该用户打过招呼"),

    RECOMMEND_CLOSED(8001, "对方已关闭被推荐"),

    NOTICE_NOT_FOUND(9001, "通知不存在或无权操作"),
    CONVERSATION_NOT_FOUND(9002, "会话不存在或无权操作");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
