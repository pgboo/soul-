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
 * 动态评论表。
 */
@Data
@TableName("dynamic_comment")
public class DynamicComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dynamicId;

    private Long userId;

    /** 父评论 ID(0=一级) */
    private Long parentId;

    /** 被回复人 ID */
    private Long replyUserId;

    private String content;

    /** 状态:1正常 2删除/下架 */
    private Integer status;

    private Integer reportCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
