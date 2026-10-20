package com.soul.social.service;

import com.soul.social.dto.req.UserTagSaveReq;
import com.soul.social.dto.resp.TagListResp;
import java.util.List;

/**
 * 标签服务（用户端）。
 */
public interface TagService {

    /** 标签分类 + 标签列表（仅启用，按 sort）。返回分类数组，直接作为 data 下发。 */
    List<TagListResp.CategoryItem> listTags();

    /** 保存用户标签（3-10 个，校验启用）。 */
    void saveUserTags(UserTagSaveReq req);
}
