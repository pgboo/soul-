package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台禁言/解除禁言请求（R002）。
 * <p>{@code muteEndTime} 为空表示解除禁言；有值表示禁言至该时间。
 */
@Data
public class AdminUserMuteReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** true=禁言 false=解除禁言 */
    @NotNull(message = "操作类型不能为空")
    private Boolean mute;

    /** 禁言天数（>0；为 null 表示永久禁言），仅 mute=true 时有效 */
    private Integer muteDays;

    @NotBlank(message = "原因不能为空")
    private String reason;
}
