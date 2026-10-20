package com.soul.social.controller.mini;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.CommentListReq;
import com.soul.social.dto.req.DynamicCollectReq;
import com.soul.social.dto.req.DynamicCommentReq;
import com.soul.social.dto.req.DynamicLikeReq;
import com.soul.social.dto.req.DynamicListReq;
import com.soul.social.dto.req.DynamicPublishReq;
import com.soul.social.dto.resp.CollectResp;
import com.soul.social.dto.resp.CommentItemResp;
import com.soul.social.dto.resp.CommentResp;
import com.soul.social.dto.resp.DynamicDetailResp;
import com.soul.social.dto.resp.DynamicItemResp;
import com.soul.social.dto.resp.IdResp;
import com.soul.social.dto.resp.LikeResp;
import com.soul.social.service.DynamicService;
import com.soul.social.service.InteractionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端广场动态接口 /api/mini/dynamic/**。
 */
@RestController
@RequestMapping("/api/mini/dynamic")
public class DynamicController {

    @Resource
    private DynamicService dynamicService;

    @Resource
    private InteractionService interactionService;

    /** 发布瞬间 */
    @PostMapping("/publish")
    public Result<IdResp> publish(@Valid @RequestBody DynamicPublishReq req) {
        Long dynamicId = dynamicService.publish(req);
        return Result.success("发布成功", new IdResp(dynamicId));
    }

    /** 瞬间列表 */
    @GetMapping("/list")
    public Result<PageResult<DynamicItemResp>> list(DynamicListReq req) {
        return Result.success(dynamicService.list(req));
    }

    /** 瞬间详情 */
    @GetMapping("/detail/{id}")
    public Result<DynamicDetailResp> detail(@PathVariable Long id) {
        return Result.success(dynamicService.detail(id));
    }

    /** 删除瞬间 */
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dynamicService.delete(id, UserContext.requireUserId());
        return Result.success("删除成功", null);
    }

    /** 点赞/取消点赞 */
    @PostMapping("/like")
    public Result<LikeResp> like(@Valid @RequestBody DynamicLikeReq req) {
        return Result.success(interactionService.like(req));
    }

    /** 收藏/取消收藏 */
    @PostMapping("/collect")
    public Result<CollectResp> collect(@Valid @RequestBody DynamicCollectReq req) {
        return Result.success(interactionService.collect(req));
    }

    /** 发表评论/回复 */
    @PostMapping("/comment")
    public Result<CommentResp> comment(@Valid @RequestBody DynamicCommentReq req) {
        return Result.success("评论成功", interactionService.comment(req));
    }

    /** 评论列表 */
    @GetMapping("/comment/list")
    public Result<PageResult<CommentItemResp>> commentList(@Valid CommentListReq req) {
        return Result.success(interactionService.commentList(req));
    }
}
