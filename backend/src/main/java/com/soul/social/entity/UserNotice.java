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
 * 站内用户通知表。
 */
@Data
@TableName("user_notice")
public class UserNotice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 类型:1点赞 2评论 3回复 4打招呼 5系统 6审核 */
    private Integer noticeType;

    private String title;

    private String content;

    private Long fromUserId;

    /** 跳转目标类型:1动态 2会话 3主页 0无 */
    private Integer targetType;

    private Long targetId;

    /** 已读:0否 1是 */
    private Integer readFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
