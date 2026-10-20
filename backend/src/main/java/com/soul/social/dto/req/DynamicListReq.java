package com.soul.social.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 瞬间列表请求。频道：recommend(默认)/latest/city。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DynamicListReq extends PageQuery {

    private String channel;

    private Long topicId;
}
