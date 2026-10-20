package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.UserTagSaveReq;
import com.soul.social.dto.resp.TagListResp;
import com.soul.social.entity.Tag;
import com.soul.social.entity.TagCategory;
import com.soul.social.entity.User;
import com.soul.social.entity.UserTag;
import com.soul.social.mapper.TagCategoryMapper;
import com.soul.social.mapper.TagMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserTagMapper;
import com.soul.social.service.TagService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标签服务实现（用户端）。
 */
@Slf4j
@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagCategoryMapper tagCategoryMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private UserTagMapper userTagMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<TagListResp.CategoryItem> listTags() {
        // 仅启用分类，按 sort
        List<TagCategory> categories = tagCategoryMapper.selectList(
                new LambdaQueryWrapper<TagCategory>()
                        .eq(TagCategory::getStatus, CommonConstant.STATUS_ENABLE)
                        .orderByAsc(TagCategory::getSort));

        // 仅启用标签，按 sort
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getStatus, CommonConstant.STATUS_ENABLE)
                        .orderByAsc(Tag::getSort));

        Map<Long, List<Tag>> tagsByCategory = tags.stream()
                .collect(Collectors.groupingBy(Tag::getCategoryId));

        List<TagListResp.CategoryItem> items = new ArrayList<>();
        for (TagCategory category : categories) {
            TagListResp.CategoryItem item = new TagListResp.CategoryItem();
            item.setCategoryId(category.getId());
            item.setName(category.getName());
            item.setIcon(category.getIcon());
            List<TagListResp.TagItem> tagItems = new ArrayList<>();
            for (Tag tag : tagsByCategory.getOrDefault(category.getId(), List.of())) {
                TagListResp.TagItem ti = new TagListResp.TagItem();
                ti.setTagId(tag.getId());
                ti.setName(tag.getName());
                ti.setHot(tag.getHotFlag() != null && tag.getHotFlag() == CommonConstant.YES);
                tagItems.add(ti);
            }
            item.setTags(tagItems);
            items.add(item);
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserTags(UserTagSaveReq req) {
        Long userId = UserContext.requireUserId();
        // 数量校验 3-10
        req.validateCount();
        List<Long> tagIds = req.distinctTagIds();
        if (tagIds.size() < 3 || tagIds.size() > 10) {
            throw new BizException(ResultCode.TAG_COUNT_INVALID);
        }

        // 校验标签存在且启用
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BizException(ResultCode.TAG_UNAVAILABLE);
        }
        for (Tag tag : tags) {
            if (tag.getStatus() == null || tag.getStatus() != CommonConstant.STATUS_ENABLE) {
                throw new BizException(ResultCode.TAG_UNAVAILABLE);
            }
        }

        // 先逻辑删除旧关联，再插入新关联
        userTagMapper.delete(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, userId));
        for (Long tagId : tagIds) {
            UserTag userTag = new UserTag();
            userTag.setUserId(userId);
            userTag.setTagId(tagId);
            userTagMapper.insert(userTag);
        }

        // 置 tag_flag=1
        User update = new User();
        update.setId(userId);
        update.setTagFlag(CommonConstant.YES);
        userMapper.updateById(update);
        log.info("用户 {} 保存标签 {} 个", userId, tagIds.size());
    }
}
