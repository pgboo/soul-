package com.soul.social.controller.mini;

import cn.hutool.core.util.StrUtil;
import com.soul.social.annotation.IgnoreAuth;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.LoginReq;
import com.soul.social.dto.req.SendCodeReq;
import com.soul.social.dto.resp.LoginResp;
import com.soul.social.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端账号接口 /api/mini/auth/**。
 */
@RestController
@RequestMapping("/api/mini/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 发送验证码（免鉴权）。
     */
    @IgnoreAuth
    @PostMapping("/code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeReq req) {
        authService.sendCode(req);
        return Result.success("验证码已发送", null);
    }

    /**
     * 登录（免鉴权）。
     */
    @IgnoreAuth
    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        LoginResp resp = authService.login(req, clientIp);
        return Result.success("登录成功", resp);
    }

    /**
     * 退出登录。
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(CommonConstant.HEADER_AUTHORIZATION);
        String token = null;
        if (StrUtil.isNotBlank(header) && header.startsWith(CommonConstant.BEARER_PREFIX)) {
            token = header.substring(CommonConstant.BEARER_PREFIX.length()).trim();
        }
        authService.logout(token);
        return Result.success("退出成功", null);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return StrUtil.blankToDefault(ip, "");
    }
}
