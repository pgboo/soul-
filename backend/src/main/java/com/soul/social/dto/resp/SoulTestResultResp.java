package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 灵魂测试结果响应。
 */
@Data
public class SoulTestResultResp {

    private String resultCode;

    private String name;

    private String icon;

    private String description;

    private String socialAdvice;
}
