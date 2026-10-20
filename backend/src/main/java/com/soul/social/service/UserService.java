package com.soul.social.service;

import com.soul.social.dto.req.ProfileCompleteReq;
import com.soul.social.dto.req.ProfileUpdateReq;
import com.soul.social.dto.resp.UserHomeResp;
import com.soul.social.dto.resp.UserInitStatusResp;
import com.soul.social.dto.resp.UserProfileResp;

/**
 * 用户资料 / 初始化状态服务。
 */
public interface UserService {

    /** 初始化状态（读 user 三个 flag）。 */
    UserInitStatusResp initStatus();

    /** 我的资料。 */
    UserProfileResp getMyProfile();

    /** 完善资料。 */
    UserProfileResp completeProfile(ProfileCompleteReq req);

    /** 编辑资料。 */
    UserProfileResp updateProfile(ProfileUpdateReq req);

    /** 他人主页（隐私过滤）。 */
    UserHomeResp getUserHome(Long userId);
}
