package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台删除敏感词请求（逻辑删除）。
 */
@Data
public class AdminSensitiveWordDeleteReq {

    @NotNull(message = "敏感词ID不能为空")
    private Long wordId;
}
