package com.soul.social.controller.mini;

import com.soul.social.common.result.Result;
import com.soul.social.dto.req.GreetSendReq;
import com.soul.social.dto.resp.GreetSendResp;
import com.soul.social.service.GreetService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端打招呼接口 /api/mini/greet/**。
 */
@RestController
@RequestMapping("/api/mini/greet")
public class GreetController {

    @Resource
    private GreetService greetService;

    /** 发送打招呼 */
    @PostMapping("/send")
    public Result<GreetSendResp> send(@Valid @RequestBody GreetSendReq req) {
        return Result.success("打招呼成功", greetService.send(req));
    }
}
