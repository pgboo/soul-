package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminUserBanReq;
import com.soul.social.dto.req.AdminUserDetailReq;
import com.soul.social.dto.req.AdminUserListReq;
import com.soul.social.dto.req.AdminUserMuteReq;
import com.soul.social.dto.resp.AdminUserDetailResp;
import com.soul.social.dto.resp.AdminUserResp;

/**
 * 后台用户管理服务（P0，治理必需）。
 */
public interface AdminUserService {

    /**
     * 用户列表：支持昵称/手机号/状态搜索，分页。
     * <p>手机号一律脱敏返回。
     */
    PageResult<AdminUserResp> userPage(AdminUserListReq req);

    /**
     * 用户详情：含资料与风险记录；手机号脱敏。
     */
    AdminUserDetailResp userDetail(AdminUserDetailReq req);

    /**
     * 禁言 / 解除禁言（R002）：写 user.mute_end_time + risk_user_record。
     */
    void mute(AdminUserMuteReq req);

    /**
     * 封禁 / 解封（R001）：改 user.status + risk_user_record。
     */
    void ban(AdminUserBanReq req);
}
