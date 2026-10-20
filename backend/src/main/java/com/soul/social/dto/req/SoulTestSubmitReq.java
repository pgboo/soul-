package com.soul.social.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * 提交灵魂测试请求。
 */
@Data
public class SoulTestSubmitReq {

    @NotEmpty(message = "答案不能为空")
    @Valid
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        /** 题目 ID */
        private Long questionId;
        /** 选项 ID */
        private Long optionId;
    }
}
