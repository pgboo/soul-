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
 * 用户主表（不含手机号/openid）。对应表 user。
 */
@Data
@TableName("user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对外用户编号 */
    private String userNo;

    /** 状态:1正常 2封禁 */
    private Integer status;

    /** 风控等级:0正常 1观察 */
    private Integer riskLevel;

    /** 禁言截止时间(NULL=未禁言) */
    private LocalDateTime muteEndTime;

    /** 资料完善:0否 1是 */
    private Integer profileCompleteFlag;

    /** 兴趣标签完成:0否 1是 */
    private Integer tagFlag;

    /** 灵魂测试完成:0否 1是 */
    private Integer testFlag;

    /** 最后活跃时间 */
    private LocalDateTime lastActiveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
