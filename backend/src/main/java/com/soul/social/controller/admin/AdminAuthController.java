package com.soul.social.controller.admin;

import com.soul.social.annotation.IgnoreAuth;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminLoginReq;
import com.soul.social.dto.resp.AdminLoginResp;
import com.soul.social.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台登录接口 /api/admin/auth/**。
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Resource
    private AuthService authService;

    /** 后台登录（免鉴权） */
    @IgnoreAuth
    @PostMapping("/login")
    public Result<AdminLoginResp> login(@Valid @RequestBody AdminLoginReq req) {
        AdminLoginResp resp = authService.adminLogin(req.getAccount(), req.getPassword());
        return Result.success("登录成功", resp);
    }
}
