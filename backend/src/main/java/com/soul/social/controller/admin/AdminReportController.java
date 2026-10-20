package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminReportHandleReq;
import com.soul.social.dto.req.AdminReportListReq;
import com.soul.social.dto.resp.AdminReportResp;
import com.soul.social.service.AdminReportService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台举报管理接口 /api/admin/report/**。
 */
@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {

    @Resource
    private AdminReportService adminReportService;

    /** 举报列表（状态筛选，分页） */
    @GetMapping("/list")
    public Result<PageResult<AdminReportResp>> list(AdminReportListReq req) {
        return Result.success(adminReportService.reportPage(req));
    }

    /** 处理举报（驳回/下架/禁言/封禁）（R011） */
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody AdminReportHandleReq req) {
        adminReportService.handle(req);
        return Result.success("操作成功", null);
    }
}
