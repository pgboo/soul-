package com.soul.social.controller.admin;

import com.soul.social.common.result.PageResult;
import com.soul.social.common.result.Result;
import com.soul.social.dto.req.AdminQuestionSaveReq;
import com.soul.social.dto.req.AdminResultSaveReq;
import com.soul.social.dto.req.PageQuery;
import com.soul.social.dto.resp.AdminQuestionResp;
import com.soul.social.dto.resp.AdminResultResp;
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
 * 后台灵魂测试管理接口 /api/admin/soulTest/**。
 */
@RestController
@RequestMapping("/api/admin/soulTest")
public class AdminSoulTestController {

    @Resource
    private AdminService adminService;

    /** 题目列表（含选项） */
    @GetMapping("/question/list")
    public Result<PageResult<AdminQuestionResp>> questionList(@RequestParam(required = false) Integer status,
                                                              PageQuery query) {
        return Result.success(adminService.questionPage(status, query));
    }

    /** 保存题目（含选项） */
    @PostMapping("/question/save")
    public Result<Void> saveQuestion(@Valid @RequestBody AdminQuestionSaveReq req) {
        adminService.saveQuestion(req);
        return Result.success("保存成功", null);
    }

    /** 结果类型列表 */
    @GetMapping("/result/list")
    public Result<List<AdminResultResp>> resultList() {
        return Result.success(adminService.resultList());
    }

    /** 保存结果类型 */
    @PostMapping("/result/save")
    public Result<Void> saveResult(@Valid @RequestBody AdminResultSaveReq req) {
        adminService.saveResult(req);
        return Result.success("保存成功", null);
    }
}
