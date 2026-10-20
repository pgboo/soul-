package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.PrivacyUpdateReq;
import com.soul.social.entity.UserPrivacySetting;
import com.soul.social.mapper.UserPrivacySettingMapper;
import com.soul.social.service.PrivacyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 隐私设置服务实现。
 */
@Slf4j
@Service
public class PrivacyServiceImpl implements PrivacyService {

    @Resource
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Override
    public void update(PrivacyUpdateReq req) {
        Long userId = UserContext.requireUserId();
        UserPrivacySetting setting = userPrivacySettingMapper.selectOne(
                new LambdaQueryWrapper<UserPrivacySetting>()
                        .eq(UserPrivacySetting::getUserId, userId)
                        .last("LIMIT 1"));
        if (setting == null) {
            setting = new UserPrivacySetting();
            setting.setUserId(userId);
            setting.setShowAge(CommonConstant.YES);
            setting.setShowCity(CommonConstant.YES);
            setting.setShowOnline(CommonConstant.YES);
            setting.setAllowRecommend(CommonConstant.YES);
            setting.setAllowStrangerMsg(CommonConstant.YES);
            applyUpdates(setting, req);
            userPrivacySettingMapper.insert(setting);
            return;
        }
        applyUpdates(setting, req);
        userPrivacySettingMapper.updateById(setting);
    }

    /**
     * 仅覆盖非空字段。
     */
    private void applyUpdates(UserPrivacySetting setting, PrivacyUpdateReq req) {
        if (req.getShowAge() != null) {
            setting.setShowAge(boolToInt(req.getShowAge()));
        }
        if (req.getShowCity() != null) {
            setting.setShowCity(boolToInt(req.getShowCity()));
        }
        if (req.getShowOnline() != null) {
            setting.setShowOnline(boolToInt(req.getShowOnline()));
        }
        if (req.getAllowRecommend() != null) {
            setting.setAllowRecommend(boolToInt(req.getAllowRecommend()));
        }
        if (req.getAllowStrangerMsg() != null) {
            setting.setAllowStrangerMsg(boolToInt(req.getAllowStrangerMsg()));
        }
    }

    private int boolToInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? CommonConstant.YES : CommonConstant.NO;
    }
}
