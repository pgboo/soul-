package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminReportHandleReq;
import com.soul.social.dto.req.AdminReportListReq;
import com.soul.social.dto.resp.AdminReportResp;

/**
 * 后台举报管理服务（P0）。
 */
public interface AdminReportService {

    /**
     * 举报列表：状态筛选，分页。
     */
    PageResult<AdminReportResp> reportPage(AdminReportListReq req);

    /**
     * 处理举报（驳回/下架/禁言/封禁）（R011）：
     * 改 report_record 处理字段 + 写 audit_record，处理动作真实影响目标对象状态。
     */
    void handle(AdminReportHandleReq req);
}
