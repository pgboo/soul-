package com.soul.social.controller.mini;

import com.soul.social.common.result.Result;
import com.soul.social.dto.req.SoulTestSubmitReq;
import com.soul.social.dto.resp.SoulQuestionListResp;
import com.soul.social.dto.resp.SoulTestResultResp;
import com.soul.social.service.SoulTestService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端灵魂测试接口 /api/mini/soulTest/**。
 */
@RestController
@RequestMapping("/api/mini/soulTest")
public class SoulTestController {

    @Resource
    private SoulTestService soulTestService;

    /** 题目列表 */
    @GetMapping("/questions")
    public Result<SoulQuestionListResp> questions() {
        return Result.success(soulTestService.questions());
    }

    /** 提交测试 */
    @PostMapping("/submit")
    public Result<SoulTestResultResp> submit(@Valid @RequestBody SoulTestSubmitReq req) {
        return Result.success("提交成功", soulTestService.submit(req));
    }

    /** 我的测试结果 */
    @GetMapping("/result")
    public Result<SoulTestResultResp> result() {
        return Result.success(soulTestService.myResult());
    }
}
