package com.soul.social.controller.admin;

import com.soul.social.common.result.Result;
import com.soul.social.dto.resp.AdminDashboardResp;
import com.soul.social.service.AdminDashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据看板接口 /api/admin/dashboard/**。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private AdminDashboardService adminDashboardService;

    /** 数据看板汇总（今日/总量） */
    @GetMapping("/summary")
    public Result<AdminDashboardResp> summary() {
        return Result.success(adminDashboardService.summary());
    }
}
