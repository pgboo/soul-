package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台处理举报请求（R011）。
 * <p>{@code handleType} 决定对目标的真实处置动作。
 */
@Data
public class AdminReportHandleReq {

    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    /**
     * 处理动作：
     * reject=驳回（仅标记，不动目标）；
     * offline=下架目标动态；
     * mute=禁言被举报用户；
     * ban=封禁被举报用户。
     */
    @NotBlank(message = "处理动作不能为空")
    private String handleType;

    /** 禁言天数（handleType=mute 时有效；为 null 表示永久禁言） */
    private Integer muteDays;

    @NotBlank(message = "处理说明不能为空")
    private String handleResult;
}
