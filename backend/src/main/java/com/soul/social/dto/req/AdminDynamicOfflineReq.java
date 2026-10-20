package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台瞬间下架/恢复请求（R008）。
 */
@Data
public class AdminDynamicOfflineReq {

    @NotNull(message = "动态ID不能为空")
    private Long dynamicId;

    /** 原因（下架建议填写） */
    private String reason;
}
