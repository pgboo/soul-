package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminQuestionSaveReq;
import com.soul.social.dto.req.AdminResultSaveReq;
import com.soul.social.dto.req.AdminTagCategorySaveReq;
import com.soul.social.dto.req.AdminTagDeleteReq;
import com.soul.social.dto.req.AdminTagSaveReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.AdminQuestionResp;
import com.soul.social.dto.resp.AdminResultResp;
import com.soul.social.dto.resp.AdminTagCategoryResp;
import com.soul.social.dto.resp.AdminTagResp;
import com.soul.social.entity.SoulTestOption;
import com.soul.social.entity.SoulTestQuestion;
import com.soul.social.entity.SoulTestResult;
import com.soul.social.entity.Tag;
import com.soul.social.entity.TagCategory;
import com.soul.social.mapper.SoulTestOptionMapper;
import com.soul.social.mapper.SoulTestQuestionMapper;
import com.soul.social.mapper.SoulTestResultMapper;
import com.soul.social.mapper.TagCategoryMapper;
import com.soul.social.mapper.TagMapper;
import com.soul.social.service.AdminService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台管理服务实现（标签 + 灵魂测试）。
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private TagCategoryMapper tagCategoryMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private SoulTestQuestionMapper soulTestQuestionMapper;

    @Resource
    private SoulTestOptionMapper soulTestOptionMapper;

    @Resource
    private SoulTestResultMapper soulTestResultMapper;

    // ---------------- 标签分类 ----------------

    @Override
    public List<AdminTagCategoryResp> categoryList() {
        List<TagCategory> list = tagCategoryMapper.selectList(
                new LambdaQueryWrapper<TagCategory>().orderByAsc(TagCategory::getSort));
        List<AdminTagCategoryResp> result = new ArrayList<>();
        for (TagCategory c : list) {
            AdminTagCategoryResp resp = new AdminTagCategoryResp();
            resp.setCategoryId(c.getId());
            resp.setName(c.getName());
            resp.setIcon(c.getIcon());
            resp.setSort(c.getSort());
            resp.setStatus(c.getStatus());
            result.add(resp);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(AdminTagCategorySaveReq req) {
        if (req.getCategoryId() != null) {
            TagCategory category = tagCategoryMapper.selectById(req.getCategoryId());
            if (category == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            category.setName(req.getName());
            category.setIcon(StrUtil.nullToEmpty(req.getIcon()));
            if (req.getSort() != null) {
                category.setSort(req.getSort());
            }
            if (req.getStatus() != null) {
                category.setStatus(req.getStatus());
            }
            tagCategoryMapper.updateById(category);
        } else {
            TagCategory category = new TagCategory();
            category.setName(req.getName());
            category.setIcon(StrUtil.nullToEmpty(req.getIcon()));
            category.setSort(req.getSort() == null ? 0 : req.getSort());
            category.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            tagCategoryMapper.insert(category);
        }
    }

    // ---------------- 标签 ----------------

    @Override
    public PageResult<AdminTagResp> tagPage(Long categoryId, Integer status, PageQuery query) {
        Page<Tag> page = new Page<>(query.safePageNum(), query.safePageSize());
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .eq(categoryId != null, Tag::getCategoryId, categoryId)
                .eq(status != null, Tag::getStatus, status)
                .orderByAsc(Tag::getSort);
        Page<Tag> result = tagMapper.selectPage(page, wrapper);

        List<Long> categoryIds = result.getRecords().stream()
                .map(Tag::getCategoryId).distinct().toList();
        Map<Long, TagCategory> categoryMap = categoryIds.isEmpty() ? Map.of()
                : tagCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(TagCategory::getId, Function.identity(), (a, b) -> a));

        List<AdminTagResp> items = new ArrayList<>();
        for (Tag tag : result.getRecords()) {
            AdminTagResp resp = new AdminTagResp();
            resp.setTagId(tag.getId());
            resp.setCategoryId(tag.getCategoryId());
            TagCategory category = categoryMap.get(tag.getCategoryId());
            resp.setCategoryName(category == null ? "" : category.getName());
            resp.setName(tag.getName());
            resp.setHotFlag(tag.getHotFlag());
            resp.setMatchFlag(tag.getMatchFlag());
            resp.setRecommendFlag(tag.getRecommendFlag());
            resp.setSort(tag.getSort());
            resp.setStatus(tag.getStatus());
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTag(AdminTagSaveReq req) {
        if (req.getTagId() != null) {
            Tag tag = tagMapper.selectById(req.getTagId());
            if (tag == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            tag.setCategoryId(req.getCategoryId());
            tag.setName(req.getName());
            if (req.getHotFlag() != null) {
                tag.setHotFlag(req.getHotFlag());
            }
            if (req.getMatchFlag() != null) {
                tag.setMatchFlag(req.getMatchFlag());
            }
            if (req.getRecommendFlag() != null) {
                tag.setRecommendFlag(req.getRecommendFlag());
            }
            if (req.getSort() != null) {
                tag.setSort(req.getSort());
            }
            if (req.getStatus() != null) {
                tag.setStatus(req.getStatus());
            }
            tagMapper.updateById(tag);
        } else {
            Tag tag = new Tag();
            tag.setCategoryId(req.getCategoryId());
            tag.setName(req.getName());
            tag.setHotFlag(req.getHotFlag() == null ? 0 : req.getHotFlag());
            tag.setMatchFlag(req.getMatchFlag() == null ? 1 : req.getMatchFlag());
            tag.setRecommendFlag(req.getRecommendFlag() == null ? 1 : req.getRecommendFlag());
            tag.setSort(req.getSort() == null ? 0 : req.getSort());
            tag.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            tagMapper.insert(tag);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(AdminTagDeleteReq req) {
        Tag tag = tagMapper.selectById(req.getTagId());
        if (tag == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        // 逻辑删除
        tagMapper.deleteById(req.getTagId());
    }

    // ---------------- 灵魂测试题目 ----------------

    @Override
    public PageResult<AdminQuestionResp> questionPage(Integer status, PageQuery query) {
        Page<SoulTestQuestion> page = new Page<>(query.safePageNum(), query.safePageSize());
        Page<SoulTestQuestion> result = soulTestQuestionMapper.selectPage(page,
                new LambdaQueryWrapper<SoulTestQuestion>()
                        .eq(status != null, SoulTestQuestion::getStatus, status)
                        .orderByAsc(SoulTestQuestion::getSort));

        List<Long> questionIds = result.getRecords().stream().map(SoulTestQuestion::getId).toList();
        Map<Long, List<SoulTestOption>> optionsByQuestion = questionIds.isEmpty() ? Map.of()
                : soulTestOptionMapper.selectList(
                        new LambdaQueryWrapper<SoulTestOption>()
                                .in(SoulTestOption::getQuestionId, questionIds)
                                .orderByAsc(SoulTestOption::getSort))
                .stream().collect(Collectors.groupingBy(SoulTestOption::getQuestionId));

        List<AdminQuestionResp> items = new ArrayList<>();
        for (SoulTestQuestion q : result.getRecords()) {
            AdminQuestionResp resp = new AdminQuestionResp();
            resp.setQuestionId(q.getId());
            resp.setTitle(q.getTitle());
            resp.setDescription(q.getDescription());
            resp.setSort(q.getSort());
            resp.setStatus(q.getStatus());
            List<AdminQuestionResp.OptionItem> optionItems = new ArrayList<>();
            for (SoulTestOption opt : optionsByQuestion.getOrDefault(q.getId(), List.of())) {
                AdminQuestionResp.OptionItem oi = new AdminQuestionResp.OptionItem();
                oi.setOptionId(opt.getId());
                oi.setContent(opt.getContent());
                oi.setResultCode(opt.getResultCode());
                oi.setScore(opt.getScore());
                oi.setSort(opt.getSort());
                optionItems.add(oi);
            }
            resp.setOptions(optionItems);
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestion(AdminQuestionSaveReq req) {
        Long questionId;
        if (req.getQuestionId() != null) {
            SoulTestQuestion question = soulTestQuestionMapper.selectById(req.getQuestionId());
            if (question == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            question.setTitle(req.getTitle());
            question.setDescription(StrUtil.nullToEmpty(req.getDescription()));
            if (req.getSort() != null) {
                question.setSort(req.getSort());
            }
            if (req.getStatus() != null) {
                question.setStatus(req.getStatus());
            }
            soulTestQuestionMapper.updateById(question);
            questionId = question.getId();
        } else {
            SoulTestQuestion question = new SoulTestQuestion();
            question.setTitle(req.getTitle());
            question.setDescription(StrUtil.nullToEmpty(req.getDescription()));
            question.setSort(req.getSort() == null ? 0 : req.getSort());
            question.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            soulTestQuestionMapper.insert(question);
            questionId = question.getId();
        }

        // 选项：先删后插
        soulTestOptionMapper.delete(
                new LambdaQueryWrapper<SoulTestOption>().eq(SoulTestOption::getQuestionId, questionId));
        if (req.getOptions() != null) {
            for (AdminQuestionSaveReq.OptionItem item : req.getOptions()) {
                SoulTestOption option = new SoulTestOption();
                option.setQuestionId(questionId);
                option.setContent(StrUtil.nullToEmpty(item.getContent()));
                option.setResultCode(StrUtil.nullToEmpty(item.getResultCode()));
                option.setScore(item.getScore() == null ? 0 : item.getScore());
                option.setSort(item.getSort() == null ? 0 : item.getSort());
                soulTestOptionMapper.insert(option);
            }
        }
    }

    // ---------------- 灵魂测试结果 ----------------

    @Override
    public List<AdminResultResp> resultList() {
        List<SoulTestResult> list = soulTestResultMapper.selectList(
                new LambdaQueryWrapper<SoulTestResult>().orderByAsc(SoulTestResult::getId));
        List<AdminResultResp> result = new ArrayList<>();
        for (SoulTestResult r : list) {
            AdminResultResp resp = new AdminResultResp();
            resp.setResultId(r.getId());
            resp.setResultCode(r.getResultCode());
            resp.setName(r.getName());
            resp.setIcon(r.getIcon());
            resp.setDescription(r.getDescription());
            resp.setSocialAdvice(r.getSocialAdvice());
            result.add(resp);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveResult(AdminResultSaveReq req) {
        if (req.getResultId() != null) {
            SoulTestResult result = soulTestResultMapper.selectById(req.getResultId());
            if (result == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            result.setResultCode(req.getResultCode());
            result.setName(req.getName());
            result.setIcon(StrUtil.nullToEmpty(req.getIcon()));
            result.setDescription(StrUtil.nullToEmpty(req.getDescription()));
            result.setSocialAdvice(StrUtil.nullToEmpty(req.getSocialAdvice()));
            soulTestResultMapper.updateById(result);
        } else {
            SoulTestResult result = new SoulTestResult();
            result.setResultCode(req.getResultCode());
            result.setName(req.getName());
            result.setIcon(StrUtil.nullToEmpty(req.getIcon()));
            result.setDescription(StrUtil.nullToEmpty(req.getDescription()));
            result.setSocialAdvice(StrUtil.nullToEmpty(req.getSocialAdvice()));
            soulTestResultMapper.insert(result);
        }
    }
}
