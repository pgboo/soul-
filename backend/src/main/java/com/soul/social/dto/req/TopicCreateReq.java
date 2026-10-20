package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 自拟话题（用户自建话题）请求。
 */
@Data
public class TopicCreateReq {

    /** 话题名，可带前导 #，服务端会清理 */
    @NotBlank(message = "话题名不能为空")
    @Size(max = 20, message = "话题名不能超过 20 个字")
    private String name;
}
