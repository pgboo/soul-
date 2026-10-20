package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台 AI 角色响应。
 */
@Data
public class AdminAiRoleResp {

    private Long roleId;

    private String name;

    private String avatar;

    private String welcomeText;

    private String prompt;

    /** 状态:0禁用 1启用 */
    private Integer status;

    private Integer sort;

    private String createTime;
}
