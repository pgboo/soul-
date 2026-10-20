package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台新增/编辑敏感词请求。
 */
@Data
public class AdminSensitiveWordSaveReq {

    /** 可选，编辑时传 */
    private Long wordId;

    @NotBlank(message = "敏感词不能为空")
    private String word;

    /** 分类 */
    private String category;

    /** 级别:1替换 2拦截 */
    private Integer level;

    /** 状态:0禁用 1启用 */
    private Integer status;
}
