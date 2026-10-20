package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台举报列表项响应。
 */
@Data
public class AdminReportResp {

    private Long reportId;

    private Long reporterId;

    /** 举报人昵称 */
    private String reporterNickname;

    /** 目标类型:1动态 2评论 3用户 */
    private Integer targetType;

    private Long targetId;

    /** 目标摘要（动态正文片段 / 评论内容 / 用户昵称） */
    private String targetSummary;

    /** 举报原因类型 */
    private Integer reasonType;

    /** 补充说明 */
    private String reason;

    /** 状态:1待处理 2已处理 3驳回 */
    private Integer status;

    private String handleResult;

    private Long handlerId;

    private String handleTime;

    private String createTime;
}
