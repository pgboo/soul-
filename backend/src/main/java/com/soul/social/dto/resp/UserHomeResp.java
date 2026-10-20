package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 用户主页响应（他人主页，隐私过滤后）。
 */
@Data
public class UserHomeResp {

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer gender;

    /** 年龄（隐私关闭则为 null） */
    private Integer age;

    /** 城市（隐私关闭则为 null） */
    private String city;

    private String sign;

    private String intro;

    private String soulId;

    private List<TagItem> tags;

    private SoulResultItem soulResult;

    private Integer dynamicCount;

    /** 我是否已拉黑对方 */
    private Boolean blacklisted;

    /** 今日是否已打招呼 */
    private Boolean greetedToday;

    @Data
    public static class TagItem {
        private Long tagId;
        private String name;

        public TagItem() {
        }

        public TagItem(Long tagId, String name) {
            this.tagId = tagId;
            this.name = name;
        }
    }

    @Data
    public static class SoulResultItem {
        private String resultCode;
        private String name;
        private String description;
        private String socialAdvice;
    }
}
