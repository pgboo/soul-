package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台新增/编辑标签请求。
 */
@Data
public class AdminTagSaveReq {

    /** 可选，编辑时传 */
    private Long tagId;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "标签名不能为空")
    private String name;

    private Integer hotFlag;

    private Integer matchFlag;

    private Integer recommendFlag;

    private Integer sort;

    private Integer status;
}
