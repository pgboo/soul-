package com.soul.social.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑资料请求。字段均可选，仅更新非空字段。
 */
@Data
public class ProfileUpdateReq {

    private String avatar;

    @Size(max = 16, message = "昵称不能超过 16 字")
    private String nickname;

    private String city;

    @Size(max = 50, message = "签名不能超过 50 字")
    private String sign;

    @Size(max = 200, message = "简介不能超过 200 字")
    private String intro;

    private Integer emotionStatus;

    private Integer friendGoal;
}
