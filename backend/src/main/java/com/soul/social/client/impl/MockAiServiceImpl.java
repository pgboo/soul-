package com.soul.social.client.impl;

import cn.hutool.core.util.StrUtil;
import com.soul.social.client.AiService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 服务 Mock 兜底实现。本期不调百炼，仅返回模板自然话术。
 */
@Slf4j
@Service
public class MockAiServiceImpl implements AiService {

    @Override
    public String suggestGreet(Long fromUserId, Long toUserId, List<String> sharedTags) {
        if (sharedTags != null && !sharedTags.isEmpty()) {
            String tag = sharedTags.get(0);
            return "嗨～看到你也喜欢" + tag + "，感觉我们会很聊得来！";
        }
        return "嗨，很高兴认识你，希望能一起聊聊～";
    }

    @Override
    public String chat(Long userId, Long roleId, String message) {
        log.debug("MockAiService.chat userId={}, roleId={}", userId, roleId);
        if (StrUtil.isBlank(message)) {
            return "你好呀，我在听～";
        }
        return "我听到了你说的话，愿意多和我聊聊吗？";
    }
}
