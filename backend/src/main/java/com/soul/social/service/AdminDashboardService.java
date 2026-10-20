package com.soul.social.service;

import com.soul.social.dto.resp.AdminDashboardResp;

/**
 * 后台数据看板服务。
 */
public interface AdminDashboardService {

    /**
     * 数据看板汇总：今日/总量（多表聚合）。
     */
    AdminDashboardResp summary();
}
