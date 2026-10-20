package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.CommentListReq;
import com.soul.social.dto.req.DynamicCollectReq;
import com.soul.social.dto.req.DynamicCommentReq;
import com.soul.social.dto.req.DynamicLikeReq;
import com.soul.social.dto.resp.CollectResp;
import com.soul.social.dto.resp.CommentItemResp;
import com.soul.social.dto.resp.CommentResp;
import com.soul.social.dto.resp.DynamicItemResp;
import com.soul.social.dto.resp.LikeResp;
import com.soul.social.entity.DynamicCollect;
import com.soul.social.entity.DynamicComment;
import com.soul.social.entity.DynamicContent;
import com.soul.social.entity.DynamicLike;
import com.soul.social.entity.UserProfile;
import com.soul.social.guard.ContentGuardService;
import com.soul.social.mapper.DynamicCollectMapper;
import com.soul.social.mapper.DynamicCommentMapper;
import com.soul.social.mapper.DynamicContentMapper;
import com.soul.social.mapper.DynamicLikeMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.InteractionService;
import com.soul.social.service.NoticeService;
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
 * 广场互动服务实现（点赞/收藏/评论）。
 */
@Slf4j
@Service
public class InteractionServiceImpl implements InteractionService {

    @Resource
    private DynamicContentMapper dynamicContentMapper;

    @Resource
    private DynamicLikeMapper dynamicLikeMapper;

    @Resource
    private DynamicCollectMapper dynamicCollectMapper;

    @Resource
    private DynamicCommentMapper dynamicCommentMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private ContentGuardService contentGuardService;

    @Resource
    private NoticeService noticeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResp like(DynamicLikeReq req) {
        Long userId = UserContext.requireUserId();
        DynamicContent content = requireDynamic(req.getDynamicId());
        // 下架不可操作
        ensureOnline(content);

        boolean doLike = "like".equalsIgnoreCase(req.getAction());
        DynamicLike exist = dynamicLikeMapper.selectOne(
                new LambdaQueryWrapper<DynamicLike>()
                        .eq(DynamicLike::getDynamicId, req.getDynamicId())
                        .eq(DynamicLike::getUserId, userId)
                        .last("LIMIT 1"));

        boolean current = exist != null && exist.getStatus() != null && exist.getStatus() == 1;
        // 幂等：状态已符合则直接返回
        if (current == doLike) {
            return new LikeResp(current, nz(content.getLikeCount()));
        }

        int newCount;
        if (doLike) {
            if (exist == null) {
                DynamicLike like = new DynamicLike();
                like.setDynamicId(req.getDynamicId());
                like.setUserId(userId);
                like.setStatus(1);
                dynamicLikeMapper.insert(like);
            } else {
                exist.setStatus(1);
                dynamicLikeMapper.updateById(exist);
            }
            newCount = nz(content.getLikeCount()) + 1;
            dynamicContentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DynamicContent>()
                            .eq(DynamicContent::getId, req.getDynamicId())
                            .set(DynamicContent::getLikeCount, newCount));
            // 通知作者（自己赞自己不通知，NoticeService 内部已处理）
            noticeService.createNotice(content.getUserId(), userId, NoticeService.TYPE_LIKE,
                    "收到一个赞", "有人赞了你的瞬间", NoticeService.TARGET_DYNAMIC, req.getDynamicId());
        } else {
            exist.setStatus(0);
            dynamicLikeMapper.updateById(exist);
            newCount = Math.max(0, nz(content.getLikeCount()) - 1);
            dynamicContentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DynamicContent>()
                            .eq(DynamicContent::getId, req.getDynamicId())
                            .set(DynamicContent::getLikeCount, newCount));
        }
        return new LikeResp(doLike, newCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectResp collect(DynamicCollectReq req) {
        Long userId = UserContext.requireUserId();
        DynamicContent content = requireDynamic(req.getDynamicId());
        // 下架不可收藏 → 5002
        if (content.getStatus() == null || content.getStatus() != 1) {
            throw new BizException(ResultCode.CONTENT_OFFLINE);
        }

        boolean doCollect = "collect".equalsIgnoreCase(req.getAction());
        DynamicCollect exist = dynamicCollectMapper.selectOne(
                new LambdaQueryWrapper<DynamicCollect>()
                        .eq(DynamicCollect::getDynamicId, req.getDynamicId())
                        .eq(DynamicCollect::getUserId, userId)
                        .last("LIMIT 1"));

        boolean current = exist != null && exist.getStatus() != null && exist.getStatus() == 1;
        if (current == doCollect) {
            return new CollectResp(current, nz(content.getCollectCount()));
        }

        int newCount;
        if (doCollect) {
            if (exist == null) {
                DynamicCollect collect = new DynamicCollect();
                collect.setDynamicId(req.getDynamicId());
                collect.setUserId(userId);
                collect.setStatus(1);
                dynamicCollectMapper.insert(collect);
            } else {
                exist.setStatus(1);
                dynamicCollectMapper.updateById(exist);
            }
            newCount = nz(content.getCollectCount()) + 1;
        } else {
            exist.setStatus(0);
            dynamicCollectMapper.updateById(exist);
            newCount = Math.max(0, nz(content.getCollectCount()) - 1);
        }
        dynamicContentMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DynamicContent>()
                        .eq(DynamicContent::getId, req.getDynamicId())
                        .set(DynamicContent::getCollectCount, newCount));
        // 收藏不通知
        return new CollectResp(doCollect, newCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResp comment(DynamicCommentReq req) {
        Long userId = UserContext.requireUserId();
        DynamicContent content = requireDynamic(req.getDynamicId());
        ensureOnline(content);

        // R002 禁言
        contentGuardService.checkMute(userId);
        // R004 与作者拉黑校验
        contentGuardService.checkBlacklist(userId, content.getUserId());
        // R010 敏感词
        contentGuardService.checkSensitive(req.getContent());

        DynamicComment comment = new DynamicComment();
        comment.setDynamicId(req.getDynamicId());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        comment.setReplyUserId(req.getReplyUserId() == null ? 0L : req.getReplyUserId());
        comment.setContent(req.getContent());
        comment.setStatus(1);
        comment.setReportCount(0);
        dynamicCommentMapper.insert(comment);

        // 评论数 +1
        int newCount = nz(content.getCommentCount()) + 1;
        dynamicContentMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DynamicContent>()
                        .eq(DynamicContent::getId, req.getDynamicId())
                        .set(DynamicContent::getCommentCount, newCount));

        // 通知作者（自己评自己不通知）
        noticeService.createNotice(content.getUserId(), userId, NoticeService.TYPE_COMMENT,
                "收到一条评论", req.getContent(), NoticeService.TARGET_DYNAMIC, req.getDynamicId());
        // 通知被回复人（若指定且不是作者本人、不是自己）
        Long replyUserId = req.getReplyUserId();
        if (replyUserId != null && replyUserId > 0
                && !replyUserId.equals(content.getUserId())
                && !replyUserId.equals(userId)) {
            noticeService.createNotice(replyUserId, userId, NoticeService.TYPE_REPLY,
                    "收到一条回复", req.getContent(), NoticeService.TARGET_DYNAMIC, req.getDynamicId());
        }

        return new CommentResp(comment.getId());
    }

    @Override
    public PageResult<CommentItemResp> commentList(CommentListReq req) {
        Page<DynamicComment> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<DynamicComment> result = dynamicCommentMapper.selectPage(page,
                new LambdaQueryWrapper<DynamicComment>()
                        .eq(DynamicComment::getDynamicId, req.getDynamicId())
                        .eq(DynamicComment::getStatus, 1)
                        .orderByDesc(DynamicComment::getCreateTime));

        List<DynamicComment> comments = result.getRecords();
        if (comments.isEmpty()) {
            return PageResult.of(result, new ArrayList<>());
        }
        List<Long> userIds = new ArrayList<>();
        for (DynamicComment c : comments) {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null && c.getReplyUserId() > 0) {
                userIds.add(c.getReplyUserId());
            }
        }
        Map<Long, UserProfile> profileMap = userProfileMapper.selectBatchIds(userIds).stream()
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
        return PageResult.of(result, items);
    }

    // ---------------- 私有辅助 ----------------

    private DynamicContent requireDynamic(Long dynamicId) {
        DynamicContent content = dynamicContentMapper.selectById(dynamicId);
        if (content == null) {
            throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
        }
        return content;
    }

    private void ensureOnline(DynamicContent content) {
        if (content.getStatus() == null || content.getStatus() != 1
                || content.getAuditStatus() == null || content.getAuditStatus() != 0) {
            throw new BizException(ResultCode.CONTENT_OFFLINE);
        }
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
