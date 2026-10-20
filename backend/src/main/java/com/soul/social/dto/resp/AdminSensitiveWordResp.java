package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台敏感词列表项响应。
 */
@Data
public class AdminSensitiveWordResp {

    private Long wordId;

    private String word;

    private String category;

    /** 级别:1替换 2拦截 */
    private Integer level;

    /** 状态:0禁用 1启用 */
    private Integer status;

    private String createTime;
}
