package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 推荐用户响应。
 */
@Data
public class MatchUserResp {

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer age;

    private String city;

    private List<String> tags;

    /** 匹配度 0-100 */
    private Integer matchScore;

    /** 推荐理由 */
    private String reason;
}
