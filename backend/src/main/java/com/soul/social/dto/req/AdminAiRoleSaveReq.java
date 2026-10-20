package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台新增/编辑 AI 角色请求。
 */
@Data
public class AdminAiRoleSaveReq {

    /** 可选，编辑时传 */
    private Long roleId;

    @NotBlank(message = "角色名不能为空")
    private String name;

    /** 头像 URL */
    private String avatar;

    /** 欢迎语 */
    private String welcomeText;

    /** 角色 Prompt */
    private String prompt;

    /** 状态:0禁用 1启用 */
    private Integer status;

    /** 排序（小在前） */
    private Integer sort;
}
