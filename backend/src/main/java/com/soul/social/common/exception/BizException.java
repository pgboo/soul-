package com.soul.social.common.exception;

/**
 * 业务异常。抛出后由 {@link GlobalExceptionHandler} 统一转为 Result。
 */
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
