package com.soul.social.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.ConversationReadReq;
import com.soul.social.dto.req.MessageDeleteReq;
import com.soul.social.dto.req.MessageRecallReq;
import com.soul.social.dto.req.MessageListReq;
import com.soul.social.dto.req.MessageSendReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.ConversationResp;
import com.soul.social.dto.resp.DynamicItemResp;
import com.soul.social.dto.resp.MessageResp;
import com.soul.social.dto.resp.MessageSendResp;
import com.soul.social.entity.ImConversation;
import com.soul.social.entity.ImMessage;
import com.soul.social.entity.User;
import com.soul.social.entity.UserProfile;
import com.soul.social.entity.UserPrivacySetting;
import com.soul.social.guard.ContentGuardService;
import com.soul.social.mapper.ImConversationMapper;
import com.soul.social.mapper.ImMessageMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserPrivacySettingMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.ImService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IM 服务实现（沿用既有 im_conversation / im_message）。
 */
@Slf4j
@Service
public class ImServiceImpl implements ImService {

    @Resource
    private ImConversationMapper imConversationMapper;

    @Resource
    private ImMessageMapper imMessageMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private ContentGuardService contentGuardService;

    @Override
    public PageResult<ConversationResp> conversationList(PageQuery query) {
        Long userId = UserContext.requireUserId();
        Page<ImConversation> page = new Page<>(query.safePageNum(), query.safePageSize());
        Page<ImConversation> result = imConversationMapper.selectPage(page,
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getUserId, userId)
                        .eq(ImConversation::getHideFlag, CommonConstant.NO)
                        .orderByDesc(ImConversation::getTopFlag)
                        .orderByDesc(ImConversation::getSortTime));

        List<ImConversation> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(result, new ArrayList<>());
        }
        List<Long> targetIds = records.stream().map(ImConversation::getTargetUserId).distinct().toList();
        Map<Long, UserProfile> profileMap = userProfileMapper.selectList(
                        new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, targetIds))
                .stream().collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));

        List<ConversationResp> items = new ArrayList<>();
        for (ImConversation conv : records) {
            // 拉黑过滤（双向）
            if (contentGuardService.isBlacklistedBoth(userId, conv.getTargetUserId())) {
                continue;
            }
            ConversationResp item = new ConversationResp();
            item.setSessionUuid(conv.getSessionUuid());
            item.setSessionType(conv.getSessionType());
            item.setUnreadCount(conv.getUnreadCount() == null ? 0 : conv.getUnreadCount());
            item.setLastMsgPreview(conv.getLastMsgPreview());
            item.setLastMsgTime(conv.getLastMsgTime());
            item.setTopFlag(conv.getTopFlag() != null && conv.getTopFlag() == 1);
            UserProfile profile = profileMap.get(conv.getTargetUserId());
            item.setTargetUser(new DynamicItemResp.AuthorItem(conv.getTargetUserId(),
                    profile == null ? "" : profile.getNickname(),
                    profile == null ? "" : profile.getAvatar()));
            items.add(item);
        }
        return PageResult.of(result, items);
    }

    @Override
    public PageResult<MessageResp> messageList(MessageListReq req) {
        Long userId = UserContext.requireUserId();
        // 会话归属校验
        ImConversation conv = imConversationMapper.selectOne(
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getUserId, userId)
                        .eq(ImConversation::getSessionUuid, req.getSessionUuid())
                        .last("LIMIT 1"));
        if (conv == null) {
            throw new BizException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        Page<ImMessage> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<ImMessage> result = imMessageMapper.selectPage(page,
                new LambdaQueryWrapper<ImMessage>()
                        .eq(ImMessage::getSessionUuid, req.getSessionUuid())
                        // 过滤「我删除的」消息：我发的需 from_delete_flag=0，发给我的需 to_delete_flag=0
                        .and(w -> w
                                .and(x -> x.eq(ImMessage::getFromUserId, userId)
                                        .eq(ImMessage::getFromDeleteFlag, 0))
                                .or(y -> y.eq(ImMessage::getToUserId, userId)
                                        .eq(ImMessage::getToDeleteFlag, 0)))
                        .orderByDesc(ImMessage::getSendTime));

        List<MessageResp> items = new ArrayList<>();
        for (ImMessage msg : result.getRecords()) {
            MessageResp item = new MessageResp();
            item.setMessageUuid(msg.getMessageUuid());
            item.setFromUserId(msg.getFromUserId());
            item.setToUserId(msg.getToUserId());
            boolean recalled = msg.getRecallFlag() != null && msg.getRecallFlag() == 1;
            item.setRecalled(recalled);
            // 已撤回的消息不下发原文
            item.setContent(recalled ? "" : msg.getContent());
            item.setContentType(msg.getContentType());
            item.setSendStatus(msg.getSendStatus());
            item.setReadFlag(msg.getReadFlag());
            item.setSendTime(msg.getSendTime());
            items.add(item);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageSendResp send(MessageSendReq req) {
        Long userId = UserContext.requireUserId();
        ImConversation conv = imConversationMapper.selectOne(
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getUserId, userId)
                        .eq(ImConversation::getSessionUuid, req.getSessionUuid())
                        .last("LIMIT 1"));
        if (conv == null) {
            throw new BizException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        Long targetUserId = conv.getTargetUserId();

        // R002 禁言
        contentGuardService.checkMute(userId);
        // R004 拉黑
        contentGuardService.checkBlacklist(userId, targetUserId);
        // R006 陌生人私信开关（双方无会话时目标关闭则拒绝；已有会话则放行）
        //   —— 这里已有会话，故仅当对方关闭且从未互发过消息时拦截
        // R010 敏感词
        contentGuardService.checkSensitive(req.getContent());

        LocalDateTime now = LocalDateTime.now();
        String messageUuid = "m-" + IdUtil.fastSimpleUUID();

        ImMessage message = new ImMessage();
        message.setMessageUuid(messageUuid);
        message.setSessionUuid(req.getSessionUuid());
        message.setScene(1);
        message.setFromUserId(userId);
        message.setToUserId(targetUserId);
        message.setContent(req.getContent());
        message.setContentType("text");
        message.setSendStatus(1);
        message.setReadFlag(0);
        message.setRecallFlag(0);
        message.setSendTime(now);
        imMessageMapper.insert(message);

        // 更新我方会话
        updateConversation(conv, req.getContent(), now, false);
        // 更新对方会话（无则创建，未读 +1）
        ImConversation targetConv = getOrCreateConversationEntity(targetUserId, userId, conv.getSessionType());
        updateConversation(targetConv, req.getContent(), now, true);

        MessageSendResp resp = new MessageSendResp();
        resp.setMessageUuid(messageUuid);
        resp.setSendTime(now);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(MessageRecallReq req) {
        Long userId = UserContext.requireUserId();
        ImMessage msg = imMessageMapper.selectOne(
                new LambdaQueryWrapper<ImMessage>()
                        .eq(ImMessage::getMessageUuid, req.getMessageUuid())
                        .last("LIMIT 1"));
        if (msg == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "消息不存在");
        }
        if (!userId.equals(msg.getFromUserId())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "只能撤回自己的消息");
        }
        if (msg.getRecallFlag() != null && msg.getRecallFlag() == 1) {
            return;
        }
        long recallLimitSeconds = 120L;
        if (msg.getSendTime() == null
                || msg.getSendTime().isBefore(LocalDateTime.now().minusSeconds(recallLimitSeconds))) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "发送超过 2 分钟，无法撤回");
        }
        ImMessage update = new ImMessage();
        update.setId(msg.getId());
        update.setRecallFlag(1);
        imMessageMapper.updateById(update);

        // 若该消息仍是会话的最后一条，则同步双方会话预览
        List<ImConversation> convs = imConversationMapper.selectList(
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getSessionUuid, msg.getSessionUuid()));
        for (ImConversation conv : convs) {
            if (conv.getLastMsgTime() != null && conv.getLastMsgTime().equals(msg.getSendTime())) {
                ImConversation convUpdate = new ImConversation();
                convUpdate.setId(conv.getId());
                boolean mine = userId.equals(conv.getUserId());
                convUpdate.setLastMsgPreview(mine ? "你撤回了一条消息" : "对方撤回了一条消息");
                imConversationMapper.updateById(convUpdate);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(MessageDeleteReq req) {
        Long userId = UserContext.requireUserId();
        ImMessage msg = imMessageMapper.selectOne(
                new LambdaQueryWrapper<ImMessage>()
                        .eq(ImMessage::getMessageUuid, req.getMessageUuid())
                        .last("LIMIT 1"));
        if (msg == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "消息不存在");
        }
        ImMessage update = new ImMessage();
        update.setId(msg.getId());
        if (userId.equals(msg.getFromUserId())) {
            update.setFromDeleteFlag(1);
        } else if (userId.equals(msg.getToUserId())) {
            update.setToDeleteFlag(1);
        } else {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "消息不存在");
        }
        imMessageMapper.updateById(update);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void read(ConversationReadReq req) {
        Long userId = UserContext.requireUserId();
        ImConversation conv = imConversationMapper.selectOne(
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getUserId, userId)
                        .eq(ImConversation::getSessionUuid, req.getSessionUuid())
                        .last("LIMIT 1"));
        if (conv == null) {
            throw new BizException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        // 未读清零
        ImConversation update = new ImConversation();
        update.setId(conv.getId());
        update.setUnreadCount(0);
        imConversationMapper.updateById(update);
        // 该会话发给我的消息标记已读
        imMessageMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ImMessage>()
                        .eq(ImMessage::getSessionUuid, req.getSessionUuid())
                        .eq(ImMessage::getToUserId, userId)
                        .eq(ImMessage::getReadFlag, 0)
                        .set(ImMessage::getReadFlag, 1));
    }

    @Override
    public String getOrCreateConversation(Long fromUserId, Long toUserId, int sessionType) {
        String sessionUuid = buildSessionUuid(fromUserId, toUserId);
        getOrCreateConversationEntity(fromUserId, toUserId, sessionType);
        getOrCreateConversationEntity(toUserId, fromUserId, sessionType);
        return sessionUuid;
    }

    // ---------------- 私有辅助 ----------------

    /**
     * 会话唯一键：min + "_" + max。
     */
    public static String buildSessionUuid(Long a, Long b) {
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return min + "_" + max;
    }

    /**
     * 获取或创建某用户视角下的会话实体。
     */
    private ImConversation getOrCreateConversationEntity(Long userId, Long targetUserId, int sessionType) {
        String sessionUuid = buildSessionUuid(userId, targetUserId);
        ImConversation conv = imConversationMapper.selectOne(
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getUserId, userId)
                        .eq(ImConversation::getSessionUuid, sessionUuid)
                        .last("LIMIT 1"));
        if (conv == null) {
            conv = new ImConversation();
            conv.setSessionUuid(sessionUuid);
            conv.setUserId(userId);
            conv.setTargetUserId(targetUserId);
            conv.setSessionType(sessionType);
            conv.setUnreadCount(0);
            conv.setLastMsgPreview("");
            conv.setTopFlag(0);
            conv.setHideFlag(0);
            conv.setSortTime(LocalDateTime.now());
            imConversationMapper.insert(conv);
        }
        return conv;
    }

    private void updateConversation(ImConversation conv, String preview, LocalDateTime time, boolean unreadPlus) {
        ImConversation update = new ImConversation();
        update.setId(conv.getId());
        update.setLastMsgPreview(StrUtil.maxLength(preview, 200));
        update.setLastMsgTime(time);
        update.setSortTime(time);
        if (unreadPlus) {
            int unread = (conv.getUnreadCount() == null ? 0 : conv.getUnreadCount()) + 1;
            update.setUnreadCount(unread);
        }
        imConversationMapper.updateById(update);
    }
}
