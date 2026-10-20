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
 * IM 消息表（沿用既有表）。
 */
@Data
@TableName("im_message")
public class ImMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String messageUuid;

    private String sessionUuid;

    /** 场景:1单聊 */
    private Integer scene;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    /** 类型:text/image/voice（本期仅 text） */
    private String contentType;

    /** 发送状态:0失败 1成功 2发送中 */
    private Integer sendStatus;

    /** 已读:0否 1是 */
    private Integer readFlag;

    /** 撤回:0否 1是 */
    private Integer recallFlag;
    /** 发送者已删除:0否 1是 */
    private Integer fromDeleteFlag;

    /** 接收者已删除:0否 1是 */
    private Integer toDeleteFlag;


    private LocalDateTime sendTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
