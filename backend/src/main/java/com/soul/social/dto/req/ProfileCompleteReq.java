package com.soul.social.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 完善资料请求。
 */
@Data
public class ProfileCompleteReq {

    /** 头像（选填；为空时后端按用户 ID 分配固定默认头像） */
    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 16, message = "昵称长度须为 1-16 字")
    private String nickname;

    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别非法")
    @Max(value = 2, message = "性别非法")
    private Integer gender;

    @NotBlank(message = "生日不能为空")
    private String birthday;

    @NotBlank(message = "城市不能为空")
    private String city;

    @Size(max = 50, message = "签名不能超过 50 字")
    private String sign;
}
