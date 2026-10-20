package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.DynamicListReq;
import com.soul.social.dto.req.DynamicPublishReq;
import com.soul.social.dto.resp.CommentItemResp;
import com.soul.social.dto.resp.DynamicDetailResp;
import com.soul.social.dto.resp.DynamicItemResp;
import com.soul.social.dto.resp.TopicItemResp;
import com.soul.social.entity.DynamicCollect;
import com.soul.social.entity.DynamicComment;
import com.soul.social.entity.DynamicContent;
import com.soul.social.entity.DynamicLike;
import com.soul.social.entity.DynamicMedia;
import com.soul.social.entity.DynamicTopic;
import com.soul.social.entity.DynamicTopicRelation;
import com.soul.social.entity.UserBlacklist;
import com.soul.social.entity.UserPrivacySetting;
import com.soul.social.entity.UserProfile;
import com.soul.social.guard.ContentGuardService;
import com.soul.social.mapper.DynamicCollectMapper;
import com.soul.social.mapper.DynamicCommentMapper;
import com.soul.social.mapper.DynamicContentMapper;
import com.soul.social.mapper.DynamicLikeMapper;
import com.soul.social.mapper.DynamicMediaMapper;
import com.soul.social.mapper.DynamicTopicMapper;
import com.soul.social.mapper.DynamicTopicRelationMapper;
import com.soul.social.mapper.UserBlacklistMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserPrivacySettingMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.DynamicService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 广场动态服务实现。
 */
@Slf4j
@Service
public class DynamicServiceImpl implements DynamicService {

    /** 单条动态图片上限 */
    private static final int MAX_MEDIA = 9;

    /** 自拟话题名长度上限 */
    private static final int MAX_TOPIC_NAME_LEN = 20;

    @Resource
    private DynamicContentMapper dynamicContentMapper;

    @Resource
    private DynamicMediaMapper dynamicMediaMapper;

    @Resource
    private DynamicTopicMapper dynamicTopicMapper;

    @Resource
    private DynamicTopicRelationMapper dynamicTopicRelationMapper;

    @Resource
    private DynamicLikeMapper dynamicLikeMapper;

    @Resource
    private DynamicCollectMapper dynamicCollectMapper;

    @Resource
    private DynamicCommentMapper dynamicCommentMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserBlacklistMapper userBlacklistMapper;

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ContentGuardService contentGuardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(DynamicPublishReq req) {
        Long userId = UserContext.requireUserId();

        boolean hasContent = StrUtil.isNotBlank(req.getContent());
        boolean hasMedia = req.getMediaUrls() != null && !req.getMediaUrls().isEmpty();
        // 正文与图片不可同时为空 → 4004
        if (!hasContent && !hasMedia) {
            throw new BizException(ResultCode.CONTENT_EMPTY);
        }
        if (req.getMediaUrls() != null && req.getMediaUrls().size() > MAX_MEDIA) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "图片不能超过 " + MAX_MEDIA + " 张");
        }

        // R002 禁言
        contentGuardService.checkMute(userId);
        // R010 敏感词
        contentGuardService.checkSensitive(req.getContent());

        // R003 风控观察 → 待审核
        int auditStatus = contentGuardService.needAudit(userId);

        // 取发布者城市（同城频道用）
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("LIMIT 1"));
        String city = profile == null ? "" : StrUtil.nullToEmpty(profile.getCity());

        DynamicContent content = new DynamicContent();
        content.setUserId(userId);
        content.setContent(StrUtil.nullToEmpty(req.getContent()));
        content.setStatus(1);
        content.setAuditStatus(auditStatus);
        content.setAnonymousFlag(Boolean.TRUE.equals(req.getAnonymousFlag()) ? 1 : 0);
        content.setVisibleScope(req.getVisibleScope() == null ? 1 : req.getVisibleScope());
        content.setLikeCount(0);
        content.setCommentCount(0);
        content.setCollectCount(0);
        content.setReportCount(0);
        content.setCity(city);
        dynamicContentMapper.insert(content);
        Long dynamicId = content.getId();

        // 图片
        if (hasMedia) {
            int sort = 0;
            for (String url : req.getMediaUrls()) {
                DynamicMedia media = new DynamicMedia();
                media.setDynamicId(dynamicId);
                media.setMediaType(1);
                media.setMediaUrl(url);
                media.setSort(sort++);
                dynamicMediaMapper.insert(media);
            }
        }

        // 话题
        if (req.getTopicId() != null) {
            DynamicTopic topic = dynamicTopicMapper.selectById(req.getTopicId());
            if (topic == null || topic.getStatus() == null || topic.getStatus() != CommonConstant.STATUS_ENABLE) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "话题不存在或未启用");
            }
            DynamicTopicRelation relation = new DynamicTopicRelation();
            relation.setDynamicId(dynamicId);
            relation.setTopicId(req.getTopicId());
            dynamicTopicRelationMapper.insert(relation);
        }

        log.info("用户 {} 发布动态 {}，审核状态 {}", userId, dynamicId, auditStatus);
        return dynamicId;
    }

    @Override
    public PageResult<DynamicItemResp> list(DynamicListReq req) {
        Long currentUserId = UserContext.requireUserId();
        Page<DynamicContent> page = new Page<>(req.safePageNum(), req.safePageSize());

        // 计算「我 + 我拉黑的人 + 拉黑我的人」，用于过滤
        Set<Long> blockedIds = getRelatedBlacklistIds(currentUserId);

        LambdaQueryWrapper<DynamicContent> wrapper = new LambdaQueryWrapper<DynamicContent>()
                .eq(DynamicContent::getStatus, 1)
                // 他人的动态：仅公开且已过审；自己的动态：不受可见范围 / 审核状态限制
                // （与 detail() 一致——作者总能看见自己的动态，避免「刚发布却在广场找不到」）
                .and(w -> w
                        .eq(DynamicContent::getUserId, currentUserId)
                        .or(x -> x.eq(DynamicContent::getAuditStatus, 0)
                                .eq(DynamicContent::getVisibleScope, 1)))
                .notIn(!blockedIds.isEmpty(), DynamicContent::getUserId, blockedIds);

        if (req.getTopicId() != null) {
            List<Long> dynamicIds = dynamicTopicRelationMapper.selectList(
                            new LambdaQueryWrapper<DynamicTopicRelation>()
                                    .eq(DynamicTopicRelation::getTopicId, req.getTopicId()))
                    .stream().map(DynamicTopicRelation::getDynamicId).toList();
            if (dynamicIds.isEmpty()) {
                return PageResult.empty(req.safePageNum(), req.safePageSize());
            }
            wrapper.in(DynamicContent::getId, dynamicIds);
        }

        String channel = StrUtil.blankToDefault(req.getChannel(), "recommend");
        switch (channel) {
            case "latest" -> wrapper.orderByDesc(DynamicContent::getCreateTime);
            case "city" -> {
                UserProfile me = userProfileMapper.selectOne(
                        new LambdaQueryWrapper<UserProfile>()
                                .eq(UserProfile::getUserId, currentUserId)
                                .last("LIMIT 1"));
                String myCity = me == null ? "" : StrUtil.nullToEmpty(me.getCity());
                wrapper.eq(DynamicContent::getCity, myCity)
                        .orderByDesc(DynamicContent::getCreateTime);
            }
            // recommend：按 点赞数 + 评论数 热度降序
            default -> wrapper.orderByDesc(DynamicContent::getLikeCount)
                    .orderByDesc(DynamicContent::getCommentCount)
                    .orderByDesc(DynamicContent::getCreateTime);
        }

        Page<DynamicContent> result = dynamicContentMapper.selectPage(page, wrapper);
        List<DynamicItemResp> items = buildItems(result.getRecords(), currentUserId);
        return PageResult.of(result, items);
    }

    @Override
    public DynamicDetailResp detail(Long id) {
        Long currentUserId = UserContext.requireUserId();
        DynamicContent content = dynamicContentMapper.selectById(id);
        if (content == null) {
            throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
        }
        boolean isAuthor = content.getUserId().equals(currentUserId);
        // 下架/待审（非本人）不可见
        if (!isAuthor) {
            if (content.getStatus() == null || content.getStatus() != 1
                    || content.getAuditStatus() == null || content.getAuditStatus() != 0) {
                throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
            }
            // 拉黑校验
            contentGuardService.checkBlacklist(currentUserId, content.getUserId());
        }

        DynamicDetailResp resp = new DynamicDetailResp();
        resp.setDynamicId(content.getId());
        resp.setContent(content.getContent());
        resp.setLikeCount(nz(content.getLikeCount()));
        resp.setCommentCount(nz(content.getCommentCount()));
        resp.setCollectCount(nz(content.getCollectCount()));
        resp.setAuditStatus(content.getAuditStatus());
        resp.setVisibleScope(content.getVisibleScope());
        resp.setCreateTime(content.getCreateTime());
        resp.setIsAuthor(isAuthor);
        resp.setCanDelete(isAuthor);

        // 作者信息
        resp.setAuthor(buildAuthor(content.getUserId(), content.getAnonymousFlag()));

        // 图片
        resp.setMediaUrls(loadMediaUrls(List.of(id)).getOrDefault(id, List.of()));

        // 话题名
        resp.setTopicName(loadTopicName(id));

        // 我的点赞/收藏状态
        resp.setLiked(hasLiked(id, currentUserId));
        resp.setCollected(hasCollected(id, currentUserId));

        // 评论（前 50 条）
        List<DynamicComment> comments = dynamicCommentMapper.selectList(
                new LambdaQueryWrapper<DynamicComment>()
                        .eq(DynamicComment::getDynamicId, id)
                        .eq(DynamicComment::getStatus, 1)
                        .orderByDesc(DynamicComment::getCreateTime)
                        .last("LIMIT 50"));
        resp.setComments(buildComments(comments));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        DynamicContent content = dynamicContentMapper.selectById(id);
        if (content == null) {
            throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
        }
        if (!content.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NO_PERMISSION_DELETE);
        }
        dynamicContentMapper.deleteById(id);
    }

    @Override
    public List<TopicItemResp> listTopics() {
        List<DynamicTopic> topics = dynamicTopicMapper.selectList(
                new LambdaQueryWrapper<DynamicTopic>()
                        .eq(DynamicTopic::getStatus, CommonConstant.STATUS_ENABLE)
                        .orderByDesc(DynamicTopic::getHotValue)
                        .orderByAsc(DynamicTopic::getSort));
        List<TopicItemResp> result = new ArrayList<>();
        for (DynamicTopic topic : topics) {
            result.add(toTopicItem(topic));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicItemResp createTopic(String name) {
        Long userId = UserContext.requireUserId();

        // 规范化：去首尾空白、去掉前导 # 号
        String normalized = StrUtil.nullToEmpty(name).trim();
        while (normalized.startsWith("#")) {
            normalized = normalized.substring(1).trim();
        }
        if (StrUtil.isBlank(normalized)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "话题名不能为空");
        }
        if (normalized.length() > MAX_TOPIC_NAME_LEN) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "话题名不能超过 " + MAX_TOPIC_NAME_LEN + " 个字");
        }

        // 与发布动态同等的治理校验
        contentGuardService.checkMute(userId);
        contentGuardService.checkSensitive(normalized);

        // 同名启用话题已存在 → 复用，避免建重复话题
        DynamicTopic exist = dynamicTopicMapper.selectOne(
                new LambdaQueryWrapper<DynamicTopic>()
                        .eq(DynamicTopic::getName, normalized)
                        .eq(DynamicTopic::getStatus, CommonConstant.STATUS_ENABLE)
                        .last("LIMIT 1"));
        if (exist != null) {
            return toTopicItem(exist);
        }

        DynamicTopic topic = new DynamicTopic();
        topic.setName(normalized);
        topic.setDescription("");
        topic.setCover("");
        topic.setHotValue(0);
        topic.setStatus(CommonConstant.STATUS_ENABLE);
        // 追加到末尾（sort 递增），热门排序仍由 hot_value 决定
        topic.setSort(nextTopicSort());
        dynamicTopicMapper.insert(topic);
        log.info("用户 {} 自建话题 {} : {}", userId, topic.getId(), normalized);
        return toTopicItem(topic);
    }

    /** 取当前最大 sort + 1，保证新话题排序稳定且排在已有话题之后。 */
    private int nextTopicSort() {
        DynamicTopic last = dynamicTopicMapper.selectOne(
                new LambdaQueryWrapper<DynamicTopic>()
                        .orderByDesc(DynamicTopic::getSort)
                        .last("LIMIT 1"));
        return (last == null || last.getSort() == null) ? 1 : last.getSort() + 1;
    }

    private TopicItemResp toTopicItem(DynamicTopic topic) {
        TopicItemResp item = new TopicItemResp();
        item.setTopicId(topic.getId());
        item.setName(topic.getName());
        item.setDescription(topic.getDescription());
        item.setCover(topic.getCover());
        item.setHotValue(topic.getHotValue());
        return item;
    }

    // ---------------- 私有辅助 ----------------

    private List<DynamicItemResp> buildItems(List<DynamicContent> records, Long currentUserId) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> dynamicIds = records.stream().map(DynamicContent::getId).toList();
        List<Long> authorIds = records.stream().map(DynamicContent::getUserId).distinct().toList();

        Map<Long, List<String>> mediaMap = loadMediaUrls(dynamicIds);
        Map<Long, String> topicMap = loadTopicNames(dynamicIds);
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(
                        new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, authorIds))
                .stream().collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));

        // 我的点赞 / 收藏
        Set<Long> likedIds = dynamicLikeMapper.selectList(
                        new LambdaQueryWrapper<DynamicLike>()
                                .eq(DynamicLike::getUserId, currentUserId)
                                .eq(DynamicLike::getStatus, 1)
                                .in(DynamicLike::getDynamicId, dynamicIds))
                .stream().map(DynamicLike::getDynamicId).collect(Collectors.toSet());
        Set<Long> collectedIds = dynamicCollectMapper.selectList(
                        new LambdaQueryWrapper<DynamicCollect>()
                                .eq(DynamicCollect::getUserId, currentUserId)
                                .eq(DynamicCollect::getStatus, 1)
                                .in(DynamicCollect::getDynamicId, dynamicIds))
                .stream().map(DynamicCollect::getDynamicId).collect(Collectors.toSet());

        List<DynamicItemResp> items = new ArrayList<>();
        for (DynamicContent content : records) {
            DynamicItemResp item = new DynamicItemResp();
            item.setDynamicId(content.getId());
            item.setContent(content.getContent());
            item.setMediaUrls(mediaMap.getOrDefault(content.getId(), List.of()));
            item.setTopicName(topicMap.getOrDefault(content.getId(), ""));
            item.setLikeCount(nz(content.getLikeCount()));
            item.setCommentCount(nz(content.getCommentCount()));
            item.setCollectCount(nz(content.getCollectCount()));
            item.setLiked(likedIds.contains(content.getId()));
            item.setCollected(collectedIds.contains(content.getId()));
            item.setAuditStatus(content.getAuditStatus());
            item.setCreateTime(content.getCreateTime());

            // 匿名处理
            if (content.getAnonymousFlag() != null && content.getAnonymousFlag() == 1
                    && !content.getUserId().equals(currentUserId)) {
                item.setAuthor(new DynamicItemResp.AuthorItem(0L, "匿名用户", ""));
            } else {
                UserProfile profile = profileMap.get(content.getUserId());
                item.setAuthor(new DynamicItemResp.AuthorItem(
                        content.getUserId(),
                        profile == null ? "" : profile.getNickname(),
                        profile == null ? "" : profile.getAvatar()));
            }
            items.add(item);
        }
        return items;
    }

    private DynamicItemResp.AuthorItem buildAuthor(Long userId, Integer anonymousFlag) {
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("LIMIT 1"));
        return new DynamicItemResp.AuthorItem(
                userId,
                profile == null ? "" : profile.getNickname(),
                profile == null ? "" : profile.getAvatar());
    }

    private Map<Long, List<String>> loadMediaUrls(List<Long> dynamicIds) {
        if (dynamicIds.isEmpty()) {
            return Map.of();
        }
        List<DynamicMedia> mediaList = dynamicMediaMapper.selectList(
                new LambdaQueryWrapper<DynamicMedia>()
                        .in(DynamicMedia::getDynamicId, dynamicIds)
                        .orderByAsc(DynamicMedia::getSort));
        return mediaList.stream().collect(Collectors.groupingBy(
                DynamicMedia::getDynamicId,
                Collectors.mapping(DynamicMedia::getMediaUrl, Collectors.toList())));
    }

    private String loadTopicName(Long dynamicId) {
        DynamicTopicRelation relation = dynamicTopicRelationMapper.selectOne(
                new LambdaQueryWrapper<DynamicTopicRelation>()
                        .eq(DynamicTopicRelation::getDynamicId, dynamicId)
                        .last("LIMIT 1"));
        if (relation == null) {
            return "";
        }
        DynamicTopic topic = dynamicTopicMapper.selectById(relation.getTopicId());
        return topic == null ? "" : topic.getName();
    }

    private Map<Long, String> loadTopicNames(List<Long> dynamicIds) {
        if (dynamicIds.isEmpty()) {
            return Map.of();
        }
        List<DynamicTopicRelation> relations = dynamicTopicRelationMapper.selectList(
                new LambdaQueryWrapper<DynamicTopicRelation>()
                        .in(DynamicTopicRelation::getDynamicId, dynamicIds));
        if (relations.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> dynamicToTopic = relations.stream().collect(Collectors.toMap(
                DynamicTopicRelation::getDynamicId, DynamicTopicRelation::getTopicId, (a, b) -> a));
        List<Long> topicIds = dynamicToTopic.values().stream().distinct().toList();
        Map<Long, String> topicNameMap = dynamicTopicMapper.selectBatchIds(topicIds).stream()
                .collect(Collectors.toMap(DynamicTopic::getId, DynamicTopic::getName, (a, b) -> a));
        Map<Long, String> result = new java.util.HashMap<>();
        dynamicToTopic.forEach((dynId, topicId) ->
                result.put(dynId, topicNameMap.getOrDefault(topicId, "")));
        return result;
    }

    private List<CommentItemResp> buildComments(List<DynamicComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = new ArrayList<>();
        for (DynamicComment c : comments) {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null && c.getReplyUserId() > 0) {
                userIds.add(c.getReplyUserId());
            }
        }
        Map<Long, UserProfile> profileMap = userIds.isEmpty() ? Map.of()
                : userProfileMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));

        List<CommentItemResp> items = new ArrayList<>();
        for (DynamicComment c : comments) {
            CommentItemResp item = new CommentItemResp();
            item.setCommentId(c.getId());
            item.setContent(c.getContent());
            item.setParentId(c.getParentId());
            item.setCreateTime(c.getCreateTime());
            UserProfile u = profileMap.get(c.getUserId());
            item.setUser(new DynamicItemResp.AuthorItem(c.getUserId(),
                    u == null ? "" : u.getNickname(), u == null ? "" : u.getAvatar()));
            if (c.getReplyUserId() != null && c.getReplyUserId() > 0) {
                UserProfile ru = profileMap.get(c.getReplyUserId());
                item.setReplyUser(new CommentItemResp.ReplyUser(c.getReplyUserId(),
                        ru == null ? "" : ru.getNickname()));
            }
            items.add(item);
        }
        return items;
    }

    private boolean hasLiked(Long dynamicId, Long userId) {
        Long count = dynamicLikeMapper.selectCount(
                new LambdaQueryWrapper<DynamicLike>()
                        .eq(DynamicLike::getDynamicId, dynamicId)
                        .eq(DynamicLike::getUserId, userId)
                        .eq(DynamicLike::getStatus, 1));
        return count != null && count > 0;
    }

    private boolean hasCollected(Long dynamicId, Long userId) {
        Long count = dynamicCollectMapper.selectCount(
                new LambdaQueryWrapper<DynamicCollect>()
                        .eq(DynamicCollect::getDynamicId, dynamicId)
                        .eq(DynamicCollect::getUserId, userId)
                        .eq(DynamicCollect::getStatus, 1));
        return count != null && count > 0;
    }

    /**
     * 取与当前用户存在拉黑关系的所有用户 id（我拉黑的 + 拉黑我的），用于列表过滤。
     */
    private Set<Long> getRelatedBlacklistIds(Long currentUserId) {
        List<UserBlacklist> list = userBlacklistMapper.selectList(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getStatus, CommonConstant.YES)
                        .and(w -> w.eq(UserBlacklist::getUserId, currentUserId)
                                .or().eq(UserBlacklist::getTargetUserId, currentUserId)));
        Set<Long> ids = new java.util.HashSet<>();
        for (UserBlacklist bl : list) {
            if (currentUserId.equals(bl.getUserId())) {
                ids.add(bl.getTargetUserId());
            } else {
                ids.add(bl.getUserId());
            }
        }
        return ids;
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
