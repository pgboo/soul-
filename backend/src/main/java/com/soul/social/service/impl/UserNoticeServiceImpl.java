package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.NoticeListReq;
import com.soul.social.dto.req.NoticeReadReq;
import com.soul.social.dto.resp.DynamicItemResp;
import com.soul.social.dto.resp.NoticeResp;
import com.soul.social.entity.UserNotice;
import com.soul.social.entity.UserProfile;
import com.soul.social.mapper.UserNoticeMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.UserNoticeService;
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
 * 站内通知服务实现（用户端查询）。
 */
@Slf4j
@Service
public class UserNoticeServiceImpl implements UserNoticeService {

    @Resource
    private UserNoticeMapper userNoticeMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Override
    public PageResult<NoticeResp> list(NoticeListReq req) {
        Long userId = UserContext.requireUserId();
        Page<UserNotice> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<UserNotice> result = userNoticeMapper.selectPage(page,
                new LambdaQueryWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .eq(req.getNoticeType() != null, UserNotice::getNoticeType, req.getNoticeType())
                        .orderByDesc(UserNotice::getCreateTime));

        List<UserNotice> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(result, new ArrayList<>());
        }
        List<Long> fromUserIds = records.stream()
                .map(UserNotice::getFromUserId)
                .filter(id -> id != null && id > 0)
                .distinct().toList();
        Map<Long, UserProfile> profileMap = fromUserIds.isEmpty() ? Map.of()
                : userProfileMapper.selectBatchIds(fromUserIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));

        List<NoticeResp> items = new ArrayList<>();
        for (UserNotice notice : records) {
            NoticeResp item = new NoticeResp();
            item.setNoticeId(notice.getId());
            item.setNoticeType(notice.getNoticeType());
            item.setTitle(notice.getTitle());
            item.setContent(notice.getContent());
            item.setTargetType(notice.getTargetType());
            item.setTargetId(notice.getTargetId());
            item.setReadFlag(notice.getReadFlag());
            item.setCreateTime(notice.getCreateTime());
            if (notice.getFromUserId() != null && notice.getFromUserId() > 0) {
                UserProfile profile = profileMap.get(notice.getFromUserId());
                item.setFromUser(new DynamicItemResp.AuthorItem(notice.getFromUserId(),
                        profile == null ? "" : profile.getNickname(),
                        profile == null ? "" : profile.getAvatar()));
            }
            items.add(item);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void read(NoticeReadReq req) {
        Long userId = UserContext.requireUserId();
        List<Long> noticeIds = req.getNoticeIds();
        if (noticeIds == null || noticeIds.isEmpty()) {
            // 全部已读
            userNoticeMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserNotice>()
                            .eq(UserNotice::getUserId, userId)
                            .eq(UserNotice::getReadFlag, 0)
                            .set(UserNotice::getReadFlag, 1));
            return;
        }
        // 指定通知：校验归属
        List<UserNotice> notices = userNoticeMapper.selectBatchIds(noticeIds);
        for (Long id : noticeIds) {
            boolean own = notices.stream().anyMatch(n -> n.getId().equals(id) && n.getUserId().equals(userId));
            if (!own) {
                throw new BizException(ResultCode.NOTICE_NOT_FOUND);
            }
        }
        userNoticeMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .in(UserNotice::getId, noticeIds)
                        .set(UserNotice::getReadFlag, 1));
    }
}
