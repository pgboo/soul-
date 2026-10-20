package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.BlacklistAddReq;
import com.soul.social.dto.req.BlacklistRemoveReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.BlacklistItemResp;
import com.soul.social.entity.UserBlacklist;
import com.soul.social.entity.UserProfile;
import com.soul.social.mapper.UserBlacklistMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.BlacklistService;
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
 * 黑名单服务实现。
 */
@Slf4j
@Service
public class BlacklistServiceImpl implements BlacklistService {

    @Resource
    private UserBlacklistMapper userBlacklistMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(BlacklistAddReq req) {
        Long userId = UserContext.requireUserId();
        Long targetUserId = req.getTargetUserId();
        if (userId.equals(targetUserId)) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }
        UserBlacklist exist = userBlacklistMapper.selectOne(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getUserId, userId)
                        .eq(UserBlacklist::getTargetUserId, targetUserId)
                        .last("LIMIT 1"));
        if (exist != null) {
            // 已存在则重新生效
            if (exist.getStatus() == null || exist.getStatus() != CommonConstant.YES) {
                exist.setStatus(CommonConstant.YES);
                userBlacklistMapper.updateById(exist);
            }
            return;
        }
        UserBlacklist record = new UserBlacklist();
        record.setUserId(userId);
        record.setTargetUserId(targetUserId);
        record.setStatus(CommonConstant.YES);
        userBlacklistMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(BlacklistRemoveReq req) {
        Long userId = UserContext.requireUserId();
        UserBlacklist exist = userBlacklistMapper.selectOne(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getUserId, userId)
                        .eq(UserBlacklist::getTargetUserId, req.getTargetUserId())
                        .last("LIMIT 1"));
        if (exist == null) {
            return;
        }
        // 逻辑删除
        userBlacklistMapper.deleteById(exist.getId());
    }

    @Override
    public PageResult<BlacklistItemResp> list(PageQuery query) {
        Long userId = UserContext.requireUserId();
        Page<UserBlacklist> page = new Page<>(query.safePageNum(), query.safePageSize());
        Page<UserBlacklist> result = userBlacklistMapper.selectPage(page,
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getUserId, userId)
                        .eq(UserBlacklist::getStatus, CommonConstant.YES)
                        .orderByDesc(UserBlacklist::getCreateTime));

        List<UserBlacklist> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(result, new ArrayList<>());
        }
        List<Long> targetIds = records.stream().map(UserBlacklist::getTargetUserId).toList();
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(
                        new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, targetIds))
                .stream().collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));

        List<BlacklistItemResp> items = new ArrayList<>();
        for (UserBlacklist bl : records) {
            BlacklistItemResp item = new BlacklistItemResp();
            item.setUserId(bl.getTargetUserId());
            UserProfile profile = profileMap.get(bl.getTargetUserId());
            item.setNickname(profile == null ? "" : profile.getNickname());
            item.setAvatar(profile == null ? "" : profile.getAvatar());
            item.setBlacklistTime(bl.getCreateTime());
            items.add(item);
        }
        return PageResult.of(result, items);
    }

    @Override
    public boolean isBlacklisted(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }
        Long count = userBlacklistMapper.selectCount(
                new LambdaQueryWrapper<UserBlacklist>()
                        .eq(UserBlacklist::getStatus, CommonConstant.YES)
                        .and(w -> w
                                .and(x -> x.eq(UserBlacklist::getUserId, userId)
                                        .eq(UserBlacklist::getTargetUserId, targetUserId))
                                .or(y -> y.eq(UserBlacklist::getUserId, targetUserId)
                                        .eq(UserBlacklist::getTargetUserId, userId))));
        return count != null && count > 0;
    }
}
