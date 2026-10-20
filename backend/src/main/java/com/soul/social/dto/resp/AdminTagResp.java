package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 后台标签响应（含分类名）。
 */
@Data
public class AdminTagResp {

    private Long tagId;

    private Long categoryId;

    private String categoryName;

    private String name;

    private Integer hotFlag;

    private Integer matchFlag;

    private Integer recommendFlag;

    private Integer sort;

    private Integer status;
}
