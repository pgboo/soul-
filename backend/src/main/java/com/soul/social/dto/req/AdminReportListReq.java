package com.soul.social.dto.req;

import lombok.Data;

/**
 * 后台举报列表查询请求（继承分页）。
 */
@Data
public class AdminReportListReq extends PageQuery {

    /** 状态:1待处理 2已处理 3驳回 */
    private Integer status;

    /** 目标类型:1动态 2评论 3用户 */
    private Integer targetType;
}
