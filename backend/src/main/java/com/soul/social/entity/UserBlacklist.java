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
 * 用户黑名单表。
 */
@Data
@TableName("user_blacklist")
public class UserBlacklist implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 拉黑发起人 */
    private Long userId;

    /** 被拉黑人 */
    private Long targetUserId;

    /** 状态:0取消 1生效 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
