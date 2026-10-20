package com.soul.social.service;

import com.soul.social.dto.req.LoginReq;
import com.soul.social.dto.req.SendCodeReq;
import com.soul.social.dto.resp.AdminLoginResp;
import com.soul.social.dto.resp.LoginResp;

/**
 * 账号服务。
 */
public interface AuthService {

    /**
     * 发送验证码。开发模式（sms_enable=0）固定 123456 打印日志。
     */
    void sendCode(SendCodeReq req);

    /**
     * 手机号 + 验证码登录，新用户自动注册。
     */
    LoginResp login(LoginReq req, String clientIp);

    /**
     * 退出登录（token 加入黑名单）。
     */
    void logout(String token);

    /**
     * 后台登录。
     */
    AdminLoginResp adminLogin(String username, String password);
}
