package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台瞬间审核请求（R007）。
 */
@Data
public class AdminDynamicAuditReq {

    @NotNull(message = "动态ID不能为空")
    private Long dynamicId;

    /** 审核结果:true=通过 false=拒绝 */
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    /** 审核原因（拒绝时建议填写） */
    private String reason;
}
