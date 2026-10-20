package com.soul.social.service;

import com.soul.social.dto.req.PrivacyUpdateReq;

/**
 * 隐私设置服务。
 */
public interface PrivacyService {

    /** 更新隐私设置。 */
    void update(PrivacyUpdateReq req);
}
