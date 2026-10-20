package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 瞬间详情响应。
 */
@Data
public class DynamicDetailResp {

    private Long dynamicId;

    private DynamicItemResp.AuthorItem author;

    private String content;

    private List<String> mediaUrls;

    private String topicName;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Boolean liked;

    private Boolean collected;

    private Integer auditStatus;

    /** 可见范围:1公开 2仅好友 3仅自己 */
    private Integer visibleScope;

    /** 是否作者本人 */
    private Boolean isAuthor;

    /** 是否可删除（本人） */
    private Boolean canDelete;

    private List<CommentItemResp> comments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
