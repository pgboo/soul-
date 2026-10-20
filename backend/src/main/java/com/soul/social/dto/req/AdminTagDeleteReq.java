package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台删除标签请求（逻辑删除）。
 */
@Data
public class AdminTagDeleteReq {

    @NotNull(message = "标签 ID 不能为空")
    private Long tagId;
}
