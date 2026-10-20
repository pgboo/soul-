package com.soul.social.service;

import com.soul.social.dto.req.SoulTestSubmitReq;
import com.soul.social.dto.resp.SoulQuestionListResp;
import com.soul.social.dto.resp.SoulTestResultResp;

/**
 * 灵魂测试服务（用户端）。
 */
public interface SoulTestService {

    /** 题目 + 选项（仅启用，按 sort，选项不含 resultCode/score）。 */
    SoulQuestionListResp questions();

    /** 提交测试，计算 result_code，保存记录，置 test_flag=1。 */
    SoulTestResultResp submit(SoulTestSubmitReq req);

    /** 我的最后一次结果。 */
    SoulTestResultResp myResult();
}
