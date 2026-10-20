package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.ConversationReadReq;
import com.soul.social.dto.req.MessageDeleteReq;
import com.soul.social.dto.req.MessageListReq;
import com.soul.social.dto.req.MessageRecallReq;
import com.soul.social.dto.req.MessageSendReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.ConversationResp;
import com.soul.social.dto.resp.MessageResp;
import com.soul.social.dto.resp.MessageSendResp;

/**
 * IM 服务（沿用既有 im_conversation / im_message）。
 */
public interface ImService {

    /** 会话列表（分页，含未读/最后消息/置顶）。 */
    PageResult<ConversationResp> conversationList(PageQuery query);

    /** 消息列表（会话归属校验，分页）。 */
    PageResult<MessageResp> messageList(MessageListReq req);

    /** 发送消息。 */
    MessageSendResp send(MessageSendReq req);
    /** 撤回消息（2 分钟内，仅本人消息）。 */
    void recall(MessageRecallReq req);

    /** 删除消息（仅对自己不可见）。 */
    void deleteMessage(MessageDeleteReq req);


    /** 标记会话已读。 */
    void read(ConversationReadReq req);

    /**
     * 获取或创建会话（供打招呼复用）。
     *
     * @return sessionUuid
     */
    String getOrCreateConversation(Long fromUserId, Long toUserId, int sessionType);
}
