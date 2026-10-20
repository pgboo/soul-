package com.soul.social.dto.resp;

import lombok.Data;

/**
 * 话题列表项响应。
 */
@Data
public class TopicItemResp {

    private Long topicId;

    private String name;

    private String description;

    private String cover;

    private Integer hotValue;
}
