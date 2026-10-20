package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminSensitiveWordDeleteReq;
import com.soul.social.dto.req.AdminSensitiveWordListReq;
import com.soul.social.dto.req.AdminSensitiveWordSaveReq;
import com.soul.social.dto.resp.AdminSensitiveWordResp;
import com.soul.social.service.AdminSensitiveWordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台敏感词管理接口 /api/admin/sensitiveWord/**。
 */
@RestController
@RequestMapping("/api/admin/sensitiveWord")
public class AdminSensitiveWordController {

    @Resource
    private AdminSensitiveWordService adminSensitiveWordService;

    /** 敏感词列表 */
    @GetMapping("/list")
    public Result<PageResult<AdminSensitiveWordResp>> list(AdminSensitiveWordListReq req) {
        return Result.success(adminSensitiveWordService.wordPage(req));
    }

    /** 新增/编辑敏感词（刷新内存词库） */
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody AdminSensitiveWordSaveReq req) {
        adminSensitiveWordService.save(req);
        return Result.success("保存成功", null);
    }

    /** 删除敏感词（逻辑删除，刷新内存词库） */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody AdminSensitiveWordDeleteReq req) {
        adminSensitiveWordService.delete(req);
        return Result.success("删除成功", null);
    }
}
