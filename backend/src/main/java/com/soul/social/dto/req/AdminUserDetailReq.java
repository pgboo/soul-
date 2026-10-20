package com.soul.social.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台用户详情查询请求。
 */
@Data
public class AdminUserDetailReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
