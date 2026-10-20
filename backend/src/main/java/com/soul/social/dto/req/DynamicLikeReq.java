package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞/取消点赞请求。action: like / cancel。
 */
@Data
public class DynamicLikeReq {

    @NotNull(message = "动态 ID 不能为空")
    private Long dynamicId;

    @NotBlank(message = "操作不能为空")
    private String action;
}
