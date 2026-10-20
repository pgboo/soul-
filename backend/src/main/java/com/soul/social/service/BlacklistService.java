package com.soul.social.service;

import com.soul.social.dto.req.BlacklistAddReq;
import com.soul.social.dto.req.BlacklistRemoveReq;
import com.soul.social.dto.resp.BlacklistItemResp;
import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.PageQuery;

/**
 * 黑名单服务。
 */
public interface BlacklistService {

    void add(BlacklistAddReq req);

    void remove(BlacklistRemoveReq req);

    PageResult<BlacklistItemResp> list(PageQuery query);

    /** 双向判断：a 拉黑 b 或 b 拉黑 a。 */
    boolean isBlacklisted(Long userId, Long targetUserId);
}
