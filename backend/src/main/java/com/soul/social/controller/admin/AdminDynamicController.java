package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminDynamicAuditReq;
import com.soul.social.dto.req.AdminDynamicListReq;
import com.soul.social.dto.req.AdminDynamicOfflineReq;
import com.soul.social.dto.resp.AdminDynamicResp;
import com.soul.social.service.AdminDynamicService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台瞬间管理接口 /api/admin/dynamic/**。
 */
@RestController
@RequestMapping("/api/admin/dynamic")
public class AdminDynamicController {

    @Resource
    private AdminDynamicService adminDynamicService;

    /** 瞬间列表（状态/审核状态筛选，分页；含已逻辑删除） */
    @GetMapping("/list")
    public Result<PageResult<AdminDynamicResp>> list(AdminDynamicListReq req) {
        return Result.success(adminDynamicService.dynamicPage(req));
    }

    /** 审核瞬间（通过/拒绝）（R007） */
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody AdminDynamicAuditReq req) {
        adminDynamicService.audit(req);
        return Result.success("操作成功", null);
    }

    /** 下架瞬间（R008） */
    @PostMapping("/offline")
    public Result<Void> offline(@Valid @RequestBody AdminDynamicOfflineReq req) {
        adminDynamicService.offline(req);
        return Result.success("操作成功", null);
    }

    /** 恢复瞬间 */
    @PostMapping("/restore")
    public Result<Void> restore(@Valid @RequestBody AdminDynamicOfflineReq req) {
        adminDynamicService.restore(req);
        return Result.success("操作成功", null);
    }
}
