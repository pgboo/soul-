package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 收藏响应。
 */
@Data
public class CollectResp {

    private Boolean collected;

    private Integer collectCount;

    public CollectResp() {
    }

    public CollectResp(Boolean collected, Integer collectCount) {
        this.collected = collected;
        this.collectCount = collectCount;
    }
}
