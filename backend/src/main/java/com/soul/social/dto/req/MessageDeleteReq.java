package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 删除消息请求（仅对自己不可见）。
 */
@Data
public class MessageDeleteReq {

    @NotBlank(message = "消息标识不能为空")
    private String messageUuid;
}