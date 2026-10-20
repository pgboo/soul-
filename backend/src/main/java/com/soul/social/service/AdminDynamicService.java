package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminDynamicAuditReq;
import com.soul.social.dto.req.AdminDynamicListReq;
import com.soul.social.dto.req.AdminDynamicOfflineReq;
import com.soul.social.dto.resp.AdminDynamicResp;

/**
 * 后台瞬间管理服务（P0）。
 */
public interface AdminDynamicService {

    /**
     * 瞬间列表：支持状态/审核状态筛选，分页；后台含已逻辑删除的数据。
     */
    PageResult<AdminDynamicResp> dynamicPage(AdminDynamicListReq req);

    /**
     * 审核瞬间（通过/拒绝）（R007）：改 dynamic_content.audit_status + 写 audit_record。
     */
    void audit(AdminDynamicAuditReq req);

    /**
     * 下架瞬间（R008）：status=2 + 写 audit_record。
     */
    void offline(AdminDynamicOfflineReq req);

    /**
     * 恢复瞬间：status=1 + 写 audit_record。
     */
    void restore(AdminDynamicOfflineReq req);
}
