package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.soul.social.entity.UserNotice;
import com.soul.social.mapper.UserNoticeMapper;
import com.soul.social.service.NoticeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现。
 */
@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private UserNoticeMapper userNoticeMapper;

    @Override
    public void createNotice(Long toUserId, Long fromUserId, int noticeType,
                             String title, String content, int targetType, Long targetId) {
        if (toUserId == null) {
            return;
        }
        // 自己触发自己不通知
        if (fromUserId != null && fromUserId.equals(toUserId)) {
            return;
        }
        UserNotice notice = new UserNotice();
        notice.setUserId(toUserId);
        notice.setNoticeType(noticeType);
        notice.setTitle(StrUtil.nullToEmpty(title));
        notice.setContent(StrUtil.nullToEmpty(content));
        notice.setFromUserId(fromUserId == null ? 0L : fromUserId);
        notice.setTargetType(targetType);
        notice.setTargetId(targetId == null ? 0L : targetId);
        notice.setReadFlag(0);
        userNoticeMapper.insert(notice);
        log.debug("创建通知: to={}, from={}, type={}, target={}/{}",
                toUserId, fromUserId, noticeType, targetType, targetId);
    }
}
