package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.client.CacheService;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.constant.RedisKeyConstant;
import com.soul.social.common.constant.SysConfigKey;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.GreetSendReq;
import com.soul.social.dto.resp.GreetSendResp;
import com.soul.social.entity.GreetRecord;
import com.soul.social.guard.ContentGuardService;
import com.soul.social.mapper.GreetRecordMapper;
import com.soul.social.service.GreetService;
import com.soul.social.service.ImService;
import com.soul.social.service.NoticeService;
import com.soul.social.service.SysConfigService;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 打招呼服务实现。
 * <p>R014 频控：每日总量上限（system_config.greet_daily_limit）+ 同人一天一次（Service 层查当日记录存在性）。
 */
@Slf4j
@Service
public class GreetServiceImpl implements GreetService {

    /** 默认每日打招呼上限 */
    private static final int DEFAULT_GREET_LIMIT = 20;

    @Resource
    private GreetRecordMapper greetRecordMapper;

    @Resource
    private ImService imService;

    @Resource
    private NoticeService noticeService;

    @Resource
    private ContentGuardService contentGuardService;

    @Resource
    private CacheService cacheService;

    @Resource
    private SysConfigService sysConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GreetSendResp send(GreetSendReq req) {
        Long fromUserId = UserContext.requireUserId();
        Long toUserId = req.getToUserId();
        if (fromUserId.equals(toUserId)) {
            throw new BizException(ResultCode.CANNOT_OPERATE_SELF);
        }

        // R002 禁言 / R004 拉黑 / R010 敏感词
        contentGuardService.checkMute(fromUserId);
        contentGuardService.checkBlacklist(fromUserId, toUserId);
        contentGuardService.checkSensitive(req.getContent());

        // R014 每日总量：内存计数（按用户 + 当日）
        String dailyKey = RedisKeyConstant.GREET_DAILY_PREFIX + fromUserId + ":count";
        long currentCount = parseLong(cacheService.get(dailyKey));
        int limit = sysConfigService.getInt(SysConfigKey.GREET_DAILY_LIMIT, DEFAULT_GREET_LIMIT);
        if (currentCount >= limit) {
            throw new BizException(ResultCode.GREET_LIMIT);
        }

        // R014 同人一天一次：查当日记录存在性（唯一键仅兜底）
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        Long todayCount = greetRecordMapper.selectCount(
                new LambdaQueryWrapper<GreetRecord>()
                        .eq(GreetRecord::getFromUserId, fromUserId)
                        .eq(GreetRecord::getToUserId, toUserId)
                        .ge(GreetRecord::getCreateTime, dayStart));
        if (todayCount != null && todayCount > 0) {
            throw new BizException(ResultCode.GREET_REPEAT);
        }

        // 默认话术
        String content = StrUtil.isNotBlank(req.getContent())
                ? req.getContent() : "嗨，很高兴认识你～";

        // 写打招呼记录
        GreetRecord record = new GreetRecord();
        record.setFromUserId(fromUserId);
        record.setToUserId(toUserId);
        record.setContent(content);
        record.setStatus(1);
        record.setReadFlag(0);
        record.setReplyFlag(0);
        greetRecordMapper.insert(record);

        // 计数 +1，设置到次日 0 点过期
        cacheService.increment(dailyKey, 1);
        cacheService.expire(dailyKey, secondsUntilTomorrow());

        // 建档会话（session_type=2 打招呼）
        String sessionUuid = imService.getOrCreateConversation(fromUserId, toUserId, 2);

        // 发第一条消息（对方未读 +1）
        sendFirstMessage(sessionUuid, fromUserId, toUserId, content);

        // 通知对方（R014 场景：打招呼）
        noticeService.createNotice(toUserId, fromUserId, NoticeService.TYPE_GREET,
                "有人向你打招呼", content, NoticeService.TARGET_CONVERSATION,
                null);

        log.info("用户 {} 向 {} 打招呼，会话 {}", fromUserId, toUserId, sessionUuid);
        return new GreetSendResp(sessionUuid);
    }

    /**
     * 打招呼首条消息：直接写 im_message 并更新会话。为避免与 ImService.send 的会话归属校验冲突，
     * 这里通过 getOrCreateConversation 已建档，再调用 ImService.send 会走归属校验（fromUser 侧会话已存在）。
     */
    private void sendFirstMessage(String sessionUuid, Long fromUserId, Long toUserId, String content) {
        com.soul.social.dto.req.MessageSendReq msgReq = new com.soul.social.dto.req.MessageSendReq();
        msgReq.setSessionUuid(sessionUuid);
        msgReq.setContent(content);
        // ImService.send 内部会用 UserContext 当前用户作为发送人，此处即 fromUserId
        imService.send(msgReq);
    }

    private long parseLong(String value) {
        if (StrUtil.isBlank(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private long secondsUntilTomorrow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, tomorrow).getSeconds();
    }
}
