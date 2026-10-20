package com.soul.social.controller.mini;

import com.soul.social.common.result.Result;
import com.soul.social.dto.req.UserTagSaveReq;
import com.soul.social.dto.resp.TagListResp;
import com.soul.social.service.TagService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端标签接口 /api/mini。
 */
@RestController
@RequestMapping("/api/mini")
public class TagController {

    @Resource
    private TagService tagService;

    /** 标签列表：data 为分类数组，与接口文档 2.13 保持一致 */
    @GetMapping("/tag/list")
    public Result<List<TagListResp.CategoryItem>> list() {
        return Result.success(tagService.listTags());
    }

    /** 保存我的标签 */
    @PostMapping("/user/tag/save")
    public Result<Void> save(@Valid @RequestBody UserTagSaveReq req) {
        tagService.saveUserTags(req);
        return Result.success("保存成功", null);
    }
}
