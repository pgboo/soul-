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
 * 风控用户记录表（禁言/封禁/观察）。对应表 risk_user_record。
 */
@Data
@TableName("risk_user_record")
public class RiskUserRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 类型:1禁言 2封禁 3观察 */
    private Integer riskType;

    /** 原因 */
    private String reason;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间（NULL=永久） */
    private LocalDateTime endTime;

    /** 操作人 */
    private Long operatorId;

    /** 状态:0解除 1生效 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
