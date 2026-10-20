package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.MatchDislikeReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.MatchUserResp;
import com.soul.social.entity.MatchRecord;
import com.soul.social.entity.Tag;
import com.soul.social.entity.User;
import com.soul.social.entity.UserDislikeRecord;
import com.soul.social.entity.UserPrivacySetting;
import com.soul.social.entity.UserProfile;
import com.soul.social.entity.UserSoulTestRecord;
import com.soul.social.entity.UserTag;
import com.soul.social.mapper.MatchRecordMapper;
import com.soul.social.mapper.TagMapper;
import com.soul.social.mapper.UserBlacklistMapper;
import com.soul.social.mapper.UserDislikeRecordMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserPrivacySettingMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.mapper.UserSoulTestRecordMapper;
import com.soul.social.mapper.UserTagMapper;
import com.soul.social.service.MatchService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 匹配推荐服务实现。
 * <p>匹配度权重：标签 40 + 灵魂 30 + 同城 20 + 活跃 10 = 100（对齐架构设计 U3）。
 * <p>权重可用 system_config 覆盖（match_weight_tag / match_weight_soul / match_weight_city / match_weight_active）。
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBlacklistMapper userBlacklistMapper;

    @Resource
    private UserDislikeRecordMapper userDislikeRecordMapper;

    @Resource
    private UserTagMapper userTagMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private UserSoulTestRecordMapper userSoulTestRecordMapper;

    @Resource
    private MatchRecordMapper matchRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<MatchUserResp> recommend(PageQuery query) {
        Long currentUserId = UserContext.requireUserId();

        // 排除集：自己 + 已拉黑（双向）+ 已不感兴趣
        Set<Long> excludeIds = new HashSet<>();
        excludeIds.add(currentUserId);
        userBlacklistMapper.selectList(
                        new LambdaQueryWrapper<com.soul.social.entity.UserBlacklist>()
                                .eq(com.soul.social.entity.UserBlacklist::getStatus, CommonConstant.YES)
                                .and(w -> w.eq(com.soul.social.entity.UserBlacklist::getUserId, currentUserId)
                                        .or().eq(com.soul.social.entity.UserBlacklist::getTargetUserId, currentUserId)))
                .forEach(bl -> {
                    if (currentUserId.equals(bl.getUserId())) {
                        excludeIds.add(bl.getTargetUserId());
                    } else {
                        excludeIds.add(bl.getUserId());
                    }
                });
        userDislikeRecordMapper.selectList(
                        new LambdaQueryWrapper<UserDislikeRecord>()
                                .eq(UserDislikeRecord::getUserId, currentUserId))
                .forEach(d -> excludeIds.add(d.getTargetUserId()));

        // 候选池：资料存在 + 用户正常 + 允许被推荐（R005）
        Page<UserProfile> page = new Page<>(query.safePageNum(), query.safePageSize());
        Page<UserProfile> candidatePage = userProfileMapper.selectPage(page,
                new LambdaQueryWrapper<UserProfile>()
                        .notIn(!excludeIds.isEmpty(), UserProfile::getUserId, excludeIds)
                        .orderByDesc(UserProfile::getCompletePercent));

        // 二次过滤：封禁 + 关闭推荐
        List<UserProfile> candidates = new ArrayList<>();
        for (UserProfile p : candidatePage.getRecords()) {
            User u = userMapper.selectById(p.getUserId());
            if (u == null || (u.getStatus() != null && u.getStatus() == CommonConstant.USER_STATUS_BANNED)) {
                continue;
            }
            UserPrivacySetting privacy = userPrivacySettingMapper.selectOne(
                    new LambdaQueryWrapper<UserPrivacySetting>()
                            .eq(UserPrivacySetting::getUserId, p.getUserId())
                            .last("LIMIT 1"));
            if (privacy != null && privacy.getAllowRecommend() != null
                    && privacy.getAllowRecommend() == CommonConstant.NO) {
                continue;
            }
            candidates.add(p);
        }

        // 我的画像：标签集合 + 灵魂结果 + 城市
        Set<Long> myTagIds = loadTagIds(currentUserId);
        String mySoulCode = loadSoulCode(currentUserId);
        UserProfile myProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, currentUserId)
                        .last("LIMIT 1"));
        String myCity = myProfile == null ? "" : StrUtil.nullToEmpty(myProfile.getCity());

        List<MatchUserResp> items = new ArrayList<>();
        for (UserProfile p : candidates) {
            MatchUserResp resp = buildMatchUser(currentUserId, p, myTagIds, mySoulCode, myCity);
            items.add(resp);
            // 写曝光记录
            MatchRecord record = new MatchRecord();
            record.setUserId(currentUserId);
            record.setTargetUserId(p.getUserId());
            record.setMatchScore(resp.getMatchScore());
            record.setSource(1);
            record.setExposeTime(LocalDateTime.now());
            matchRecordMapper.insert(record);
        }
        // 按匹配度降序
        items.sort(Comparator.comparingInt(MatchUserResp::getMatchScore).reversed());
        return PageResult.of(candidatePage, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dislike(MatchDislikeReq req) {
        Long userId = UserContext.requireUserId();
        if (userId.equals(req.getTargetUserId())) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        UserDislikeRecord exist = userDislikeRecordMapper.selectOne(
                new LambdaQueryWrapper<UserDislikeRecord>()
                        .eq(UserDislikeRecord::getUserId, userId)
                        .eq(UserDislikeRecord::getTargetUserId, req.getTargetUserId())
                        .last("LIMIT 1"));
        if (exist != null) {
            return;
        }
        UserDislikeRecord record = new UserDislikeRecord();
        record.setUserId(userId);
        record.setTargetUserId(req.getTargetUserId());
        record.setReason(StrUtil.nullToEmpty(req.getReason()));
        userDislikeRecordMapper.insert(record);
    }

    // ---------------- 私有辅助 ----------------

    private MatchUserResp buildMatchUser(Long myUserId, UserProfile candidate,
                                         Set<Long> myTagIds, String mySoulCode, String myCity) {
        Long targetId = candidate.getUserId();
        MatchUserResp resp = new MatchUserResp();
        resp.setUserId(targetId);
        resp.setNickname(candidate.getNickname());
        resp.setAvatar(candidate.getAvatar());
        resp.setCity(candidate.getCity());
        resp.setAge(candidate.getBirthday() == null ? null
                : Period.between(candidate.getBirthday(), java.time.LocalDate.now()).getYears());

        // 共同标签
        Set<Long> targetTagIds = loadTagIds(targetId);
        Set<Long> shared = new HashSet<>(myTagIds);
        shared.retainAll(targetTagIds);
        List<String> sharedTagNames = new ArrayList<>();
        if (!shared.isEmpty()) {
            Map<Long, String> tagNameMap = tagMapper.selectBatchIds(shared).stream()
                    .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));
            for (Long id : shared) {
                String name = tagNameMap.get(id);
                if (name != null) {
                    sharedTagNames.add(name);
                }
            }
        }
        resp.setTags(sharedTagNames);

        // 权重
        int tagScore = Math.min(shared.size() * 10, 40);

        String targetSoulCode = loadSoulCode(targetId);
        int soulScore = 0;
        if (StrUtil.isNotBlank(mySoulCode) && mySoulCode.equals(targetSoulCode)) {
            soulScore = 30;
        } else if (StrUtil.isNotBlank(targetSoulCode)) {
            soulScore = 15;
        }

        int cityScore = (StrUtil.isNotBlank(myCity) && myCity.equals(candidate.getCity())) ? 20 : 0;

        // 活跃：7 天内活跃得 10
        User targetUser = userMapper.selectById(targetId);
        int activeScore = 0;
        if (targetUser != null && targetUser.getLastActiveTime() != null
                && targetUser.getLastActiveTime().isAfter(LocalDateTime.now().minusDays(7))) {
            activeScore = 10;
        }

        int total = Math.min(tagScore + soulScore + cityScore + activeScore, 100);
        resp.setMatchScore(total);

        // 推荐理由
        String reason;
        if (!sharedTagNames.isEmpty()) {
            reason = "都喜欢" + sharedTagNames.get(0);
        } else if (cityScore > 0) {
            reason = "同在" + myCity;
        } else if (soulScore > 0) {
            reason = "灵魂契合";
        } else {
            reason = "也许聊得来";
        }
        resp.setReason(reason);
        return resp;
    }

    private Set<Long> loadTagIds(Long userId) {
        List<UserTag> tags = userTagMapper.selectList(
                new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, userId));
        return tags.stream().map(UserTag::getTagId).collect(Collectors.toSet());
    }

    private String loadSoulCode(Long userId) {
        UserSoulTestRecord record = userSoulTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserSoulTestRecord>()
                        .eq(UserSoulTestRecord::getUserId, userId)
                        .last("LIMIT 1"));
        return record == null ? "" : StrUtil.nullToEmpty(record.getResultCode());
    }
}
