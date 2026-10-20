package com.soul.social.dto.req;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 发布瞬间请求。
 */
@Data
public class DynamicPublishReq {

    @Size(max = 500, message = "正文不能超过 500 字")
    private String content;

    /** 图片 URL 列表，≤9 张 */
    private List<String> mediaUrls;

    /** 话题 ID */
    private Long topicId;

    /** 匿名 */
    private Boolean anonymousFlag;

    /** 可见范围:1公开 2仅好友 3仅自己 */
    private Integer visibleScope;
}
