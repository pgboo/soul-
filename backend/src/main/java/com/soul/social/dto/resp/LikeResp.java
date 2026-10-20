package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 点赞响应。
 */
@Data
public class LikeResp {

    private Boolean liked;

    private Integer likeCount;

    public LikeResp() {
    }

    public LikeResp(Boolean liked, Integer likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }
}
