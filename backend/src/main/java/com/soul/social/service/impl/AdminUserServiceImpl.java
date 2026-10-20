package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.AdminUserBanReq;
import com.soul.social.dto.req.AdminUserDetailReq;
import com.soul.social.dto.req.AdminUserListReq;
import com.soul.social.dto.req.AdminUserMuteReq;
import com.soul.social.dto.resp.AdminUserDetailResp;
import com.soul.social.dto.resp.AdminUserResp;
import com.soul.social.entity.RiskUserRecord;
import com.soul.social.entity.User;
import com.soul.social.entity.UserProfile;
import com.soul.social.entity.UserSensitiveInfo;
import com.soul.social.mapper.RiskUserRecordMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.mapper.UserSensitiveInfoMapper;
import com.soul.social.service.AdminUserService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台用户管理服务实现。
 * <p>约束：手机号一律脱敏返回，不返回明文；治理动作落库到 user 与 risk_user_record。
 */
@Slf4j
@Service
public class AdminUserServiceImpl implements AdminUserService {

    /** 时间格式化 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserSensitiveInfoMapper userSensitiveInfoMapper;

    @Resource
    private RiskUserRecordMapper riskUserRecordMapper;

    @Override
    public PageResult<AdminUserResp> userPage(AdminUserListReq req) {
        // 手机号检索：先按手机号模糊查 user_id 集合，再并入 user 主表条件
        Set<Long> phoneMatchedUserIds = null;
        if (StrUtil.isNotBlank(req.getPhone())) {
            List<UserSensitiveInfo> sensitiveList = userSensitiveInfoMapper.selectList(
                    new LambdaQueryWrapper<UserSensitiveInfo>()
                            .like(UserSensitiveInfo::getPhone, req.getPhone().trim()));
            phoneMatchedUserIds = sensitiveList.stream()
                    .map(UserSensitiveInfo::getUserId)
                    .collect(Collectors.toSet());
            if (phoneMatchedUserIds.isEmpty()) {
                return PageResult.empty((long) req.safePageNum(), (long) req.safePageSize());
            }
        }

        // 昵称检索：先按昵称模糊查 user_id 集合
        Set<Long> nicknameMatchedUserIds = null;
        if (StrUtil.isNotBlank(req.getNickname())) {
            List<UserProfile> profileList = userProfileMapper.selectList(
                    new LambdaQueryWrapper<UserProfile>()
                            .like(UserProfile::getNickname, req.getNickname().trim()));
            nicknameMatchedUserIds = profileList.stream()
                    .map(UserProfile::getUserId)
                    .collect(Collectors.toSet());
            if (nicknameMatchedUserIds.isEmpty()) {
                return PageResult.empty((long) req.safePageNum(), (long) req.safePageSize());
            }
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(req.getStatus() != null, User::getStatus, req.getStatus())
                .in(phoneMatchedUserIds != null, User::getId, phoneMatchedUserIds)
                .in(nicknameMatchedUserIds != null, User::getId, nicknameMatchedUserIds)
                .orderByDesc(User::getId);

        Page<User> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<User> result = userMapper.selectPage(page, wrapper);

        List<Long> userIds = result.getRecords().stream().map(User::getId).toList();
        Map<Long, UserProfile> profileMap = loadProfileMap(userIds);
        Map<Long, String> phoneMap = loadMaskedPhoneMap(userIds);

        List<AdminUserResp> items = new ArrayList<>();
        for (User user : result.getRecords()) {
            AdminUserResp resp = new AdminUserResp();
            resp.setUserId(user.getId());
            resp.setUserNo(user.getUserNo());
            UserProfile profile = profileMap.get(user.getId());
            resp.setNickname(profile == null ? "" : profile.getNickname());
            resp.setAvatar(profile == null ? "" : profile.getAvatar());
            resp.setGender(profile == null ? 0 : profile.getGender());
            resp.setPhoneMask(phoneMap.getOrDefault(user.getId(), ""));
            resp.setStatus(user.getStatus());
            resp.setRiskLevel(user.getRiskLevel());
            resp.setMuteEndTime(formatTime(user.getMuteEndTime()));
            resp.setProfileCompleteFlag(user.getProfileCompleteFlag());
            resp.setTagFlag(user.getTagFlag());
            resp.setTestFlag(user.getTestFlag());
            resp.setCreateTime(formatTime(user.getCreateTime()));
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    public AdminUserDetailResp userDetail(AdminUserDetailReq req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, user.getId()));

        AdminUserDetailResp resp = new AdminUserDetailResp();
        resp.setUserId(user.getId());
        resp.setUserNo(user.getUserNo());
        resp.setStatus(user.getStatus());
        resp.setRiskLevel(user.getRiskLevel());
        resp.setMuteEndTime(formatTime(user.getMuteEndTime()));
        if (profile != null) {
            resp.setNickname(profile.getNickname());
            resp.setAvatar(profile.getAvatar());
            resp.setGender(profile.getGender());
            resp.setBirthday(profile.getBirthday() == null ? null : profile.getBirthday().toString());
            resp.setCity(profile.getCity());
            resp.setSign(profile.getSign());
            resp.setIntro(profile.getIntro());
            resp.setSoulId(profile.getSoulId());
            resp.setCompletePercent(profile.getCompletePercent());
        } else {
            resp.setNickname("");
            resp.setAvatar("");
            resp.setGender(0);
            resp.setCity("");
            resp.setSign("");
            resp.setIntro("");
            resp.setSoulId("");
            resp.setCompletePercent(0);
        }
        resp.setPhoneMask(loadMaskedPhone(user.getId()));

        List<RiskUserRecord> records = riskUserRecordMapper.selectList(
                new LambdaQueryWrapper<RiskUserRecord>()
                        .eq(RiskUserRecord::getUserId, user.getId())
                        .orderByDesc(RiskUserRecord::getId));
        List<AdminUserDetailResp.RiskRecordItem> riskItems = new ArrayList<>();
        for (RiskUserRecord r : records) {
            AdminUserDetailResp.RiskRecordItem item = new AdminUserDetailResp.RiskRecordItem();
            item.setRecordId(r.getId());
            item.setRiskType(r.getRiskType());
            item.setReason(r.getReason());
            item.setStartTime(formatTime(r.getStartTime()));
            item.setEndTime(formatTime(r.getEndTime()));
            item.setOperatorId(r.getOperatorId());
            item.setStatus(r.getStatus());
            item.setCreateTime(formatTime(r.getCreateTime()));
            riskItems.add(item);
        }
        resp.setRiskRecords(riskItems);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mute(AdminUserMuteReq req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Long operatorId = currentOperatorId();
        LocalDateTime now = LocalDateTime.now();

        if (Boolean.TRUE.equals(req.getMute())) {
            // 禁言：muteDays 为 null 表示永久（用远期时间兜底，DB 列非 NOT NULL 但业务视作永久）
            LocalDateTime muteEnd = req.getMuteDays() == null || req.getMuteDays() <= 0
                    ? now.plusYears(100L)
                    : now.plusDays(req.getMuteDays());
            // 使用 UpdateWrapper 显式写值（updateById 会忽略 null，此处非 null 亦可）
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, user.getId())
                    .set(User::getMuteEndTime, muteEnd));

            RiskUserRecord record = new RiskUserRecord();
            record.setUserId(user.getId());
            record.setRiskType(CommonConstant.RISK_TYPE_MUTE);
            record.setReason(req.getReason());
            record.setStartTime(now);
            record.setEndTime(muteEnd);
            record.setOperatorId(operatorId);
            record.setStatus(CommonConstant.RISK_STATUS_ACTIVE);
            riskUserRecordMapper.insert(record);
        } else {
            // 解除禁言：必须用 UpdateWrapper 显式 set NULL（updateById 会忽略 null 字段）
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, user.getId())
                    .set(User::getMuteEndTime, null));

            RiskUserRecord record = new RiskUserRecord();
            record.setUserId(user.getId());
            record.setRiskType(CommonConstant.RISK_TYPE_MUTE);
            record.setReason(req.getReason());
            record.setStartTime(now);
            record.setEndTime(now);
            record.setOperatorId(operatorId);
            record.setStatus(CommonConstant.RISK_STATUS_RELEASED);
            riskUserRecordMapper.insert(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ban(AdminUserBanReq req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Long operatorId = currentOperatorId();
        LocalDateTime now = LocalDateTime.now();

        if (Boolean.TRUE.equals(req.getBan())) {
            user.setStatus(CommonConstant.USER_STATUS_BANNED);
            userMapper.updateById(user);

            RiskUserRecord record = new RiskUserRecord();
            record.setUserId(user.getId());
            record.setRiskType(CommonConstant.RISK_TYPE_BAN);
            record.setReason(req.getReason());
            record.setStartTime(now);
            record.setEndTime(null);
            record.setOperatorId(operatorId);
            record.setStatus(CommonConstant.RISK_STATUS_ACTIVE);
            riskUserRecordMapper.insert(record);
        } else {
            user.setStatus(CommonConstant.USER_STATUS_NORMAL);
            userMapper.updateById(user);

            RiskUserRecord record = new RiskUserRecord();
            record.setUserId(user.getId());
            record.setRiskType(CommonConstant.RISK_TYPE_BAN);
            record.setReason(req.getReason());
            record.setStartTime(now);
            record.setEndTime(now);
            record.setOperatorId(operatorId);
            record.setStatus(CommonConstant.RISK_STATUS_RELEASED);
            riskUserRecordMapper.insert(record);
        }
    }

    /**
     * 当前后台操作人 ID。
     */
    private Long currentOperatorId() {
        Long userId = UserContext.getUserId();
        return userId == null ? 0L : userId;
    }

    /**
     * 批量加载 user_id → UserProfile。
     */
    private Map<Long, UserProfile> loadProfileMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<UserProfile> profiles = userProfileMapper.selectList(
                new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, userIds));
        return profiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量加载 user_id → 脱敏手机号。
     */
    private Map<Long, String> loadMaskedPhoneMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<UserSensitiveInfo> list = userSensitiveInfoMapper.selectList(
                new LambdaQueryWrapper<UserSensitiveInfo>().in(UserSensitiveInfo::getUserId, userIds));
        Map<Long, String> map = new HashMap<>();
        for (UserSensitiveInfo info : list) {
            map.put(info.getUserId(), maskPhone(info.getPhone()));
        }
        return map;
    }

    /**
     * 单个手机号脱敏。
     */
    private String loadMaskedPhone(Long userId) {
        List<UserSensitiveInfo> list = userSensitiveInfoMapper.selectList(
                new LambdaQueryWrapper<UserSensitiveInfo>().eq(UserSensitiveInfo::getUserId, userId));
        if (list.isEmpty()) {
            return "";
        }
        return maskPhone(list.get(0).getPhone());
    }

    /**
     * 手机号脱敏：保留前 3 后 4，中间用 * 号，如 138****8000。
     * <p>非 11 位手机号时，保留首尾各若干位。
     */
    private String maskPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return "";
        }
        String trimmed = phone.trim();
        if (trimmed.length() >= 11) {
            return trimmed.substring(0, 3) + "****" + trimmed.substring(trimmed.length() - 4);
        }
        if (trimmed.length() <= 3) {
            return "***";
        }
        int keep = Math.max(1, trimmed.length() / 4);
        String head = trimmed.substring(0, keep);
        String tail = trimmed.substring(trimmed.length() - keep);
        return head + "****" + tail;
    }

    /**
     * 时间格式化（null 安全）。
     */
    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }
}
