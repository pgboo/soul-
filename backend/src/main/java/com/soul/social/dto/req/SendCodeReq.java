package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送验证码请求。
 */
@Data
public class SendCodeReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号格式错误")
    private String phone;
}
