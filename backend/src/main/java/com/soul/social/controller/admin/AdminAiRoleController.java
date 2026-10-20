package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminAiRoleDeleteReq;
import com.soul.social.dto.req.AdminAiRoleListReq;
import com.soul.social.dto.req.AdminAiRoleSaveReq;
import com.soul.social.dto.resp.AdminAiRoleResp;
import com.soul.social.service.AdminAiRoleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 AI 角色管理接口 /api/admin/ai/role/**。本期仅 CRUD。
 */
@RestController
@RequestMapping("/api/admin/ai/role")
public class AdminAiRoleController {

    @Resource
    private AdminAiRoleService adminAiRoleService;

    /** AI 角色列表 */
    @GetMapping("/list")
    public Result<PageResult<AdminAiRoleResp>> list(AdminAiRoleListReq req) {
        return Result.success(adminAiRoleService.rolePage(req));
    }

    /** 新增/编辑 AI 角色 */
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody AdminAiRoleSaveReq req) {
        adminAiRoleService.save(req);
        return Result.success("保存成功", null);
    }

    /** 删除 AI 角色（逻辑删除） */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody AdminAiRoleDeleteReq req) {
        adminAiRoleService.delete(req);
        return Result.success("删除成功", null);
    }
}
