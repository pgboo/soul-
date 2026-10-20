package com.soul.social.controller.mini;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.NoticeListReq;
import com.soul.social.dto.req.NoticeReadReq;
import com.soul.social.dto.resp.NoticeResp;
import com.soul.social.service.UserNoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端通知接口 /api/mini/notice/**。
 */
@RestController
@RequestMapping("/api/mini/notice")
public class NoticeController {

    @Resource
    private UserNoticeService userNoticeService;

    /** 通知列表 */
    @GetMapping("/list")
    public Result<PageResult<NoticeResp>> list(NoticeListReq req) {
        return Result.success(userNoticeService.list(req));
    }

    /** 通知已读 */
    @PostMapping("/read")
    public Result<Void> read(@RequestBody NoticeReadReq req) {
        userNoticeService.read(req);
        return Result.success("已读", null);
    }
}
