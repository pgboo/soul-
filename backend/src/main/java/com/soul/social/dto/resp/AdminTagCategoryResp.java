package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台标签分类响应。
 */
@Data
public class AdminTagCategoryResp {

    private Long categoryId;

    private String name;

    private String icon;

    private Integer sort;

    private Integer status;
}
