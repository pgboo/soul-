package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 后台瞬间列表项响应。含作者信息与媒体。
 */
@Data
public class AdminDynamicResp {

    private Long dynamicId;

    private Long userId;

    /** 作者昵称 */
    private String nickname;

    /** 作者头像 */
    private String avatar;

    private String content;

    /** 图片 URL 列表 */
    private List<String> mediaUrls;

    /** 状态:1正常 2下架 */
    private Integer status;

    /** 审核状态:0已过审 1待审核 2拒绝 */
    private Integer auditStatus;

    /** 匿名:0否 1是 */
    private Integer anonymousFlag;

    /** 可见范围:1公开 2仅好友 3仅自己 */
    private Integer visibleScope;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Integer reportCount;

    /** 是否已逻辑删除:0否 1是 */
    private Integer isDelete;

    private String createTime;
}
