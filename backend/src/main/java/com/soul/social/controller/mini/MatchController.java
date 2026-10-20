package com.soul.social.controller.mini;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.MatchDislikeReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.MatchUserResp;
import com.soul.social.service.MatchService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端匹配接口 /api/mini/match/**。
 */
@RestController
@RequestMapping("/api/mini/match")
public class MatchController {

    @Resource
    private MatchService matchService;

    /** 推荐用户 */
    @GetMapping("/recommend")
    public Result<PageResult<MatchUserResp>> recommend(PageQuery query) {
        return Result.success(matchService.recommend(query));
    }

    /** 不感兴趣 */
    @PostMapping("/dislike")
    public Result<Void> dislike(@Valid @RequestBody MatchDislikeReq req) {
        matchService.dislike(req);
        return Result.success("已记录", null);
    }
}
