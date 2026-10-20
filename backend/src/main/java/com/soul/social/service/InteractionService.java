package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.CommentListReq;
import com.soul.social.dto.req.DynamicCollectReq;
import com.soul.social.dto.req.DynamicCommentReq;
import com.soul.social.dto.req.DynamicLikeReq;
import com.soul.social.dto.resp.CollectResp;
import com.soul.social.dto.resp.CommentItemResp;
import com.soul.social.dto.resp.CommentResp;
import com.soul.social.dto.resp.LikeResp;

/**
 * 广场互动服务（点赞/收藏/评论）。
 */
public interface InteractionService {

    /** 点赞/取消点赞（幂等）。 */
    LikeResp like(DynamicLikeReq req);

    /** 收藏/取消收藏（幂等）。 */
    CollectResp collect(DynamicCollectReq req);

    /** 发表评论/回复，返回 commentId。 */
    CommentResp comment(DynamicCommentReq req);

    /** 评论列表（分页，状态过滤）。 */
    PageResult<CommentItemResp> commentList(CommentListReq req);
}
