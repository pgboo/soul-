package com.soul.social.controller.mini;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.ConversationReadReq;
import com.soul.social.dto.req.MessageDeleteReq;
import com.soul.social.dto.req.MessageListReq;
import com.soul.social.dto.req.MessageRecallReq;
import com.soul.social.dto.req.MessageSendReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.ConversationResp;
import com.soul.social.dto.resp.MessageResp;
import com.soul.social.dto.resp.MessageSendResp;
import com.soul.social.service.ImService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 IM 接口 /api/mini/im/**。
 */
@RestController
@RequestMapping("/api/mini/im")
public class ImController {

    @Resource
    private ImService imService;

    /** 会话列表 */
    @GetMapping("/conversation/list")
    public Result<PageResult<ConversationResp>> conversationList(PageQuery query) {
        return Result.success(imService.conversationList(query));
    }

    /** 消息列表 */
    @GetMapping("/message/list")
    public Result<PageResult<MessageResp>> messageList(@Valid MessageListReq req) {
        return Result.success(imService.messageList(req));
    }

    /** 发送消息 */
    @PostMapping("/message/send")
    public Result<MessageSendResp> send(@Valid @RequestBody MessageSendReq req) {
        return Result.success("发送成功", imService.send(req));
    }

    /** 撤回消息（2 分钟内，仅本人消息） */
    @PostMapping("/message/recall")
    public Result<Void> recall(@Valid @RequestBody MessageRecallReq req) {
        imService.recall(req);
        return Result.success("已撤回", null);
    }

    /** 删除消息（仅自己不可见） */
    @PostMapping("/message/delete")
    public Result<Void> deleteMessage(@Valid @RequestBody MessageDeleteReq req) {
        imService.deleteMessage(req);
        return Result.success("已删除", null);
    }

    /** 标记会话已读 */
    @PostMapping("/conversation/read")
    public Result<Void> read(@Valid @RequestBody ConversationReadReq req) {
        imService.read(req);
        return Result.success("已读", null);
    }
}
