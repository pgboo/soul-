package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台删除 AI 角色请求（逻辑删除）。
 */
@Data
public class AdminAiRoleDeleteReq {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
