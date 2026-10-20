package com.soul.social.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户资料响应。**不含任何敏感字段**。
 */
@Data
public class UserProfileResp {

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String city;

    private String sign;

    private String intro;

    private Integer emotionStatus;

    private Integer friendGoal;

    private String soulId;

    private Integer completePercent;

    private Integer nicknameModifyCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
