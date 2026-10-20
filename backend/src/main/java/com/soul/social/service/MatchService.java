package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.MatchDislikeReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.MatchUserResp;

/**
 * 匹配推荐服务。
 */
public interface MatchService {

    /** 推荐用户（含匹配度），分页。 */
    PageResult<MatchUserResp> recommend(PageQuery query);

    /** 不感兴趣。 */
    void dislike(MatchDislikeReq req);
}
