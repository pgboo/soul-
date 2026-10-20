package com.soul.social.service;

import com.soul.social.dto.req.GreetSendReq;
import com.soul.social.dto.resp.GreetSendResp;

/**
 * 打招呼服务。
 */
public interface GreetService {

    /** 发送打招呼（频控 R014，自动建档会话）。 */
    GreetSendResp send(GreetSendReq req);
}
