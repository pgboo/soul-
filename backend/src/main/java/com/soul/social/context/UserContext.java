package com.soul.social.context;

import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;

/**
 * ThreadLocal 当前用户上下文。请求结束必须在 afterCompletion 清理。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /**
     * 获取当前用户 ID，未登录抛 401。用于受保护接口内部。
     */
    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && Boolean.TRUE.equals(user.getAdminFlag());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
