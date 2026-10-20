package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.AdminDynamicAuditReq;
import com.soul.social.dto.req.AdminDynamicListReq;
import com.soul.social.dto.req.AdminDynamicOfflineReq;
import com.soul.social.dto.resp.AdminDynamicResp;
import com.soul.social.entity.AuditRecord;
import com.soul.social.entity.DynamicContent;
import com.soul.social.entity.DynamicMedia;
import com.soul.social.entity.UserProfile;
import com.soul.social.mapper.AuditRecordMapper;
import com.soul.social.mapper.DynamicContentMapper;
import com.soul.social.mapper.DynamicMediaMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.AdminDynamicService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台瞬间管理服务实现。
 * <p>后台列表含已逻辑删除数据；审核/下架/恢复均落 audit_record 保证可追溯。
 */
@Slf4j
@Service
public class AdminDynamicServiceImpl implements AdminDynamicService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private DynamicContentMapper dynamicContentMapper;

    @Resource
    private DynamicMediaMapper dynamicMediaMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private AuditRecordMapper auditRecordMapper;

    @Override
    public PageResult<AdminDynamicResp> dynamicPage(AdminDynamicListReq req) {
        Page<DynamicContent> page = new Page<>(req.safePageNum(), req.safePageSize());
        IPage<DynamicContent> result = dynamicContentMapper.selectAdminPage(page,
                req.getStatus(), req.getAuditStatus(), req.getUserId(), req.getKeyword());

        List<DynamicContent> records = result.getRecords();
        List<Long> dynamicIds = records.stream().map(DynamicContent::getId).toList();
        List<Long> authorIds = records.stream().map(DynamicContent::getUserId).distinct().toList();

        Map<Long, List<String>> mediaMap = loadMediaMap(dynamicIds);
        Map<Long, UserProfile> profileMap = loadProfileMap(authorIds);

        List<AdminDynamicResp> items = new ArrayList<>();
        for (DynamicContent d : records) {
            AdminDynamicResp resp = new AdminDynamicResp();
            resp.setDynamicId(d.getId());
            resp.setUserId(d.getUserId());
            UserProfile profile = profileMap.get(d.getUserId());
            resp.setNickname(profile == null ? "" : profile.getNickname());
            resp.setAvatar(profile == null ? "" : profile.getAvatar());
            resp.setContent(d.getContent());
            resp.setMediaUrls(mediaMap.getOrDefault(d.getId(), new ArrayList<>()));
            resp.setStatus(d.getStatus());
            resp.setAuditStatus(d.getAuditStatus());
            resp.setAnonymousFlag(d.getAnonymousFlag());
            resp.setVisibleScope(d.getVisibleScope());
            resp.setLikeCount(d.getLikeCount());
            resp.setCommentCount(d.getCommentCount());
            resp.setCollectCount(d.getCollectCount());
            resp.setReportCount(d.getReportCount());
            resp.setIsDelete(d.getIsDelete());
            resp.setCreateTime(formatTime(d.getCreateTime()));
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AdminDynamicAuditReq req) {
        DynamicContent dynamic = dynamicContentMapper.selectById(req.getDynamicId());
        if (dynamic == null) {
            throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
        }
        boolean pass = Boolean.TRUE.equals(req.getPass());
        int auditStatus = pass ? CommonConstant.DYNAMIC_AUDIT_PASS : CommonConstant.DYNAMIC_AUDIT_REJECT;
        dynamic.setAuditStatus(auditStatus);
        // 拒绝则同时下架
        if (!pass) {
            dynamic.setStatus(CommonConstant.DYNAMIC_STATUS_OFFLINE);
        }
        dynamicContentMapper.updateById(dynamic);

        writeAuditRecord(dynamic, auditStatus, req.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(AdminDynamicOfflineReq req) {
        DynamicContent dynamic = dynamicContentMapper.selectById(req.getDynamicId());
        if (dynamic == null) {
            throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
        }
        dynamic.setStatus(CommonConstant.DYNAMIC_STATUS_OFFLINE);
        dynamicContentMapper.updateById(dynamic);

        writeAuditRecord(dynamic, CommonConstant.AUDIT_STATUS_REJECT,
                req.getReason() == null ? "后台下架" : req.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(AdminDynamicOfflineReq req) {
        DynamicContent dynamic = dynamicContentMapper.selectById(req.getDynamicId());
        if (dynamic == null) {
            throw new BizException(ResultCode.DYNAMIC_NOT_FOUND);
        }
        dynamic.setStatus(CommonConstant.DYNAMIC_STATUS_NORMAL);
        dynamicContentMapper.updateById(dynamic);

        writeAuditRecord(dynamic, CommonConstant.AUDIT_STATUS_PASS,
                req.getReason() == null ? "后台恢复" : req.getReason());
    }

    /**
     * 写审核记录。
     */
    private void writeAuditRecord(DynamicContent dynamic, int auditStatus, String reason) {
        AuditRecord record = new AuditRecord();
        record.setBizType(CommonConstant.AUDIT_BIZ_DYNAMIC);
        record.setBizId(dynamic.getId());
        record.setUserId(dynamic.getUserId());
        record.setAuditStatus(auditStatus);
        record.setAuditReason(reason == null ? "" : reason);
        record.setAuditorId(currentOperatorId());
        record.setAuditTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }

    private Long currentOperatorId() {
        Long userId = UserContext.getUserId();
        return userId == null ? 0L : userId;
    }

    private Map<Long, List<String>> loadMediaMap(List<Long> dynamicIds) {
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return new HashMap<>();
        }
        List<DynamicMedia> mediaList = dynamicMediaMapper.selectList(
                new LambdaQueryWrapper<DynamicMedia>()
                        .in(DynamicMedia::getDynamicId, dynamicIds)
                        .orderByAsc(DynamicMedia::getSort));
        Map<Long, List<String>> map = new HashMap<>();
        for (DynamicMedia media : mediaList) {
            map.computeIfAbsent(media.getDynamicId(), k -> new ArrayList<>()).add(media.getMediaUrl());
        }
        return map;
    }

    private Map<Long, UserProfile> loadProfileMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<UserProfile> profiles = userProfileMapper.selectList(
                new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, userIds));
        return profiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }
}
