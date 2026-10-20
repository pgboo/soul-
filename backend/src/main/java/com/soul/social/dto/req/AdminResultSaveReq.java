package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台保存灵魂测试结果类型。
 */
@Data
public class AdminResultSaveReq {

    /** 可选，编辑时传 */
    private Long resultId;

    @NotBlank(message = "结果编码不能为空")
    private String resultCode;

    @NotBlank(message = "结果名称不能为空")
    private String name;

    private String icon;

    private String description;

    private String socialAdvice;
}
