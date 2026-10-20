package com.soul.social.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt 密码工具封装。
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密明文密码。
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 校验明文密码与密文是否匹配。密文为空时返回 false。
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}
