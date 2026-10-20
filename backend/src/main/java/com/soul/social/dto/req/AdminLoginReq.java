package com.soul.social.dto.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台登录请求。
 * <p>字段名按 docs/03-API接口文档.md §3.0 约定为 {@code account}；
 * 同时通过 {@code @JsonAlias} 兼容 {@code username}，避免旧调用方失效。
 */
@Data
public class AdminLoginReq {

    @NotBlank(message = "账号不能为空")
    @JsonAlias({"username"})
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
