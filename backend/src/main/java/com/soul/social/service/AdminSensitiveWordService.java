package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminSensitiveWordDeleteReq;
import com.soul.social.dto.req.AdminSensitiveWordListReq;
import com.soul.social.dto.req.AdminSensitiveWordSaveReq;
import com.soul.social.dto.resp.AdminSensitiveWordResp;

/**
 * 后台敏感词管理服务。
 * <p>新增/修改/删除后刷新内存词库，保证实时生效。
 */
public interface AdminSensitiveWordService {

    /**
     * 敏感词列表：关键字/级别/状态筛选，分页。
     */
    PageResult<AdminSensitiveWordResp> wordPage(AdminSensitiveWordListReq req);

    /**
     * 新增/编辑敏感词，并刷新内存词库。
     */
    void save(AdminSensitiveWordSaveReq req);

    /**
     * 删除敏感词（逻辑删除），并刷新内存词库。
     */
    void delete(AdminSensitiveWordDeleteReq req);
}
