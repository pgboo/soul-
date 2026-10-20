package com.soul.social.dto.req;

import java.util.List;
import lombok.Data;

/**
 * 通知已读请求。noticeIds 为空则全部已读。
 */
@Data
public class NoticeReadReq {

    private List<Long> noticeIds;
}
