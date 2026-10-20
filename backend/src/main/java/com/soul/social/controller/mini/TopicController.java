package com.soul.social.controller.mini;

import com.soul.social.common.result.Result;
import com.soul.social.dto.req.TopicCreateReq;
import com.soul.social.dto.resp.TopicItemResp;
import com.soul.social.service.DynamicService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端话题接口 /api/mini/topic/**。
 */
@RestController
@RequestMapping("/api/mini/topic")
public class TopicController {

    @Resource
    private DynamicService dynamicService;

    /** 话题列表 */
    @GetMapping("/list")
    public Result<List<TopicItemResp>> list() {
        return Result.success(dynamicService.listTopics());
    }

    /** 自拟话题：创建（同名启用话题则复用），返回 {topicId, name, ...} */
    @PostMapping("/create")
    public Result<TopicItemResp> create(@Valid @RequestBody TopicCreateReq req) {
        return Result.success("话题已创建", dynamicService.createTopic(req.getName()));
    }
}
