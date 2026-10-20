package com.soul.social.guard;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.client.SensitiveWordService;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.entity.User;
import com.soul.social.entity.UserBlacklist;
import com.soul.social.mapper.UserBlacklistMapper;
import com.soul.social.mapper.UserMapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 治理横切服务：禁言 / 黑名单 / 敏感词 / 私信开关统一校验。
 * <p>对应 R002 / R004 / R006 / R010，供各写接口复用。
 */
@Slf4j
@Service
public class ContentGuardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBlacklistMapper userBlacklistMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    /**
     * R002 禁言校验：mute_end_time > now → USER_MUTED。
     */
    public void checkMute(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        LocalDateTime muteEnd = user.getMuteEndTime();
        if (muteEnd != null && muteEnd.isAfter(LocalDateTime.now())) {
            throw new BizException(ResultCode.USER_MUTED);
        }
    }

    /**
     * R003 风控观察：risk_level>=1 需要审核。返回 auditStatus（0 已过审 / 1 待审核）。
     */
    public int needAudit(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getRiskLevel() != null && user.getRiskLevel() >= 1) {
            return 1;
        }
        return 0;
    }

    /**
     * R004 黑名单校验：双向命中 → BLACKLISTED。
     */
    public void checkBlacklist(Long fromUserId, Long toUserId) {
        if (fromUserId == null || toUserId == null) {
            return;
        }
        Long count = userBlacklistMapper.selectCount(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getStatus, CommonConstant.YES)
                        .and(w -> w
                                .and(x -> x.eq(UserBlacklist::getUserId, fromUserId)
                                        .eq(UserBlacklist::getTargetUserId, toUserId))
                                .or(y -> y.eq(UserBlacklist::getUserId, toUserId)
                                        .eq(UserBlacklist::getTargetUserId, fromUserId))));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BLACKLISTED);
        }
    }

    /**
     * R010 敏感词校验：命中 → CONTENT_SENSITIVE。
     */
    public void checkSensitive(String text) {
        if (StrUtil.isNotBlank(text) && sensitiveWordService.containsSensitive(text)) {
            throw new BizException(ResultCode.CONTENT_SENSITIVE);
        }
    }

    /**
     * 判断是否双向拉黑（返回布尔，不抛异常，供列表过滤使用）。
     */
    public boolean isBlacklistedBoth(Long a, Long b) {
        if (a == null || b == null) {
            return false;
        }
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
}
