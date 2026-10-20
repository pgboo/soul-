package com.soul.social.client.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.client.SensitiveWordService;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.entity.SensitiveWord;
import com.soul.social.mapper.SensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 敏感词服务实现。启动时从 sensitive_word 表加载启用词到内存 Set。
 * <p>一期用简单 contains 遍历（词量小）；预留 DFA/Trie 优化位。
 */
@Slf4j
@Service
public class LocalSensitiveWordServiceImpl implements SensitiveWordService {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    /** 内存词库 */
    private volatile Set<String> wordSet = Collections.emptySet();

    @PostConstruct
    public void load() {
        refresh();
    }

    /**
     * 重新加载词库（后台维护敏感词后可调用）。
     */
    public void refresh() {
        List<SensitiveWord> list = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>()
                        .eq(SensitiveWord::getStatus, CommonConstant.STATUS_ENABLE));
        Set<String> set = new HashSet<>();
        for (SensitiveWord sw : list) {
            if (StrUtil.isNotBlank(sw.getWord())) {
                set.add(sw.getWord().trim());
            }
        }
        this.wordSet = set;
        log.info("敏感词库加载完成，共 {} 个词", set.size());
    }

    @Override
    public void reload() {
        refresh();
    }

    @Override
    public boolean containsSensitive(String text) {
        return !hitWords(text).isEmpty();
    }

    @Override
    public List<String> hitWords(String text) {
        if (StrUtil.isBlank(text) || wordSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> hits = new ArrayList<>();
        for (String word : wordSet) {
            if (text.contains(word)) {
                hits.add(word);
            }
        }
        return hits;
    }

    @Override
    public String replace(String text) {
        if (StrUtil.isBlank(text) || wordSet.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : wordSet) {
            if (result.contains(word)) {
                result = result.replace(word, StrUtil.repeat('*', word.length()));
            }
        }
        return result;
    }
}
