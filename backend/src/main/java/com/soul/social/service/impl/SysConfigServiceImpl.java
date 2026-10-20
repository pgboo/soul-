package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.constant.SysConfigKey;
import com.soul.social.entity.SystemConfig;
import com.soul.social.mapper.SystemConfigMapper;
import com.soul.social.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统配置读取实现。
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public String getValue(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return null;
        }
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
                        .eq(SystemConfig::getStatus, CommonConstant.STATUS_ENABLE)
                        .last("LIMIT 1"));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public String getValue(String configKey, String defaultValue) {
        String value = getValue(configKey);
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    @Override
    public int getInt(String configKey, int defaultValue) {
        String value = getValue(configKey);
        if (StrUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 {} 值非法: {}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    public boolean smsEnabled() {
        // 默认 0（开发模式）；仅当值为 "1" 时启用真实短信
        return "1".equals(getValue(SysConfigKey.SMS_ENABLE, "0"));
    }
}
