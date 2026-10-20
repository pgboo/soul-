package com.soul.social.converter;

import com.soul.social.dto.resp.UserProfileResp;
import com.soul.social.entity.UserProfile;

/**
 * 用户资料 Entity ↔ Resp 转换。敏感字段在此物理隔离（Resp 不含 phone/openid）。
 */
public final class UserConverter {

    private UserConverter() {
    }

    /**
     * UserProfile → UserProfileResp。
     */
    public static UserProfileResp toProfileResp(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        UserProfileResp resp = new UserProfileResp();
        resp.setUserId(profile.getUserId());
        resp.setNickname(profile.getNickname());
        resp.setAvatar(profile.getAvatar());
        resp.setGender(profile.getGender());
        resp.setBirthday(profile.getBirthday());
        resp.setCity(profile.getCity());
        resp.setSign(profile.getSign());
        resp.setIntro(profile.getIntro());
        resp.setEmotionStatus(profile.getEmotionStatus());
        resp.setFriendGoal(profile.getFriendGoal());
        resp.setSoulId(profile.getSoulId());
        resp.setCompletePercent(profile.getCompletePercent());
        resp.setNicknameModifyCount(profile.getNicknameModifyCount());
        resp.setCreateTime(profile.getCreateTime());
        return resp;
    }

    /**
     * 由生日计算年龄。出生日期为空返回 null。
     */
    public static Integer calcAge(java.time.LocalDate birthday) {
        if (birthday == null) {
            return null;
        }
        return java.time.Period.between(birthday, java.time.LocalDate.now()).getYears();
    }
}
