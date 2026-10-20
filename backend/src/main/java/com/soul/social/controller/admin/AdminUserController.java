package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminUserBanReq;
import com.soul.social.dto.req.AdminUserDetailReq;
import com.soul.social.dto.req.AdminUserListReq;
import com.soul.social.dto.req.AdminUserMuteReq;
import com.soul.social.dto.resp.AdminUserDetailResp;
import com.soul.social.dto.resp.AdminUserResp;
import com.soul.social.service.AdminUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理接口 /api/admin/user/**。
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    /** 用户列表（昵称/手机号/状态搜索，分页；手机号脱敏） */
    @GetMapping("/list")
    public Result<PageResult<AdminUserResp>> list(AdminUserListReq req) {
        return Result.success(adminUserService.userPage(req));
    }

    /** 用户详情（含资料、风险记录；手机号脱敏） */
    @GetMapping("/detail")
    public Result<AdminUserDetailResp> detail(@Valid AdminUserDetailReq req) {
        return Result.success(adminUserService.userDetail(req));
    }

    /** 禁言/解除禁言（R002） */
    @PostMapping("/mute")
    public Result<Void> mute(@Valid @RequestBody AdminUserMuteReq req) {
        adminUserService.mute(req);
        return Result.success("操作成功", null);
    }

    /** 封禁/解封（R001） */
    @PostMapping("/ban")
    public Result<Void> ban(@Valid @RequestBody AdminUserBanReq req) {
        adminUserService.ban(req);
        return Result.success("操作成功", null);
    }
}
