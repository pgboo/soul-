package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.NoticeListReq;
import com.soul.social.dto.req.NoticeReadReq;
import com.soul.social.dto.resp.NoticeResp;

/**
 * 站内通知服务（用户端查询）。
 */
public interface UserNoticeService {

    /** 通知列表（分页）。 */
    PageResult<NoticeResp> list(NoticeListReq req);

    /** 通知已读（指定或不传全部）。 */
    void read(NoticeReadReq req);
}
