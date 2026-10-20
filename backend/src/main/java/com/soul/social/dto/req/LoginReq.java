package com.soul.social.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求。
 */
@Data
public class LoginReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
