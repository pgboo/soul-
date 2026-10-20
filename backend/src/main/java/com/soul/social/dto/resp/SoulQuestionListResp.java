package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 灵魂测试题目响应（**选项不返回 result_code/score**，防作弊）。
 */
@Data
public class SoulQuestionListResp {

    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem {
        private Long questionId;
        private String title;
        private String description;
        private Integer sort;
        private List<OptionItem> options;
    }

    @Data
    public static class OptionItem {
        private Long optionId;
        private String content;
        private Integer sort;
    }
}
