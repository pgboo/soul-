package com.soul.social.dto.req;

import lombok.Data;

/**
 * 后台瞬间列表查询请求（继承分页）。
 * <p>后台可见已逻辑删除的数据，故不传 isDelete 条件。
 */
@Data
public class AdminDynamicListReq extends PageQuery {

    /** 状态:1正常 2下架 */
    private Integer status;

    /** 审核状态:0已过审 1待审核 2拒绝 */
    private Integer auditStatus;

    /** 作者 ID */
    private Long userId;

    /** 正文关键字（模糊） */
    private String keyword;
}
