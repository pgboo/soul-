package com.soul.social.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 分页返回体 {total, pageNum, pageSize, records}。
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private Long total;

    /** 当前页码（从 1 起） */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    /** 记录列表 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long total, Long pageNum, Long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }

    /**
     * 由 MyBatis-Plus 的 IPage 构造（记录类型一致）。
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 由 IPage 的实体记录 + 转换后的记录构造（分页查询 Entity，返回 Resp 场景）。
     *
     * @param page       原始实体分页
     * @param mappedList 已转换的 Resp 列表
     */
    public static <T> PageResult<T> of(IPage<?> page, List<T> mappedList) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(),
                mappedList == null ? new ArrayList<>() : mappedList);
    }

    /**
     * 空分页结果。
     */
    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return new PageResult<>(0L, pageNum, pageSize, new ArrayList<>());
    }
}
