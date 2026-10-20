package com.soul.social.service;

/**
 * 系统配置读取服务。
 */
public interface SysConfigService {

    /**
     * 取配置值，不存在返回 null。
     */
    String getValue(String configKey);

    /**
     * 取配置值，不存在返回默认值。
     */
    String getValue(String configKey, String defaultValue);

    /**
     * 取整型配置值。
     */
    int getInt(String configKey, int defaultValue);

    /**
     * 短信是否启用真实通道（true=真实短信，false=开发模式固定码）。
     */
    boolean smsEnabled();
}
