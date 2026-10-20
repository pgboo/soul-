package com.soul.social.interceptor;

import cn.hutool.core.util.StrUtil;
import com.soul.social.client.CacheService;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.constant.RedisKeyConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.context.LoginUser;
import com.soul.social.context.UserContext;
import com.soul.social.entity.User;
import com.soul.social.mapper.UserMapper;
import com.soul.social.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户端鉴权拦截器。/api/mini/** 校验 JWT，写入 UserContext，校验封禁。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private CacheService cacheService;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非 HandlerMethod（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String header = request.getHeader(CommonConstant.HEADER_AUTHORIZATION);
        if (StrUtil.isBlank(header) || !header.startsWith(CommonConstant.BEARER_PREFIX)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        String token = header.substring(CommonConstant.BEARER_PREFIX.length()).trim();
        if (cacheService.hasKey(RedisKeyConstant.LOGOUT_TOKEN_PREFIX + token)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        String type = claims.get("type", String.class);
        if (!CommonConstant.TOKEN_TYPE_MINI.equals(type)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        Long userId;
        try {
            userId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        // 校验封禁
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_BANNED) {
            throw new BizException(ResultCode.USER_BANNED);
        }
        UserContext.set(new LoginUser(userId, Boolean.FALSE));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
