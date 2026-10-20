package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台封禁/解封请求（R001）。
 */
@Data
public class AdminUserBanReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** true=封禁 false=解封 */
    @NotNull(message = "操作类型不能为空")
    private Boolean ban;

    @NotBlank(message = "原因不能为空")
    private String reason;
}
