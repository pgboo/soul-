package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 初始化状态响应。
 */
@Data
public class UserInitStatusResp {

    private Boolean profileComplete;

    private Boolean tagComplete;

    private Boolean testComplete;

    private Boolean allCompleted;

    /** 下一步：profile/tag/test/square */
    private String nextStep;
}
