package com.soul.social.dto.req;

import lombok.Data;

/**
 * 更新隐私设置请求。字段均可选。
 */
@Data
public class PrivacyUpdateReq {

    private Boolean showAge;

    private Boolean showCity;

    private Boolean showOnline;

    private Boolean allowRecommend;

    private Boolean allowStrangerMsg;
}
