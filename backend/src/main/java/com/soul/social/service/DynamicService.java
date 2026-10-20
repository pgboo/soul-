package com.soul.social.service;

import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.DynamicListReq;
import com.soul.social.dto.req.DynamicPublishReq;
import com.soul.social.dto.resp.DynamicDetailResp;
import com.soul.social.dto.resp.DynamicItemResp;
import java.util.List;

/**
 * 广场动态服务。
 */
public interface DynamicService {

    /** 发布瞬间，返回 dynamicId。 */
    Long publish(DynamicPublishReq req);

    /** 瞬间列表。 */
    PageResult<DynamicItemResp> list(DynamicListReq req);

    /** 瞬间详情。 */
    DynamicDetailResp detail(Long id);

    /** 删除瞬间（仅作者，逻辑删除）。 */
    void delete(Long id, Long userId);

    /** 话题列表（启用，按热度/排序）。 */
    List<com.soul.social.dto.resp.TopicItemResp> listTopics();

    /**
     * 自拟话题：创建新话题；若同名启用话题已存在则直接复用（不重复建）。
     *
     * @param name 话题名（可带前导 #，会自动清理）
     * @return 话题信息（含 topicId）
     */
    com.soul.social.dto.resp.TopicItemResp createTopic(String name);
}
