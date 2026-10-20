package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.AdminReportHandleReq;
import com.soul.social.dto.req.AdminReportListReq;
import com.soul.social.dto.resp.AdminReportResp;
import com.soul.social.entity.AuditRecord;
import com.soul.social.entity.DynamicComment;
import com.soul.social.entity.DynamicContent;
import com.soul.social.entity.ReportRecord;
import com.soul.social.entity.RiskUserRecord;
import com.soul.social.entity.User;
import com.soul.social.entity.UserProfile;
import com.soul.social.mapper.AuditRecordMapper;
import com.soul.social.mapper.DynamicCommentMapper;
import com.soul.social.mapper.DynamicContentMapper;
import com.soul.social.mapper.ReportRecordMapper;
import com.soul.social.mapper.RiskUserRecordMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.service.AdminReportService;
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
 * 后台举报管理服务实现。
 * <p>处理举报时真实影响目标对象状态（下架动态 / 禁言 / 封禁），并落 audit_record。
 */
@Slf4j
@Service
public class AdminReportServiceImpl implements AdminReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 处理动作：驳回 */
    private static final String HANDLE_REJECT = "reject";
    /** 处理动作：下架 */
    private static final String HANDLE_OFFLINE = "offline";
    /** 处理动作：禁言 */
    private static final String HANDLE_MUTE = "mute";
    /** 处理动作：封禁 */
    private static final String HANDLE_BAN = "ban";

    /** 摘要截断长度 */
    private static final int SUMMARY_MAX_LEN = 50;

    @Resource
    private ReportRecordMapper reportRecordMapper;

    @Resource
    private DynamicContentMapper dynamicContentMapper;

    @Resource
    private DynamicCommentMapper dynamicCommentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private RiskUserRecordMapper riskUserRecordMapper;

    @Resource
    private AuditRecordMapper auditRecordMapper;

    @Override
    public PageResult<AdminReportResp> reportPage(AdminReportListReq req) {
        Page<ReportRecord> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<ReportRecord> result = reportRecordMapper.selectPage(page,
                new LambdaQueryWrapper<ReportRecord>()
                        .eq(req.getStatus() != null, ReportRecord::getStatus, req.getStatus())
                        .eq(req.getTargetType() != null, ReportRecord::getTargetType, req.getTargetType())
                        .orderByDesc(ReportRecord::getId));

        List<ReportRecord> records = result.getRecords();
        // 举报人昵称
        List<Long> reporterIds = records.stream().map(ReportRecord::getReporterId).distinct().toList();
        Map<Long, UserProfile> profileMap = loadProfileMap(reporterIds);

        // 目标摘要（按类型分组批量查询）
        Map<Long, DynamicContent> dynamicMap = loadDynamicMap(records);
        Map<Long, DynamicComment> commentMap = loadCommentMap(records);

        List<AdminReportResp> items = new ArrayList<>();
        for (ReportRecord r : records) {
            AdminReportResp resp = new AdminReportResp();
            resp.setReportId(r.getId());
            resp.setReporterId(r.getReporterId());
            UserProfile reporter = profileMap.get(r.getReporterId());
            resp.setReporterNickname(reporter == null ? "" : reporter.getNickname());
            resp.setTargetType(r.getTargetType());
            resp.setTargetId(r.getTargetId());
            resp.setTargetSummary(buildTargetSummary(r, dynamicMap, commentMap, profileMap));
            resp.setReasonType(r.getReasonType());
            resp.setReason(r.getReason());
            resp.setStatus(r.getStatus());
            resp.setHandleResult(r.getHandleResult());
            resp.setHandlerId(r.getHandlerId());
            resp.setHandleTime(formatTime(r.getHandleTime()));
            resp.setCreateTime(formatTime(r.getCreateTime()));
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(AdminReportHandleReq req) {
        ReportRecord report = reportRecordMapper.selectById(req.getReportId());
        if (report == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Long operatorId = currentOperatorId();
        LocalDateTime now = LocalDateTime.now();
        String handleType = req.getHandleType() == null ? "" : req.getHandleType().trim().toLowerCase();

        Long targetUserId = resolveTargetUserId(report);

        switch (handleType) {
            case HANDLE_REJECT -> report.setStatus(CommonConstant.REPORT_STATUS_REJECT);
            case HANDLE_OFFLINE -> {
                offlineTarget(report);
                report.setStatus(CommonConstant.REPORT_STATUS_HANDLED);
            }
            case HANDLE_MUTE -> {
                muteTargetUser(targetUserId, req.getMuteDays(), req.getHandleResult(), operatorId, now);
                report.setStatus(CommonConstant.REPORT_STATUS_HANDLED);
            }
            case HANDLE_BAN -> {
                banTargetUser(targetUserId, req.getHandleResult(), operatorId, now);
                report.setStatus(CommonConstant.REPORT_STATUS_HANDLED);
            }
            default -> throw new BizException(ResultCode.PARAM_ERROR, "处理动作非法");
        }

        report.setHandleResult(req.getHandleResult());
        report.setHandlerId(operatorId);
        report.setHandleTime(now);
        reportRecordMapper.updateById(report);

        // 审计记录：举报处理 → biz_type=评论(2) 兜底，biz_id=举报ID
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setBizType(report.getTargetType() != null && report.getTargetType() == CommonConstant.REPORT_TARGET_DYNAMIC
                ? CommonConstant.AUDIT_BIZ_DYNAMIC : CommonConstant.AUDIT_BIZ_COMMENT);
        auditRecord.setBizId(report.getTargetId());
        auditRecord.setUserId(targetUserId == null ? 0L : targetUserId);
        auditRecord.setAuditStatus(CommonConstant.REPORT_STATUS_HANDLED == report.getStatus()
                ? CommonConstant.AUDIT_STATUS_REJECT : CommonConstant.AUDIT_STATUS_PASS);
        auditRecord.setAuditReason("举报处理[" + handleType + "]：" + req.getHandleResult());
        auditRecord.setAuditorId(operatorId);
        auditRecord.setAuditTime(now);
        auditRecordMapper.insert(auditRecord);
    }

    /**
     * 解析举报目标所属用户 ID（动态作者 / 评论作者 / 目标用户）。
     */
    private Long resolveTargetUserId(ReportRecord report) {
        if (report.getTargetType() == null) {
            return null;
        }
        switch (report.getTargetType()) {
            case CommonConstant.REPORT_TARGET_DYNAMIC -> {
                DynamicContent dynamic = dynamicContentMapper.selectById(report.getTargetId());
                return dynamic == null ? null : dynamic.getUserId();
            }
            case CommonConstant.REPORT_TARGET_COMMENT -> {
                DynamicComment comment = dynamicCommentMapper.selectById(report.getTargetId());
                return comment == null ? null : comment.getUserId();
            }
            case CommonConstant.REPORT_TARGET_USER -> {
                return report.getTargetId();
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * 下架被举报的动态/评论。
     */
    private void offlineTarget(ReportRecord report) {
        if (report.getTargetType() == null) {
            return;
        }
        if (report.getTargetType() == CommonConstant.REPORT_TARGET_DYNAMIC) {
            DynamicContent dynamic = dynamicContentMapper.selectById(report.getTargetId());
            if (dynamic != null) {
                dynamic.setStatus(CommonConstant.DYNAMIC_STATUS_OFFLINE);
                dynamicContentMapper.updateById(dynamic);
            }
        } else if (report.getTargetType() == CommonConstant.REPORT_TARGET_COMMENT) {
            DynamicComment comment = dynamicCommentMapper.selectById(report.getTargetId());
            if (comment != null) {
                comment.setStatus(2);
                dynamicCommentMapper.updateById(comment);
            }
        }
    }

    /**
     * 禁言被举报用户。
     */
    private void muteTargetUser(Long targetUserId, Integer muteDays, String reason, Long operatorId, LocalDateTime now) {
        if (targetUserId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "无法定位被举报用户");
        }
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        LocalDateTime muteEnd = (muteDays == null || muteDays <= 0)
                ? now.plusYears(100L) : now.plusDays(muteDays);
        user.setMuteEndTime(muteEnd);
        userMapper.updateById(user);

        RiskUserRecord record = new RiskUserRecord();
        record.setUserId(targetUserId);
        record.setRiskType(CommonConstant.RISK_TYPE_MUTE);
        record.setReason(reason);
        record.setStartTime(now);
        record.setEndTime(muteEnd);
        record.setOperatorId(operatorId);
        record.setStatus(CommonConstant.RISK_STATUS_ACTIVE);
        riskUserRecordMapper.insert(record);
    }

    /**
     * 封禁被举报用户。
     */
    private void banTargetUser(Long targetUserId, String reason, Long operatorId, LocalDateTime now) {
        if (targetUserId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "无法定位被举报用户");
        }
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        user.setStatus(CommonConstant.USER_STATUS_BANNED);
        userMapper.updateById(user);

        RiskUserRecord record = new RiskUserRecord();
        record.setUserId(targetUserId);
        record.setRiskType(CommonConstant.RISK_TYPE_BAN);
        record.setReason(reason);
        record.setStartTime(now);
        record.setEndTime(null);
        record.setOperatorId(operatorId);
        record.setStatus(CommonConstant.RISK_STATUS_ACTIVE);
        riskUserRecordMapper.insert(record);
    }

    /**
     * 构造目标摘要。
     */
    private String buildTargetSummary(ReportRecord report, Map<Long, DynamicContent> dynamicMap,
                                      Map<Long, DynamicComment> commentMap, Map<Long, UserProfile> profileMap) {
        if (report.getTargetType() == null) {
            return "";
        }
        switch (report.getTargetType()) {
            case CommonConstant.REPORT_TARGET_DYNAMIC -> {
                DynamicContent dynamic = dynamicMap.get(report.getTargetId());
                return dynamic == null ? "[瞬间已删除]" : truncate(dynamic.getContent());
            }
            case CommonConstant.REPORT_TARGET_COMMENT -> {
                DynamicComment comment = commentMap.get(report.getTargetId());
                return comment == null ? "[评论已删除]" : truncate(comment.getContent());
            }
            case CommonConstant.REPORT_TARGET_USER -> {
                UserProfile profile = profileMap.get(report.getTargetId());
                return profile == null ? "[用户]" : "用户：" + profile.getNickname();
            }
            default -> {
                return "";
            }
        }
    }

    private String truncate(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        return text.length() <= SUMMARY_MAX_LEN ? text : text.substring(0, SUMMARY_MAX_LEN) + "...";
    }

    private Map<Long, DynamicContent> loadDynamicMap(List<ReportRecord> records) {
        List<Long> ids = records.stream()
                .filter(r -> r.getTargetType() != null
                        && r.getTargetType() == CommonConstant.REPORT_TARGET_DYNAMIC)
                .map(ReportRecord::getTargetId).distinct().toList();
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        List<DynamicContent> list = dynamicContentMapper.selectBatchIds(ids);
        return list.stream().collect(Collectors.toMap(DynamicContent::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, DynamicComment> loadCommentMap(List<ReportRecord> records) {
        List<Long> ids = records.stream()
                .filter(r -> r.getTargetType() != null
                        && r.getTargetType() == CommonConstant.REPORT_TARGET_COMMENT)
                .map(ReportRecord::getTargetId).distinct().toList();
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        List<DynamicComment> list = dynamicCommentMapper.selectBatchIds(ids);
        return list.stream().collect(Collectors.toMap(DynamicComment::getId, Function.identity(), (a, b) -> a));
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

    private Long currentOperatorId() {
        Long userId = UserContext.getUserId();
        return userId == null ? 0L : userId;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }
}
