package com.soul.social.dto.req;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知列表请求。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoticeListReq extends PageQuery {

    /** 通知类型筛选 */
    private Integer noticeType;

    /** 预留：指定通知 id 列表 */
    private List<Long> noticeIds;
}
