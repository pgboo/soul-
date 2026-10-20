package com.soul.social.client;

import java.util.List;

/**
 * AI 能力抽象。本期仅接口位 + Mock 兜底，不调百炼 DashScope。
 */
public interface AiService {

    /** 生成打招呼话术建议（本期 Mock） */
    String suggestGreet(Long fromUserId, Long toUserId, List<String> sharedTags);

    /** AI 聊天（本期不实现，留接口位） */
    String chat(Long userId, Long roleId, String message);
}
