package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminTagCategorySaveReq;
import com.soul.social.dto.req.AdminTagDeleteReq;
import com.soul.social.dto.req.AdminTagSaveReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.AdminTagCategoryResp;
import com.soul.social.dto.resp.AdminTagResp;
import com.soul.social.service.AdminService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台标签管理接口 /api/admin/tag/**。
 */
@RestController
@RequestMapping("/api/admin/tag")
public class AdminTagController {

    @Resource
    private AdminService adminService;

    /** 标签分类列表 */
    @GetMapping("/category/list")
    public Result<List<AdminTagCategoryResp>> categoryList() {
        return Result.success(adminService.categoryList());
    }

    /** 新增/编辑分类 */
    @PostMapping("/category/save")
    public Result<Void> saveCategory(@Valid @RequestBody AdminTagCategorySaveReq req) {
        adminService.saveCategory(req);
        return Result.success("保存成功", null);
    }

    /** 标签列表（后台） */
    @GetMapping("/list")
    public Result<PageResult<AdminTagResp>> list(@RequestParam(required = false) Long categoryId,
                                                 @RequestParam(required = false) Integer status,
                                                 PageQuery query) {
        return Result.success(adminService.tagPage(categoryId, status, query));
    }

    /** 新增/编辑标签 */
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody AdminTagSaveReq req) {
        adminService.saveTag(req);
        return Result.success("保存成功", null);
    }

    /** 删除标签（逻辑删除） */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody AdminTagDeleteReq req) {
        adminService.deleteTag(req);
        return Result.success("删除成功", null);
    }
}
