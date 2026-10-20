package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台新增/编辑标签分类请求。
 */
@Data
public class AdminTagCategorySaveReq {

    /** 可选，编辑时传 */
    private Long categoryId;

    @NotBlank(message = "分类名不能为空")
    private String name;

    private String icon;

    private Integer sort;

    private Integer status;
}
