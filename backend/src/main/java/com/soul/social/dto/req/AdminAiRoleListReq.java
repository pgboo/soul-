package com.soul.social.dto.req;

import lombok.Data;

/**
 * 后台 AI 角色列表查询请求（继承分页）。
 */
@Data
public class AdminAiRoleListReq extends PageQuery {

    /** 状态:0禁用 1启用 */
    private Integer status;

    /** 角色名关键字（模糊） */
    private String keyword;
}
