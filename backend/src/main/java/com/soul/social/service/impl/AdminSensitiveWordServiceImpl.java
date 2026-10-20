package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.client.SensitiveWordService;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminSensitiveWordDeleteReq;
import com.soul.social.dto.req.AdminSensitiveWordListReq;
import com.soul.social.dto.req.AdminSensitiveWordSaveReq;
import com.soul.social.dto.resp.AdminSensitiveWordResp;
import com.soul.social.entity.SensitiveWord;
import com.soul.social.mapper.SensitiveWordMapper;
import com.soul.social.service.AdminSensitiveWordService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台敏感词管理服务实现。
 * <p>增删改后调用 {@link SensitiveWordService#reload()} 刷新内存词库，保证实时生效。
 */
@Slf4j
@Service
public class AdminSensitiveWordServiceImpl implements AdminSensitiveWordService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Override
    public PageResult<AdminSensitiveWordResp> wordPage(AdminSensitiveWordListReq req) {
        Page<SensitiveWord> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<SensitiveWord> result = sensitiveWordMapper.selectPage(page,
                new LambdaQueryWrapper<SensitiveWord>()
                        .like(StrUtil.isNotBlank(req.getKeyword()), SensitiveWord::getWord, req.getKeyword())
                        .eq(req.getLevel() != null, SensitiveWord::getLevel, req.getLevel())
                        .eq(req.getStatus() != null, SensitiveWord::getStatus, req.getStatus())
                        .orderByDesc(SensitiveWord::getId));

        List<AdminSensitiveWordResp> items = new ArrayList<>();
        for (SensitiveWord sw : result.getRecords()) {
            AdminSensitiveWordResp resp = new AdminSensitiveWordResp();
            resp.setWordId(sw.getId());
            resp.setWord(sw.getWord());
            resp.setCategory(sw.getCategory());
            resp.setLevel(sw.getLevel());
            resp.setStatus(sw.getStatus());
            resp.setCreateTime(formatTime(sw.getCreateTime()));
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(AdminSensitiveWordSaveReq req) {
        String word = req.getWord().trim();
        if (req.getWordId() != null) {
            SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(req.getWordId());
            if (sensitiveWord == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            Long dupCount = sensitiveWordMapper.countByWordIncludeDeleted(word, req.getWordId());
            if (dupCount != null && dupCount > 0) {
                throw new BizException(ResultCode.PARAM_ERROR, "敏感词已存在");
            }
            sensitiveWord.setWord(word);
            sensitiveWord.setCategory(StrUtil.nullToEmpty(req.getCategory()));
            if (req.getLevel() != null) {
                sensitiveWord.setLevel(req.getLevel());
            }
            if (req.getStatus() != null) {
                sensitiveWord.setStatus(req.getStatus());
            }
            sensitiveWordMapper.updateById(sensitiveWord);
        } else {
            long dupCount = sensitiveWordMapper.countByWordIncludeDeleted(word, null);
            if (dupCount > 0) {
                throw new BizException(ResultCode.PARAM_ERROR, "敏感词已存在");
            }
            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setCategory(StrUtil.nullToEmpty(req.getCategory()));
            sensitiveWord.setLevel(req.getLevel() == null ? 1 : req.getLevel());
            sensitiveWord.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            sensitiveWordMapper.insert(sensitiveWord);
        }
        // 刷新内存词库
        sensitiveWordService.reload();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(AdminSensitiveWordDeleteReq req) {
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(req.getWordId());
        if (sensitiveWord == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        // 逻辑删除
        sensitiveWordMapper.deleteById(req.getWordId());
        // 刷新内存词库
        sensitiveWordService.reload();
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }
}
