package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 标签列表响应（分类 + 标签）。
 *
 * <p>注意：接口 {@code GET /api/mini/tag/list} 的 {@code data} 直接是「分类数组」，
 * 即 {@code List<TagListResp.CategoryItem>}，不要额外包一层对象。
 */
@Data
public class TagListResp {

    @Data
    public static class CategoryItem {
        private Long categoryId;
        private String name;
        private String icon;
        private List<TagItem> tags;
    }

    @Data
    public static class TagItem {
        private Long tagId;
        private String name;
        /** 是否热门 */
        private Boolean hot;
    }
}
