package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏/取消收藏请求。action: collect / cancel。
 */
@Data
public class DynamicCollectReq {

    @NotNull(message = "动态 ID 不能为空")
    private Long dynamicId;

    @NotBlank(message = "操作不能为空")
    private String action;
}
