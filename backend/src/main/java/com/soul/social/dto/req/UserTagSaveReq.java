package com.soul.social.dto.req;

import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * 保存用户标签请求。校验 3-10 个。
 */
@Data
public class UserTagSaveReq {

    @NotEmpty(message = "标签不能为空")
    private List<Long> tagIds;

    /**
     * 校验数量 3-10，非法抛 2001。
     * <p>注：数量校验放 Service 内统一处理，便于错误码返回。
     */
    public void validateCount() {
        if (tagIds == null || tagIds.size() < 3 || tagIds.size() > 10) {
            throw new BizException(ResultCode.TAG_COUNT_INVALID);
        }
    }

    /** 去重后的 tagIds */
    public List<Long> distinctTagIds() {
        return tagIds == null ? List.of() : tagIds.stream().distinct().toList();
    }
}
