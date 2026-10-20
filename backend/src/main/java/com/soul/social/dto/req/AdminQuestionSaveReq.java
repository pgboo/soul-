package com.soul.social.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

/**
 * 后台保存灵魂测试题目（含选项）。
 */
@Data
public class AdminQuestionSaveReq {

    /** 可选，编辑时传 */
    private Long questionId;

    @NotBlank(message = "题目标题不能为空")
    private String title;

    private String description;

    private Integer sort;

    private Integer status;

    @Valid
    private List<OptionItem> options;

    @Data
    public static class OptionItem {
        /** 可选，编辑时传 */
        private Long optionId;
        private String content;
        private String resultCode;
        private Integer score;
        private Integer sort;
    }
}
