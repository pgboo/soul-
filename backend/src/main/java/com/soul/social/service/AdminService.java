package com.soul.social.service;

import com.soul.social.dto.req.AdminQuestionSaveReq;
import com.soul.social.dto.req.AdminResultSaveReq;
import com.soul.social.dto.req.AdminTagCategorySaveReq;
import com.soul.social.dto.req.AdminTagDeleteReq;
import com.soul.social.dto.req.AdminTagSaveReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.AdminQuestionResp;
import com.soul.social.dto.resp.AdminResultResp;
import com.soul.social.dto.resp.AdminTagCategoryResp;
import com.soul.social.dto.resp.AdminTagResp;
import com.soul.social.common.result.PageResult;
import java.util.List;

/**
 * 后台管理服务（标签 + 灵魂测试）。
 */
public interface AdminService {

    // ---------- 标签分类 ----------
    List<AdminTagCategoryResp> categoryList();

    void saveCategory(AdminTagCategorySaveReq req);

    // ---------- 标签 ----------
    PageResult<AdminTagResp> tagPage(Long categoryId, Integer status, PageQuery query);

    void saveTag(AdminTagSaveReq req);

    void deleteTag(AdminTagDeleteReq req);

    // ---------- 灵魂测试题目 ----------
    PageResult<AdminQuestionResp> questionPage(Integer status, PageQuery query);

    void saveQuestion(AdminQuestionSaveReq req);

    // ---------- 灵魂测试结果 ----------
    List<AdminResultResp> resultList();

    void saveResult(AdminResultSaveReq req);
}
