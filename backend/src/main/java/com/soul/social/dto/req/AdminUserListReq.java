package com.soul.social.dto.req;

import lombok.Data;

/**
 * 后台用户列表查询请求（继承分页）。
 */
@Data
public class AdminUserListReq extends PageQuery {

    /** 昵称（模糊匹配） */
    private String nickname;

    /** 手机号（模糊匹配，在 user_sensitive_info 中检索） */
    private String phone;

    /** 用户状态:1正常 2封禁 */
    private Integer status;
}
