package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminAiRoleDeleteReq;
import com.soul.social.dto.req.AdminAiRoleListReq;
import com.soul.social.dto.req.AdminAiRoleSaveReq;
import com.soul.social.dto.resp.AdminAiRoleResp;

/**
 * 后台 AI 角色管理服务。本期仅 CRUD，不做真实 AI 调用。
 */
public interface AdminAiRoleService {

    /**
     * AI 角色列表：状态/关键字筛选，分页。
     */
    PageResult<AdminAiRoleResp> rolePage(AdminAiRoleListReq req);

    /**
     * 新增/编辑 AI 角色。
     */
    void save(AdminAiRoleSaveReq req);

    /**
     * 删除 AI 角色（逻辑删除）。
     */
    void delete(AdminAiRoleDeleteReq req);
}
