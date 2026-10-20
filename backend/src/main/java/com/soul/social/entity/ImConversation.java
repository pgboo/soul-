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
 * IM 会话表（沿用既有表，严禁新建 chat 系表）。
 */
@Data
@TableName("im_conversation")
public class ImConversation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话唯一标识（min(uidA,uidB) + "_" + max(uidA,uidB)） */
    private String sessionUuid;

    /** 会话归属用户（本人） */
    private Long userId;

    /** 对方用户 */
    private Long targetUserId;

    /** 类型:1普通 2打招呼 3AI */
    private Integer sessionType;

    private Integer unreadCount;

    private String lastMsgPreview;

    private LocalDateTime lastMsgTime;

    private LocalDateTime sortTime;

    /** 置顶:0否 1是 */
    private Integer topFlag;

    /** 隐藏:0否 1是 */
    private Integer hideFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
