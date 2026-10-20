package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 撤回消息请求。
 */
@Data
public class MessageRecallReq {

    @NotBlank(message = "消息标识不能为空")
    private String messageUuid;
}