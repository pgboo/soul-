package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台灵魂测试结果类型响应。
 */
@Data
public class AdminResultResp {

    private Long resultId;

    private String resultCode;

    private String name;

    private String icon;

    private String description;

    private String socialAdvice;
}
