package com.soul.social.dto.req;

import com.soul.social.common.constant.CommonConstant;
import lombok.Data;

/**
 * 基础分页请求。分页参数统一 pageNum / pageSize。
 */
@Data
public class PageQuery {

    /** 页码，从 1 起 */
    private Integer pageNum = (int) CommonConstant.DEFAULT_PAGE_NUM;

    /** 每页条数 */
    private Integer pageSize = (int) CommonConstant.DEFAULT_PAGE_SIZE;

    /**
     * 归一化页码（≥1）。
     */
    public long safePageNum() {
        return (pageNum == null || pageNum < 1) ? CommonConstant.DEFAULT_PAGE_NUM : pageNum;
    }

    /**
     * 归一化每页条数（1..MAX_PAGE_SIZE）。
     */
    public long safePageSize() {
        if (pageSize == null || pageSize < 1) {
            return CommonConstant.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, CommonConstant.MAX_PAGE_SIZE);
    }
}
