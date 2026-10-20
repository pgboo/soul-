package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 后台灵魂测试题目响应（含选项，选项**返回** resultCode/score 供后台编辑）。
 */
@Data
public class AdminQuestionResp {

    private Long questionId;

    private String title;

    private String description;

    private Integer sort;

    private Integer status;

    private List<OptionItem> options;

    @Data
    public static class OptionItem {
        private Long optionId;
        private String content;
        private String resultCode;
        private Integer score;
        private Integer sort;
    }
}
