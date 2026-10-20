package com.soul.social.service;

import com.soul.social.common.constant.CommonConstant;

/**
 * 站内通知服务（共享）。
 * <p>统一封装 user_notice 写入，供点赞/评论/打招呼等复用。
 */
public interface NoticeService {

    /** 通知类型常量 */
    int TYPE_LIKE = 1;
    int TYPE_COMMENT = 2;
    int TYPE_REPLY = 3;
    int TYPE_GREET = 4;
    int TYPE_SYSTEM = 5;
    int TYPE_AUDIT = 6;

    /** 跳转目标类型 */
    int TARGET_NONE = 0;
    int TARGET_DYNAMIC = 1;
    int TARGET_CONVERSATION = 2;
    int TARGET_HOME = 3;

    /**
     * 创建通知。fromUserId 与 toUserId 相同（自己触发自己）时静默忽略。
     *
     * @param toUserId    接收人
     * @param fromUserId  触发人
     * @param noticeType  通知类型
     * @param title       标题
     * @param content     内容
     * @param targetType  跳转目标类型
     * @param targetId    跳转目标 ID
     */
    void createNotice(Long toUserId, Long fromUserId, int noticeType,
                      String title, String content, int targetType, Long targetId);
}
