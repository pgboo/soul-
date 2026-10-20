package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.client.SensitiveWordService;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.context.UserContext;
import com.soul.social.converter.UserConverter;
import com.soul.social.dto.req.ProfileCompleteReq;
import com.soul.social.dto.req.ProfileUpdateReq;
import com.soul.social.dto.resp.UserHomeResp;
import com.soul.social.dto.resp.UserInitStatusResp;
import com.soul.social.dto.resp.UserProfileResp;
import com.soul.social.entity.DynamicContent;
import com.soul.social.entity.GreetRecord;
import com.soul.social.entity.SoulTestResult;
import com.soul.social.entity.Tag;
import com.soul.social.entity.User;
import com.soul.social.entity.UserBlacklist;
import com.soul.social.entity.UserPrivacySetting;
import com.soul.social.entity.UserProfile;
import com.soul.social.entity.UserSoulTestRecord;
import com.soul.social.entity.UserTag;
import com.soul.social.mapper.DynamicContentMapper;
import com.soul.social.mapper.GreetRecordMapper;
import com.soul.social.mapper.SoulTestResultMapper;
import com.soul.social.mapper.TagMapper;
import com.soul.social.mapper.UserBlacklistMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserPrivacySettingMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.mapper.UserSoulTestRecordMapper;
import com.soul.social.mapper.UserTagMapper;
import com.soul.social.service.UserService;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户资料 / 初始化状态实现。
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 昵称最大修改次数 */
    private static final int NICKNAME_MAX_MODIFY = 3;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private UserTagMapper userTagMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private UserSoulTestRecordMapper userSoulTestRecordMapper;

    @Resource
    private SoulTestResultMapper soulTestResultMapper;

    @Resource
    private DynamicContentMapper dynamicContentMapper;

    @Resource
    private UserBlacklistMapper userBlacklistMapper;

    @Resource
    private GreetRecordMapper greetRecordMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Override
    public UserInitStatusResp initStatus() {
        Long userId = UserContext.requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        boolean profile = isYes(user.getProfileCompleteFlag());
        boolean tag = isYes(user.getTagFlag());
        boolean test = isYes(user.getTestFlag());

        UserInitStatusResp resp = new UserInitStatusResp();
        resp.setProfileComplete(profile);
        resp.setTagComplete(tag);
        resp.setTestComplete(test);
        resp.setAllCompleted(profile && tag && test);
        // 分流：profile → tag → test → square
        if (!profile) {
            resp.setNextStep("profile");
        } else if (!tag) {
            resp.setNextStep("tag");
        } else if (!test) {
            resp.setNextStep("test");
        } else {
            resp.setNextStep("square");
        }
        return resp;
    }

    @Override
    public UserProfileResp getMyProfile() {
        Long userId = UserContext.requireUserId();
        UserProfile profile = getOrCreateProfile(userId);
        return UserConverter.toProfileResp(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResp completeProfile(ProfileCompleteReq req) {
        Long userId = UserContext.requireUserId();

        // 敏感词校验（昵称、签名）
        if (sensitiveWordService.containsSensitive(req.getNickname())) {
            throw new BizException(ResultCode.CONTENT_SENSITIVE);
        }
        if (StrUtil.isNotBlank(req.getSign()) && sensitiveWordService.containsSensitive(req.getSign())) {
            throw new BizException(ResultCode.CONTENT_SENSITIVE);
        }

        LocalDate birthday = parseBirthday(req.getBirthday());
        if (birthday.isAfter(LocalDate.now())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "生日不能晚于今天");
        }

        UserProfile profile = getOrCreateProfile(userId);
        // 头像选填：未上传/未选择时按用户 ID 分配固定默认头像
        String avatar = StrUtil.isBlank(req.getAvatar())
                ? "/avatars/preset-" + (userId % 8 + 1) + ".svg"
                : req.getAvatar();
        profile.setAvatar(avatar);
        profile.setNickname(req.getNickname());
        profile.setGender(req.getGender());
        profile.setBirthday(birthday);
        profile.setCity(req.getCity());
        profile.setSign(StrUtil.nullToEmpty(req.getSign()));
        profile.setCompletePercent(calcCompletePercent(profile));
        userProfileMapper.updateById(profile);

        // 置 profile_complete_flag=1
        updateUserFlag(userId, "profile");
        log.info("用户 {} 完善资料完成，完整度 {}%", userId, profile.getCompletePercent());
        return UserConverter.toProfileResp(userProfileMapper.selectById(profile.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResp updateProfile(ProfileUpdateReq req) {
        Long userId = UserContext.requireUserId();
        UserProfile profile = getOrCreateProfile(userId);

        // 昵称：敏感词 + 修改次数校验
        if (StrUtil.isNotBlank(req.getNickname()) && !req.getNickname().equals(profile.getNickname())) {
            if (sensitiveWordService.containsSensitive(req.getNickname())) {
                throw new BizException(ResultCode.CONTENT_SENSITIVE);
            }
            int modifyCount = profile.getNicknameModifyCount() == null ? 0 : profile.getNicknameModifyCount();
            if (modifyCount >= NICKNAME_MAX_MODIFY) {
                throw new BizException(ResultCode.NO_PERMISSION_DELETE.getCode(), "昵称修改次数已达上限");
            }
            profile.setNickname(req.getNickname());
            profile.setNicknameModifyCount(modifyCount + 1);
        }
        if (StrUtil.isNotBlank(req.getSign())) {
            if (sensitiveWordService.containsSensitive(req.getSign())) {
                throw new BizException(ResultCode.CONTENT_SENSITIVE);
            }
            profile.setSign(req.getSign());
        }
        if (StrUtil.isNotBlank(req.getIntro())) {
            if (sensitiveWordService.containsSensitive(req.getIntro())) {
                throw new BizException(ResultCode.CONTENT_SENSITIVE);
            }
            profile.setIntro(req.getIntro());
        }
        if (StrUtil.isNotBlank(req.getAvatar())) {
            // 头像选填：未上传/未选择时按用户 ID 分配固定默认头像
        String avatar = StrUtil.isBlank(req.getAvatar())
                ? "/avatars/preset-" + (userId % 8 + 1) + ".svg"
                : req.getAvatar();
        profile.setAvatar(avatar);
        }
        if (StrUtil.isNotBlank(req.getCity())) {
            profile.setCity(req.getCity());
        }
        if (req.getEmotionStatus() != null) {
            profile.setEmotionStatus(req.getEmotionStatus());
        }
        if (req.getFriendGoal() != null) {
            profile.setFriendGoal(req.getFriendGoal());
        }
        profile.setCompletePercent(calcCompletePercent(profile));
        userProfileMapper.updateById(profile);
        return UserConverter.toProfileResp(userProfileMapper.selectById(profile.getId()));
    }

    @Override
    public UserHomeResp getUserHome(Long userId) {
        Long currentUserId = UserContext.requireUserId();

        // 被拉黑不可访问（双向）
        if (isBlacklistedBoth(currentUserId, userId)) {
            throw new BizException(ResultCode.BLACKLISTED);
        }

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("LIMIT 1"));
        if (profile == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        UserPrivacySetting privacy = userPrivacySettingMapper.selectOne(
                new LambdaQueryWrapper<UserPrivacySetting>()
                        .eq(UserPrivacySetting::getUserId, userId)
                        .last("LIMIT 1"));

        UserHomeResp resp = new UserHomeResp();
        resp.setUserId(userId);
        resp.setNickname(profile.getNickname());
        resp.setAvatar(profile.getAvatar());
        resp.setGender(profile.getGender());
        resp.setSign(profile.getSign());
        resp.setIntro(profile.getIntro());
        resp.setSoulId(profile.getSoulId());

        // 隐私过滤
        boolean showAge = privacy == null || isYes(privacy.getShowAge());
        boolean showCity = privacy == null || isYes(privacy.getShowCity());
        resp.setAge(showAge ? UserConverter.calcAge(profile.getBirthday()) : null);
        resp.setCity(showCity ? profile.getCity() : null);

        // 标签
        List<UserTag> userTags = userTagMapper.selectList(
                new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, userId));
        List<Long> tagIds = userTags.stream().map(UserTag::getTagId).toList();
        List<UserHomeResp.TagItem> tagItems = new ArrayList<>();
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            for (Tag tag : tags) {
                if (tag.getStatus() != null && tag.getStatus() == CommonConstant.STATUS_ENABLE) {
                    tagItems.add(new UserHomeResp.TagItem(tag.getId(), tag.getName()));
                }
            }
        }
        resp.setTags(tagItems);

        // 灵魂测试结果
        UserSoulTestRecord record = userSoulTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserSoulTestRecord>()
                        .eq(UserSoulTestRecord::getUserId, userId)
                        .last("LIMIT 1"));
        if (record != null && StrUtil.isNotBlank(record.getResultCode())) {
            SoulTestResult result = soulTestResultMapper.selectOne(
                    new LambdaQueryWrapper<SoulTestResult>()
                            .eq(SoulTestResult::getResultCode, record.getResultCode())
                            .last("LIMIT 1"));
            if (result != null) {
                UserHomeResp.SoulResultItem item = new UserHomeResp.SoulResultItem();
                item.setResultCode(result.getResultCode());
                item.setName(result.getName());
                item.setDescription(result.getDescription());
                item.setSocialAdvice(result.getSocialAdvice());
                resp.setSoulResult(item);
            }
        }

        // 动态数（仅统计正常且已过审）
        Long dynamicCount = dynamicContentMapper.selectCount(
                new LambdaQueryWrapper<DynamicContent>()
                        .eq(DynamicContent::getUserId, userId)
                        .eq(DynamicContent::getStatus, 1)
                        .eq(DynamicContent::getAuditStatus, 0));
        resp.setDynamicCount(dynamicCount.intValue());

        // 我是否已拉黑对方
        Long blackCount = userBlacklistMapper.selectCount(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getUserId, currentUserId)
                        .eq(UserBlacklist::getTargetUserId, userId)
                        .eq(UserBlacklist::getStatus, CommonConstant.YES));
        resp.setBlacklisted(blackCount != null && blackCount > 0);

        // 今日是否已打招呼（我 → 对方）
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        Long greetCount = greetRecordMapper.selectCount(
                new LambdaQueryWrapper<GreetRecord>()
                        .eq(GreetRecord::getFromUserId, currentUserId)
                        .eq(GreetRecord::getToUserId, userId)
                        .ge(GreetRecord::getCreateTime, dayStart));
        resp.setGreetedToday(greetCount != null && greetCount > 0);

        return resp;
    }

    // ---------------- 私有辅助 ----------------

    private UserProfile getOrCreateProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("LIMIT 1"));
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setNickname("用户" + userId);
            profile.setAvatar("");
            profile.setGender(0);
            profile.setCity("");
            profile.setSign("");
            profile.setEmotionStatus(0);
            profile.setFriendGoal(0);
            profile.setSoulId("S" + userId);
            profile.setCompletePercent(0);
            profile.setNicknameModifyCount(0);
            userProfileMapper.insert(profile);
        }
        return profile;
    }

    /**
     * 计算资料完整度（百分比）。按字段权重求和。
     * <p>权重：头像 15、昵称 15、性别 10、生日 15、城市 15、签名 10、情感状态 10、交友目的 10 = 100。
     */
    private int calcCompletePercent(UserProfile p) {
        int percent = 0;
        if (StrUtil.isNotBlank(p.getAvatar())) {
            percent += 15;
        }
        if (StrUtil.isNotBlank(p.getNickname())) {
            percent += 15;
        }
        if (p.getGender() != null && p.getGender() != 0) {
            percent += 10;
        }
        if (p.getBirthday() != null) {
            percent += 15;
        }
        if (StrUtil.isNotBlank(p.getCity())) {
            percent += 15;
        }
        if (StrUtil.isNotBlank(p.getSign())) {
            percent += 10;
        }
        if (p.getEmotionStatus() != null && p.getEmotionStatus() != 0) {
            percent += 10;
        }
        if (p.getFriendGoal() != null && p.getFriendGoal() != 0) {
            percent += 10;
        }
        return percent;
    }

    private LocalDate parseBirthday(String birthday) {
        try {
            return LocalDate.parse(birthday, DATE_FMT);
        } catch (Exception e) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "生日格式须为 yyyy-MM-dd");
        }
    }

    private void updateUserFlag(Long userId, String flagType) {
        User update = new User();
        update.setId(userId);
        switch (flagType) {
            case "profile" -> update.setProfileCompleteFlag(CommonConstant.YES);
            case "tag" -> update.setTagFlag(CommonConstant.YES);
            case "test" -> update.setTestFlag(CommonConstant.YES);
            default -> {
                return;
            }
        }
        userMapper.updateById(update);
    }

    private boolean isBlacklistedBoth(Long a, Long b) {
        Long count = userBlacklistMapper.selectCount(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getStatus, CommonConstant.YES)
                        .and(w -> w
                                .and(x -> x.eq(UserBlacklist::getUserId, a)
                                        .eq(UserBlacklist::getTargetUserId, b))
                                .or(y -> y.eq(UserBlacklist::getUserId, b)
                                        .eq(UserBlacklist::getTargetUserId, a))));
        return count != null && count > 0;
    }

    private boolean isYes(Integer value) {
        return value != null && value == CommonConstant.YES;
    }
}
