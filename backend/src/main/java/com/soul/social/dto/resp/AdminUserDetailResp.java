package com.soul.social.dto.resp;

import java.util.List;
import lombok.Data;

/**
 * 后台用户详情响应。手机号脱敏；含资料与风险记录。
 */
@Data
public class AdminUserDetailResp {

    private Long userId;

    private String userNo;

    /** 昵称 */
    private String nickname;

    private String avatar;

    /** 性别:0未知 1男 2女 */
    private Integer gender;

    private String birthday;

    private String city;

    private String sign;

    private String intro;

    private String soulId;

    /** 资料完整度百分比 */
    private Integer completePercent;

    /** 脱敏手机号（如 138****8000） */
    private String phoneMask;

    /** 状态:1正常 2封禁 */
    private Integer status;

    /** 风控等级:0正常 1观察 */
    private Integer riskLevel;

    /** 禁言截止时间（字符串，null=未禁言） */
    private String muteEndTime;

    /** 风险记录列表（禁言/封禁/观察） */
    private List<RiskRecordItem> riskRecords;

    /** 风险记录项 */
    @Data
    public static class RiskRecordItem {
        private Long recordId;
        /** 类型:1禁言 2封禁 3观察 */
        private Integer riskType;
        private String reason;
        private String startTime;
        private String endTime;
        private Long operatorId;
        /** 状态:0解除 1生效 */
        private Integer status;
        private String createTime;
    }
}
