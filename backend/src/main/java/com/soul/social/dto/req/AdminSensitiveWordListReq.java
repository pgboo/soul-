package com.soul.social.dto.req;

import lombok.Data;

/**
 * 后台敏感词列表查询请求（继承分页）。
 */
@Data
public class AdminSensitiveWordListReq extends PageQuery {

    /** 关键字（词模糊） */
    private String keyword;

    /** 级别:1替换 2拦截 */
    private Integer level;

    /** 状态:0禁用 1启用 */
    private Integer status;
}
