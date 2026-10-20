package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 打招呼响应。
 */
@Data
public class GreetSendResp {

    private String sessionUuid;

    public GreetSendResp() {
    }

    public GreetSendResp(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }
}
