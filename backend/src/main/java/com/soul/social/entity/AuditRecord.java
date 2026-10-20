package com.soul.social.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 审核记录表。对应表 audit_record。
 * <p>审核动态/评论/资料/昵称时落库，保证「谁、何时、对什么、做了什么、原因」可追溯。
 */
@Data
@TableName("audit_record")
public class AuditRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型:1动态 2评论 3资料 4昵称 */
    private Integer bizType;

    /** 业务 ID */
    private Long bizId;

    /** 提交用户 */
    private Long userId;

    /** 审核结果:1待审 2通过 3拒绝 */
    private Integer auditStatus;

    /** 审核原因 */
    private String auditReason;

    /** 审核人 */
    private Long auditorId;

    /** 审核时间 */
    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
