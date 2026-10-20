package com.soul.social.client;

import java.util.List;

/**
 * 敏感词过滤抽象。
 */
public interface SensitiveWordService {

    /** 是否命中敏感词 */
    boolean containsSensitive(String text);

    /** 命中的敏感词列表（用于审计） */
    List<String> hitWords(String text);

    /** 替换为 * */
    String replace(String text);

    /**
     * 重新加载词库（后台维护敏感词后调用，保证实时生效）。
     */
    void reload();
}
