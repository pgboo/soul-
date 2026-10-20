package com.soul.social.controller.mini;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.BlacklistAddReq;
import com.soul.social.dto.req.BlacklistRemoveReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.req.PrivacyUpdateReq;
import com.soul.social.dto.req.ProfileCompleteReq;
import com.soul.social.dto.req.ProfileUpdateReq;
import com.soul.social.dto.resp.BlacklistItemResp;
import com.soul.social.dto.resp.UserHomeResp;
import com.soul.social.dto.resp.UserInitStatusResp;
import com.soul.social.dto.resp.UserProfileResp;
import com.soul.social.service.BlacklistService;
import com.soul.social.service.PrivacyService;
import com.soul.social.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端用户接口 /api/mini/user/**。
 */
@RestController
@RequestMapping("/api/mini/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private PrivacyService privacyService;

    @Resource
    private BlacklistService blacklistService;

    /** 初始化状态 */
    @GetMapping("/initStatus")
    public Result<UserInitStatusResp> initStatus() {
        return Result.success(userService.initStatus());
    }

    /** 我的资料 */
    @GetMapping("/profile")
    public Result<UserProfileResp> profile() {
        return Result.success(userService.getMyProfile());
    }

    /** 完善资料 */
    @PostMapping("/profile/complete")
    public Result<UserProfileResp> completeProfile(@Valid @RequestBody ProfileCompleteReq req) {
        return Result.success("保存成功", userService.completeProfile(req));
    }

    /** 编辑资料 */
    @PostMapping("/profile/update")
    public Result<UserProfileResp> updateProfile(@Valid @RequestBody ProfileUpdateReq req) {
        return Result.success("保存成功", userService.updateProfile(req));
    }

    /** 他人主页 */
    @GetMapping("/home/{userId}")
    public Result<UserHomeResp> home(@PathVariable Long userId) {
        return Result.success(userService.getUserHome(userId));
    }

    /** 更新隐私设置 */
    @PostMapping("/privacy/update")
    public Result<Void> updatePrivacy(@RequestBody PrivacyUpdateReq req) {
        privacyService.update(req);
        return Result.success("保存成功", null);
    }

    /** 拉黑 */
    @PostMapping("/blacklist/add")
    public Result<Void> blacklistAdd(@Valid @RequestBody BlacklistAddReq req) {
        blacklistService.add(req);
        return Result.success("已拉黑", null);
    }

    /** 取消拉黑 */
    @PostMapping("/blacklist/remove")
    public Result<Void> blacklistRemove(@Valid @RequestBody BlacklistRemoveReq req) {
        blacklistService.remove(req);
        return Result.success("已取消", null);
    }

    /** 黑名单列表 */
    @GetMapping("/blacklist/list")
    public Result<PageResult<BlacklistItemResp>> blacklistList(PageQuery query) {
        return Result.success(blacklistService.list(query));
    }
}
