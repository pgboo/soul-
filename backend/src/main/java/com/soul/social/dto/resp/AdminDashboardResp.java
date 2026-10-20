package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台数据看板汇总响应（今日 / 总量）。
 */
@Data
public class AdminDashboardResp {

    /** 用户总数 */
    private Long userTotal;

    /** 今日新增用户 */
    private Long userToday;

    /** 动态总数（未删除） */
    private Long dynamicTotal;

    /** 今日新增动态 */
    private Long dynamicToday;

    /** 评论总数 */
    private Long commentTotal;

    /** 今日新增评论 */
    private Long commentToday;

    /** 聊天消息总数 */
    private Long messageTotal;

    /** 今日新增聊天消息 */
    private Long messageToday;

    /** 举报总数 */
    private Long reportTotal;

    /** 今日新增举报 */
    private Long reportToday;

    /** 待处理举报数 */
    private Long reportPending;
}
