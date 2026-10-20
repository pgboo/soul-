package com.soul.social.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.client.CacheService;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.constant.RedisKeyConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.dto.req.LoginReq;
import com.soul.social.dto.req.SendCodeReq;
import com.soul.social.dto.resp.AdminLoginResp;
import com.soul.social.dto.resp.LoginResp;
import com.soul.social.entity.User;
import com.soul.social.entity.UserLoginLog;
import com.soul.social.entity.UserPrivacySetting;
import com.soul.social.entity.UserProfile;
import com.soul.social.entity.UserSensitiveInfo;
import com.soul.social.mapper.UserLoginLogMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserPrivacySettingMapper;
import com.soul.social.mapper.UserProfileMapper;
import com.soul.social.mapper.UserSensitiveInfoMapper;
import com.soul.social.security.JwtUtil;
import com.soul.social.security.PasswordUtil;
import com.soul.social.service.AuthService;
import com.soul.social.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号服务实现。
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserSensitiveInfoMapper userSensitiveInfoMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Resource
    private UserLoginLogMapper userLoginLogMapper;

    @Resource
    private CacheService cacheService;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PasswordUtil passwordUtil;

    @Override
    public void sendCode(SendCodeReq req) {
        String phone = req.getPhone();
        // 60s 内重复发送 → 1007
        if (cacheService.hasKey(RedisKeyConstant.SMS_LIMIT_PREFIX + phone)) {
            throw new BizException(ResultCode.CODE_TOO_OFTEN);
        }
        String code;
        if (sysConfigService.smsEnabled()) {
            // 真实短信通道（本期不接入，仅占位）
            code = IdUtil.fastSimpleUUID().substring(0, 6);
            log.info("【真实短信通道】向 {} 发送验证码（通道未接入）", phone);
        } else {
            // 开发模式固定验证码
            code = CommonConstant.DEV_FIXED_SMS_CODE;
            log.info("【开发模式】手机号 {} 的验证码为 {}（固定码）", phone, code);
        }
        cacheService.set(RedisKeyConstant.SMS_CODE_PREFIX + phone, code, CommonConstant.SMS_CODE_TTL_SECONDS);
        cacheService.set(RedisKeyConstant.SMS_LIMIT_PREFIX + phone, "1", CommonConstant.SMS_CODE_SEND_INTERVAL_SECONDS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp login(LoginReq req, String clientIp) {
        String phone = req.getPhone();
        String inputCode = req.getCode();

        // 1. 校验验证码
        String cacheCode = cacheService.get(RedisKeyConstant.SMS_CODE_PREFIX + phone);
        if (StrUtil.isBlank(cacheCode)) {
            throw new BizException(ResultCode.CODE_EXPIRED);
        }
        if (!cacheCode.equals(inputCode)) {
            throw new BizException(ResultCode.CODE_ERROR);
        }
        // 验证通过后清除验证码
        cacheService.delete(RedisKeyConstant.SMS_CODE_PREFIX + phone);

        // 2. 查敏感表定位用户
        UserSensitiveInfo sensitive = userSensitiveInfoMapper.selectOne(
                new LambdaQueryWrapper<UserSensitiveInfo>()
                        .eq(UserSensitiveInfo::getPhone, phone)
                        .last("LIMIT 1"));

        Long userId;
        if (sensitive == null) {
            // 3. 新用户自动注册
            userId = registerNewUser(phone);
        } else {
            userId = sensitive.getUserId();
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BizException(ResultCode.UNAUTHORIZED);
            }
            // 4. 封禁校验
            if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_BANNED) {
                log.warn("封禁用户尝试登录: userId={}", userId);
                writeLoginLog(userId, clientIp, CommonConstant.NO);
                throw new BizException(ResultCode.USER_BANNED);
            }
            // 更新最后活跃时间
            User update = new User();
            update.setId(userId);
            update.setLastActiveTime(java.time.LocalDateTime.now());
            userMapper.updateById(update);
        }

        // 5. 写登录日志
        writeLoginLog(userId, clientIp, CommonConstant.YES);

        // 6. 组装响应
        User user = userMapper.selectById(userId);
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("LIMIT 1"));

        LoginResp resp = new LoginResp();
        resp.setToken(jwtUtil.generateMiniToken(userId));
        resp.setUserId(userId);
        resp.setNickname(profile == null ? "" : profile.getNickname());
        resp.setAvatar(profile == null ? "" : profile.getAvatar());
        resp.setProfileComplete(flag(user.getProfileCompleteFlag()));
        resp.setTagComplete(flag(user.getTagFlag()));
        resp.setTestComplete(flag(user.getTestFlag()));
        return resp;
    }

    @Override
    public void logout(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }
        long remaining = jwtUtil.getRemainingSeconds(token);
        if (remaining > 0) {
            cacheService.set(RedisKeyConstant.LOGOUT_TOKEN_PREFIX + token, "1", remaining);
        }
    }

    @Override
    public AdminLoginResp adminLogin(String username, String password) {
        // 后台账号：手机号字段存唯一标识（如 admin）
        UserSensitiveInfo sensitive = userSensitiveInfoMapper.selectOne(
                new LambdaQueryWrapper<UserSensitiveInfo>()
                        .eq(UserSensitiveInfo::getPhone, username)
                        .last("LIMIT 1"));
        if (sensitive == null || !passwordUtil.matches(password, sensitive.getPassword())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "账号或密码错误");
        }
        Long userId = sensitive.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_BANNED) {
            throw new BizException(ResultCode.USER_BANNED);
        }
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
                        .last("LIMIT 1"));

        AdminLoginResp resp = new AdminLoginResp();
        resp.setToken(jwtUtil.generateAdminToken(userId));
        resp.setUserId(userId);
        resp.setNickname(profile == null ? "" : profile.getNickname());
        return resp;
    }

    /**
     * 注册新用户：写 user + user_sensitive_info + 默认 user_profile + user_privacy_setting。
     */
    private Long registerNewUser(String phone) {
        User user = new User();
        user.setUserNo("S" + IdUtil.getSnowflakeNextIdStr());
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        user.setRiskLevel(0);
        user.setProfileCompleteFlag(CommonConstant.NO);
        user.setTagFlag(CommonConstant.NO);
        user.setTestFlag(CommonConstant.NO);
        user.setLastActiveTime(java.time.LocalDateTime.now());
        userMapper.insert(user);
        Long userId = user.getId();

        UserSensitiveInfo sensitive = new UserSensitiveInfo();
        sensitive.setUserId(userId);
        sensitive.setPhone(phone);
        // openid/unionid 未绑定时必须留 NULL：唯一索引允许多个 NULL，但只允许一个 ''，
        // 若写 '' 会导致第二个注册用户撞 uk_openid 报 DuplicateKeyException。
        sensitive.setOpenid(null);
        sensitive.setUnionid(null);
        sensitive.setPassword("");
        userSensitiveInfoMapper.insert(sensitive);

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNickname("用户" + userId);
        profile.setAvatar("");
        profile.setGender(0);
        profile.setCity("");
        profile.setSign("");
        profile.setEmotionStatus(0);
        profile.setFriendGoal(0);
        profile.setSoulId("S" + userId);
        profile.setCompletePercent(0);
        profile.setNicknameModifyCount(0);
        userProfileMapper.insert(profile);

        UserPrivacySetting privacy = new UserPrivacySetting();
        privacy.setUserId(userId);
        privacy.setShowAge(CommonConstant.YES);
        privacy.setShowCity(CommonConstant.YES);
        privacy.setShowOnline(CommonConstant.YES);
        privacy.setAllowRecommend(CommonConstant.YES);
        privacy.setAllowStrangerMsg(CommonConstant.YES);
        userPrivacySettingMapper.insert(privacy);

        log.info("新用户注册成功: userId={}, phone={}", userId, phone);
        return userId;
    }

    private void writeLoginLog(Long userId, String clientIp, int status) {
        UserLoginLog logRecord = new UserLoginLog();
        logRecord.setUserId(userId);
        logRecord.setLoginType(1);
        logRecord.setLoginIp(StrUtil.blankToDefault(clientIp, ""));
        logRecord.setDeviceInfo("");
        logRecord.setLoginStatus(status);
        userLoginLogMapper.insert(logRecord);
    }

    private Boolean flag(Integer value) {
        return value != null && value == CommonConstant.YES;
    }
}
