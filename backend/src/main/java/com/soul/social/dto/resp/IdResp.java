package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 通用 ID 响应（发布动态返回 dynamicId 等）。
 */
@Data
public class IdResp {

    private Long dynamicId;

    public IdResp() {
    }

    public IdResp(Long dynamicId) {
        this.dynamicId = dynamicId;
    }
}
