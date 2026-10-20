package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台用户列表项响应。
 * <p><b>手机号一律脱敏</b>（如 138****8000），任何情况下不返回明文。
 */
@Data
public class AdminUserResp {

    private Long userId;

    private String userNo;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 性别:0未知 1男 2女 */
    private Integer gender;

    /** 脱敏手机号（如 138****8000） */
    private String phoneMask;

    /** 状态:1正常 2封禁 */
    private Integer status;

    /** 风控等级:0正常 1观察 */
    private Integer riskLevel;

    /** 禁言截止时间（字符串，null=未禁言） */
    private String muteEndTime;

    /** 资料完善:0否 1是 */
    private Integer profileCompleteFlag;

    /** 标签完成:0否 1是 */
    private Integer tagFlag;

    /** 测试完成:0否 1是 */
    private Integer testFlag;

    private String createTime;
}
